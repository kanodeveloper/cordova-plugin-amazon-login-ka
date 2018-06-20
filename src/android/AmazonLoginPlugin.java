package com.kanoapps.cordova.amazonlogin;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

/**
 * Cordova plugin that allows for an arbitrarly sized and positioned WebView to be shown ontop of the canvas
 */
public class AmazonLoginPlugin extends CordovaPlugin {

    private static final String TAG = "AmazonLoginPlugin";
    private static final String PREFS_NAME = "AmazonLoginPlugin";

    private RequestContext requestContext;
    private CallbackContext responseCallback;

    /**
     * Initializes the plugin
     *
     * @param cordova The context of the main Activity.
     * @param webView The associated CordovaWebView.
     */
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {

        requestContext = RequestContext.create( this.cordova.getActivity() );

        requestContext.registerListener(new AuthorizeListener() {

            /* Authorization was completed successfully. */
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                Log.v(TAG, "AmazonLoginPlugin :: login success");
                fetchUserProfile();
            }
            /* There was an error during the attempt to authorize the application */
            @Override
            public void onError(AuthError authError) {
                Log.e(TAG, "AmazonLoginPlugin :: login failed", authError);
            }

            /* Authorization was cancelled before it could be completed. */
            @Override
            public void onCancel(AuthCancellation authCancellation) {
                Log.e(TAG, "AmazonLoginPlugin :: login cancelled");
            }
        });
    }

    /**
     * Executes the request and returns PluginResult
     *
     * @param  action
     * @param  args
     * @param  callbackContext
     * @return boolean
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Context context = this.cordova.getActivity().getApplicationContext();

        responseCallback = callbackContext;

        if (action.equals("login")) {

            try {
                AuthorizationManager.authorize(
                        new AuthorizeRequest.Builder(requestContext)
                                .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                                .build()
                );

            } catch (Exception e) {
                responseCallback.error( e.getMessage() );
            }

            return true;
        }
        else if (action.equals("logout")) {

            try {
                AuthorizationManager.signOut(context, new Listener<Void, AuthError>() {
                    @Override
                    public void onSuccess(Void response) {
                        responseCallback.success();
                    }

                    @Override
                    public void onError(AuthError authError) {
                        responseCallback.error( authError.getMessage() );
                    }
                });
            } catch (Exception e) {
                responseCallback.error( e.getMessage() );
            }

            return true;
        }
        else if (action.equals("getToken")) {

            try {
                Scope[] scopes = {ProfileScope.profile(), ProfileScope.postalCode()};
                AuthorizationManager.getToken(context, scopes, new Listener<AuthorizeResult, AuthError>() {
                    @Override
                    public void onSuccess(AuthorizeResult result) {

                        final String access_token = result.getAccessToken();

                        Log.v(TAG, "AmazonLoginPlugin :: getToken success " + access_token);

                        if (access_token != null) {
                            try {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("access_token", access_token);
                                responseCallback.success(userInfo);
                            }
                            catch (JSONException e) {
                                responseCallback.error( e.getMessage() );
                            }
                        } else {
                            responseCallback.success();
                        }
                    }

                    @Override
                    public void onError(AuthError authError) {
                        responseCallback.error( authError.getMessage() );
                    }
                });
            } catch (Exception e) {
                responseCallback.error( e.getMessage() );
            }

            return true;
        }

        // Default response to say the action hasn't been handled
        return false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        requestContext.onResume();
    }

    private void fetchUserProfile() {

        try {
            User.fetch( this.cordova.getActivity(), new Listener<User, AuthError>() {

                /* fetch completed successfully. */
                @Override
                public void onSuccess(User user) {

                    final String name = user.getUserName();
                    final String email = user.getUserEmail();
                    final String account = user.getUserId();
                    final String zipCode = user.getUserPostalCode();

                    try {
                        JSONObject userInfo = new JSONObject();
                        userInfo.put("name", name);
                        userInfo.put("email", email);
                        userInfo.put("account", account);
                        userInfo.put("zipCode", zipCode);
                        responseCallback.success(userInfo);
                    }
                    catch (JSONException e) {
                        responseCallback.error( e.getMessage() );
                    }
                }

                /* There was an error during the attempt to get the profile. */
                @Override
                public void onError(AuthError authError) {
                    responseCallback.error( authError.getMessage() );
                }
            });
        } catch (Exception e) {
            responseCallback.error( e.getMessage() );
        }
    }
}

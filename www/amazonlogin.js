var exec = require('cordova/exec');

var AmazonLoginPlugin = function() {};

AmazonLoginPlugin.login = function(success, error) {
    exec(success, error, "AmazonLoginPlugin", "login");
};

AmazonLoginPlugin.logout = function(success, error) {
    exec(success, error, "AmazonLoginPlugin", "logout");
};

AmazonLoginPlugin.getToken = function(success, error) {
    exec(success, error, "AmazonLoginPlugin", "getToken");
};

module.exports = AmazonLoginPlugin;
#!/usr/bin/env node
'use strict';
var fs = require('fs');

var configobj = JSON.parse(fs.readFileSync('res/config/amazon.json', 'utf8'));

var filename = "platforms/android/app/src/main/assets/api_key.txt";
fs.writeFileSync(filename, configobj.AMAZON_API_KEY, 'utf8');
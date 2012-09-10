#!/bin/bash

echo -ne "Please enter plugin name: [MessageBox] "
read $pluginName;
if [[ -z $pluginName ]]; then pluginName="MessageBox"; fi;

if [[ ! -d cordova/cordova-ios ]]; then
	git submodule update --init cordova/cordova-ios
fi;

path=samples/ios/$pluginName;
rm -rf $path
cordova/cordova-ios/bin/create $path org.apache.cordova.plugins.$pluginName $pluginName

cp www/*.js $path/www/js;
cp samples/ios/www/*.js $path/www/js;
cp samples/ios/www/*.css $path/www/css;
cp samples/ios/www/*.html $path/www;
ln -s ./../../../../../src/ios $path/$pluginName/Plugins/$pluginName;
sed "/<key>Device<\/key>/i\ \t\t<key>$pluginName<\/key>\n\t\t<string>$pluginName<\/string>" -i $path/$pluginName/Cordova.plist

echo -ne "Drag \"Plugins/$pluginName\" folder to XCode then build/run.\n"

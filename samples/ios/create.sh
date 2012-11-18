#!/bin/bash
INFO="\033[32m\033[1m[INFO]\033[22m\033[39m"

echo -ne "$INFO Please enter plugin name: [FacebookConnect] "
read $pluginName;
if [[ -z $pluginName ]]; then pluginName="FacebookConnect"; fi;

if [[ ! -d cordova/cordova-ios ]]; then
	git submodule update --init cordova/cordova-ios
fi;

path=samples/ios/$pluginName;
rm -rf $path
cordova/cordova-ios/bin/create --shared $path org.apache.cordova.plugins.$pluginName $pluginName

cp www/*.js $path/www/js;
cp samples/ios/www/*.js $path/www/js;
cp samples/ios/www/*.css $path/www/css;
cp samples/ios/www/*.html $path/www;
ln -s ./../../../../../src/ios $path/$pluginName/Plugins/$pluginName;
sed "/<key>Device<\/key>/i\ \t\t<key>$pluginName<\/key>\n\t\t<string>$pluginName<\/string>" -i $path/$pluginName/Cordova.plist

echo -ne "$INFO Drag \"Plugins/$pluginName\" folder to Xcode \"Plugins\" folder then build/run.\n"

##
## ()
##

ANDROID_HOME := ${HOME}/lib/android-sdk-linux
ANDROID_BUILD_TOOLS := ${ANDROID_HOME}/build-tools/21.1.2
AAPT :=     ${ANDROID_BUILD_TOOLS}/aapt
DX :=       ${ANDROID_BUILD_TOOLS}/dx
ZIPALIGN := ${ANDROID_BUILD_TOOLS}/zipalign
LINT :=     ${ANDROID_HOME}/tools/lint
JAVAC = javac
JARSIGNER = jarsigner

minSdkVersion := 11
ANDROID_JAR := ${ANDROID_HOME}/platforms/android-${minSdkVersion}/android.jar

DEV_HOME := $(shell pwd)

resources = res/drawable/blitz2_1.png res/layout/main.xml res/menu/actions.xml res/values/arrays.xml res/values/strings.xml res/xml/preferences.xml

all: bin/blitz2_1.apk

#
# Before running the commands in this Makefile, you have to create the keyfile.
#
#blitz2_1.keystore:
# keytool -genkeypair -validity 10000 -dname "CN=blitz2_1.zaitcev.us, OU=Brain, O=Pete Zaitcev, L=Los Lunas, S=New Mexico, C=US" -keystore blitz2_1.keystore -storepass miageta -keypass shitaten -alias Blitz21Key -keyalg RSA -v

# -v
src/us/zaitcev/package1/R.java:  AndroidManifest.xml $(resources)
	${AAPT} package -f -m -J ${DEV_HOME}/src -M AndroidManifest.xml -S ${DEV_HOME}/res -I ${ANDROID_JAR}

# There's actually a ton of classes that javac produces, but oh well.
# This is exactly why javists use Ant.
# XXX replace *.java with the list - what about the full path?
# -verbose
# XXX Create some kind of .PHONY: prep stage that runs mkdir
obj/us/zaitcev/package1/R.class obj/us/zaitcev/package1/HelloAndroid.class obj/us/zaitcev/package1/SettingsFragment.class: src/us/zaitcev/package1/R.java src/us/zaitcev/package1/HelloAndroid.java src/us/zaitcev/package1/SettingsFragment.java
	mkdir -p bin docs lib obj
	${JAVAC} -Xlint:all -d ${DEV_HOME}/obj -classpath ${ANDROID_JAR}:${DEV_HOME}/obj -sourcepath ${DEV_HOME}/src ${DEV_HOME}/src/us/zaitcev/package1/*.java
 
# --verbose
bin/classes.dex: obj/us/zaitcev/package1/R.class obj/us/zaitcev/package1/HelloAndroid.class obj/us/zaitcev/package1/SettingsFragment.class
	${DX} --dex --output=bin/classes.dex ${DEV_HOME}/obj ${DEV_HOME}/lib

# -v
# Alternatively we could do something like this:
#	${AAPT} package -f -M AndroidManifest.xml -S ${DEV_HOME}/res -I ${ANDROID_HOME}/platforms/android-7/android.jar -F $@ ${DEV_HOME}/bin
# But the above only takes a directory as argument, so it sweeps
# the old APKs too.
# A downside of the below is the extra output.
bin/blitz2_1.unsigned.apk:  AndroidManifest.xml $(resources) bin/classes.dex
	${AAPT} package -f -F $@ -M AndroidManifest.xml -S ${DEV_HOME}/res -I ${ANDROID_JAR}
	${AAPT} add -k $@ bin/classes.dex

# -verbose
bin/blitz2_1.signed.apk: bin/blitz2_1.unsigned.apk blitz2_1.keystore
	${JARSIGNER} -keystore blitz2_1.keystore -storepass miageta -keypass shitaten -signedjar bin/blitz2_1.signed.apk bin/blitz2_1.unsigned.apk Blitz21Key

# -v
bin/blitz2_1.apk:  bin/blitz2_1.signed.apk
	${ZIPALIGN} -f 4 bin/blitz2_1.signed.apk bin/blitz2_1.apk

# Lint scans build results too, so make it depend on the build.
lint:  all
	${LINT} --sources src --resources res --classpath obj .

clean:
	rm -f src/us/zaitcev/package1/R.java
	rm -f obj/us/zaitcev/package1/*.class
	rm -f bin/classes.dex
	rm -f bin/blitz2_1*.apk

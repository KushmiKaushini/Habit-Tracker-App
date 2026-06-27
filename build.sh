#!/bin/sh
# Build helper for Windows/MSYS
# Uses Java 17 (Temurin) for Android compatibility
export ANDROID_HOME=/c/Users/kushm/AppData/Local/Android/Sdk
export JAVA_HOME=/c/Users/kushm/java/jdk-17.0.12+7
export PATH=/c/Users/kushm/java/jdk-17.0.12+7/bin:$PATH
./gradlew "$@"

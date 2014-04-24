project(android-sensor-app)
cmake_minimum_required(VERSION 2.8)

include(def_apk)

def_apk(AndroidSensorApp
  JNI_DIR "${CMAKE_CURRENT_SOURCE_DIR}/jni"
  JNI_SOURCES NativeSensorInterface.cpp
  JNI_LIB NativeSensorInterface
  )
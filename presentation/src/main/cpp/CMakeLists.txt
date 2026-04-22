cmake_minimum_required(VERSION 3.10.2)
project("fumi-native")

# This dummy library forces the Android Gradle Plugin to include libc++_shared.so
add_library(fumi-native SHARED dummy.cpp)

find_library(log-lib log)
target_link_libraries(fumi-native ${log-lib})

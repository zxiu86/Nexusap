# Olympus Cleaner Native Libraries (jniLibs)

This directory contains the native shared libraries for Olympus Cleaner:

- `arm64-v8a/libmtxv8a.so`: 64-bit ARM architecture (modern Android devices)
- `armeabi-v7a/libmtxv7a.so`: 32-bit ARM architecture (legacy Android devices)

## How to update:
Replace the placeholder files with your compiled `.so` binaries.
The application will automatically detect and load the appropriate native library according to the device's CPU architecture (`arm64-v8a` or `armeabi-v7a`).

# Olympus Cleaner Native Libraries (jniLibs)

This directory contains the real compiled native shared libraries for Olympus Cleaner (MTX engine):

- `arm64-v8a/libolympus_cleaner.so`: 64-bit ARM architecture (ARM aarch64, modern Android devices)
- `armeabi-v7a/libolympus_cleaner.so`: 32-bit ARM architecture (ARM v7a EABI5, legacy Android devices)

## Architecture & Integration:
The application loads `olympus_cleaner` via `System.loadLibrary("olympus_cleaner")` in `OlympusCleanerEngine.kt`.
It binds to:
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeCreate`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeCreateAdvanced`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeProcessBitmap`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeProcessByteBuffer`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeCancel`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeGetVersion`
- `Java_com_olympus_cleaner_OlympusCleanerEngine_nativeDestroy`


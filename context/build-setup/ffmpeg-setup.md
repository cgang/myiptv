# FFmpeg AAR Setup

Since some IPTV sources use MP2/MP3 audio (not natively supported by Android), FFmpeg extensions are required.

## Setup Steps

1. Clone the [AndroidX Media](https://github.com/androidx/media) repository:
   ```bash
   git clone https://github.com/androidx/media.git
   ```

2. Follow the instructions in `libraries/decoder_ffmpeg/README.md`

3. Select the required audio encoders for your IPTV source (e.g., `mp3`, `aac`, `ac-3`)

4. Build FFmpeg and generate the AAR:
   ```bash
   ./gradlew extension-ffmpeg:assembleRelease
   ```

5. Copy the generated AAR file to `app/libs/`

## Building the App

This is a standard Android application. No special build steps are required beyond the FFmpeg setup.

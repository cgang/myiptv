# Dependencies

## Key Libraries

| Library | Purpose |
|---------|---------|
| `androidx.media3:media3-exoplayer` | Core playback functionality |
| `androidx.media3:media3-ui` | Player UI components |
| `com.squareup.okhttp3:okhttp` | Network operations |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Lifecycle-aware components |
| `androidx.preference:preference-ktx` | Settings management |
| FFmpeg extension AAR files | Custom compiled for MP2/MP3 support |

## FFmpeg Extension

Required for MP2/MP3 audio codec support. Pre-compiled AAR files must be placed in `app/libs/` directory.

See [FFmpeg Setup](../build-setup/ffmpeg-setup.md) for build instructions.

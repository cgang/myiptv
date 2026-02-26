# MyIPTV - A Simple IPTV Player

A lightweight IPTV player for Android TV built with Android Media3 ExoPlayer and FFmpeg library.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20TV-green.svg)

## Features

- 📺 M3U playlist support with EPG integration
- 🎬 Support for MP2/MP3 audio streams via FFmpeg
- 🌐 RTP/UDP multicast streaming
- 🔧 Easy configuration via settings menu
- 📱 Optimized for Android TV remote control

## Prerequisites

- **Required**: RTP/UDP multicast streams provider
- **Optional**: HTTP streaming server (e.g., [udpxy](https://github.com/pch/udpxy)) for HTTP-based IPTV
- **Optional**: EPG provider for program guide data

## Build Instructions

### FFmpeg AAR Setup

Since some IPTV sources use MP2/MP3 audio (not natively supported by Android), FFmpeg extensions are required.

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

### Building the App

This is a standard Android application. No special build steps are required beyond the FFmpeg setup.

## Usage

### Remote Control Navigation

| Button | Action |
|--------|--------|
| **OK** | Open playlist |
| **Left/Right** | Switch channel groups |
| **Up/Down** | Navigate channels |
| **MENU** | Open settings |

### M3U Playlist Format

Configure an M3U URL as your streaming source. The playlist should follow this format:

```m3u
#EXTM3U
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
http://192.168.1.1:4022/udp/239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtp://239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
udp://239.9.9.9:9999
```

**Default URL**: `http://openwrt.lan/iptv.m3u`

#### Supported Protocols

- `http://` - HTTP streaming
- `rtp://` / `udp://` - RTP/UDP multicast (experimental)
  - Configure multicast interface in settings
- `rtsp://` - RTSP streaming (SMIL file support)

#### SMIL File Support

Channels can reference SMIL files for dynamic stream URLs:

```m3u
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtsp://server.example.com/stream.smil
```

When a channel URL ends with `.smil` or `.smi`, the player automatically downloads and parses the SMIL file to extract the actual video URL.

### EPG (Electronic Program Guide)

Configure an optional EPG URL pointing to an XMLTV file:

- Use `x-tvg-url` in the M3U file to specify the EPG source
- Use `tvg-id` to match channels with EPG entries (no name matching yet)

## Known Limitations

- ❌ No channel selection by number input
- ⚠️ Multicast support is experimental

## Configuration

Access the settings page via the **MENU** button to configure:

- Custom M3U playlist URL
- Multicast interface selection
- EPG source URL
- Other playback preferences

## License

This project is released under the [MIT License](LICENSE).

---

📖 [中文版本](README.zh.md)

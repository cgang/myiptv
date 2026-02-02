# A Simple IPTV player
This player is based on Android media3 ExoPlayer and FFmpeg library, to provide a simple player for IPTV user.

## Prerequisite
- RTP/UDP multicast streams provider.
- (Optional) HTTP streaming server, such as udpxy that provides a HTTP support for IPTV.
- (Optional) EPG provider

## Compile
Since there are IPTV source support MP2/MP3 audio only, and which is not supported by Android platform,
FFmpeg extensions is required to be complied, and the compiled AAR files can be put to app/libs/ folder.
There is nothing need to be noted for compiling, as it's a very simple Android  application.

### FFmpeg AAR
Clone https://github.com/androidx/media and follow instructions in libraries/decoder\_ffmpeg/README.md
Select needed encoder for your IPTV, for example, mp3, aac, ac-3
After finished steps to compile ffmpeg, use following command to create FFmpeg AAR:
```
./gradlew extension-ffmpeg:assembleRelease
```
Copy the output AAR to your app/libs at the end.

## Usage
Suppose you have a typical Android TV remote controller, you can use OK button to get the playlist,
Left/Right button to switch group, and Up/Down to select channel.
To configure this application, use MENU button to open settings page.

### M3U Playlist
A M3U URL is used to tell player where is the streaming source, it needs to be a M3U file with following syntax:
```
#EXTM3U
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
http://192.168.1.1:4022/udp/239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtp://239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
udp://239.9.9.9:9999
```

This player will try to open http://openwrt.lan/iptv.m3u by default.

Use rtp:// or udp:// for RTP/UDP multicast support, please note multicast support is still experimental.
You can specify multicast interface in settings from menu.

Individual channel URLs can also reference SMIL files:
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtsp://server.example.com/stream.smil
```
When a channel URL ends with .smil or .smi, the player will automatically download and parse the SMIL file to extract the actual video URL for playback.

### EPG URL
An optional EPG URL is used to tell player where is the EPG, it needs to point to a XMLTV file.

Use `x-tvg-url` to specify EPG URL to get EPG (in XMLTV format).
Use `tvg-id` to match channel in EPG, there is no name match (yet).

### Known Limitations
- No channel selection by number

## License
This program is released under MIT license, see LICENSE for detail.

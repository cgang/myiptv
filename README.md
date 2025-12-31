# A Simple IPTV player
This player is based on Android media3 ExoPlayer and FFmpeg library, to provide a simple player for IPTV user.

## Prerequisite
- HTTP streaming server, such as udpxy that provides a HTTP support for IPTV.
- (Optional) EPG provider
- For direct RTP/UDP multicast streams, no HTTP relay server is required

## Compile
Since there are IPTV source support MP2/MP3 audio only, and which is not supported by Android platform,
FFmpeg extensions is required to be complied, and the compiled AAR files can be put to app/libs/ folder.
There is nothing need to be noted for compiling, as it's a very simple Android  application.

## Usage
Suppose you have a typical Android TV remote controller, you can use OK button to get the playlist,
Left/Right button to switch group, and Up/Down to select channel.
To configure this application, use MENU button to open settings page.

### M3U URL
A M3U URL is used to tell player where is the streaming source, it needs to be a M3U file with following syntax:
```
#EXTM3U
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
http://192.168.1.1:4022/udp/239.9.9.9:9999
```
Use `x-tvg-url` to specify EPG URL to get EPG (in XMLTV format).
Use `tvg-id` to match channel in EPG, there is no name match (yet).
This player will try to open http://openwrt.lan/iptv.m3u by default.

Individual channel URLs can also reference SMIL files:
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtsp://server.example.com/stream.smil
```
When a channel URL ends with .smil or .smi, the player will automatically download and parse the SMIL file to extract the actual video URL for playback.

### RTP/UDP Multicast Support (Under Development)
The player now supports direct RTP/UDP multicast streams without requiring an HTTP relay server. This feature is currently under development and should be considered experimental. You can specify multicast streams using the following formats:

RTP multicast (with automatic interface detection):
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtp://239.9.9.9:9999
```

UDP multicast (with automatic interface detection):
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
udp://239.9.9.9:9999
```

RTP multicast (with specific interface):
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtp://eth0@239.9.9.9:9999
```

UDP multicast (with specific interface):
```
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
udp://wlan0@239.9.9.9:9999
```

### EPG URL
An optional EPG URL is used to tell player where is the EPG, it needs to point to a XMLTV file.

### Known Limitations
- No channel selection by number

## License
This program is released under MIT license, see LICENSE for detail.

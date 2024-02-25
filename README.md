# A Simple IPTV player
This player is based on Android media3 ExoPlayer and FFmpeg library, to provide a simple player for IPTV user.

## Prerequisite
- HTTP streaming server, such as udpxy that provides a HTTP support for IPTV.
- (Optional) EPG provider

## Compile
Since there are IPTV source support MP2/MP3 audio only, and which is not supported by Android platform,
FFmpeg extensions is required to be complied, and the compiled AAR files can be put to app/libs/ folder.
There is nothing need to be noted for compiling, as it's a very simple Android  application.

## Usage
Suppose you have a typical Android TV remote controller, you can use OK button to get the playlist,
Left/Right button to switch group, and Up/Down to select channel.
To configure this application, use MENU button to open settings page.
There are two config currently, M3U URL and EPG URL.
### M3U URL
A M3U URL is used to tell player where is the streaming source, it needs to be a M3U file with following syntax:
```
#EXTM3U
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
http://192.168.1.1:4022/udp/239.9.9.9:9999
```
The `tvg-id` is used for to match channel in EPG, there is no name match (yet).
This player will try to open http://openwrt.lan/iptv.m3u by default.

### EPG URL
An EPG URL is used to tell player where is the EPG, it needs to point to a XMLTV file.

## License
This program is released under MIT license, see LICENSE for detail.

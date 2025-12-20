# SMIL URL Support for MyIPTV

This module provides support for handling SMIL URLs within M3U playlists in the MyIPTV application.

## Overview

MyIPTV now supports SMIL URLs as channel sources in M3U playlists. When the application encounters a channel URL ending with `.smil` or `.smi`, it will:

1. Download the SMIL file
2. Parse the SMIL content to extract video URLs
3. Use the resolved video URL for playback

## How It Works

1. **Detection**: The system detects SMIL URLs by checking the file extension (`.smil` or `.smi`)
2. **Resolution**: When parsing an M3U playlist, any SMIL URLs are automatically resolved
3. **Playback**: The actual video URL extracted from the SMIL file is used for playback

## Supported SMIL Elements

- `<smil>`: Root element
- `<body>`: Container for media elements
- `<video>`: Video media elements with `src` attribute
- `<switch>`: Alternative media versions (uses the first/preferred version)

## Example M3U Playlist with SMIL URLs

```
#EXTM3U
#EXTINF:-1 tvg-id="1" group-title="News",CNN News
rtsp://news.example.com/cnn.smil
#EXTINF:-1 tvg-id="2" group-title="Sports",ESPN Sports
rtsp://sports.example.com/espn.smil
#EXTINF:-1 tvg-id="3" group-title="Movies",HBO Movies
http://movies.example.com/hbo.m3u8
```

In this example, the first two channels use SMIL URLs, while the third uses a direct stream URL.

## SMIL File Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<smil>
  <body>
    <switch>
      <video src="rtsp://stream.example.com/high_quality" />
      <video src="rtsp://stream.example.com/low_quality" />
    </switch>
  </body>
</smil>
```

The system will extract the first video URL from the switch element for playback.

## Error Handling

If a SMIL URL cannot be resolved for any reason (network error, malformed SMIL, etc.), the system will log a warning and continue with the original URL.
# MyIPTV - 简易 IPTV 播放器

一款基于 Android Media3 ExoPlayer 和 FFmpeg 库的轻量级 Android TV IPTV 播放器。

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20TV-green.svg)

## 功能特性

- 📺 支持 M3U 播放列表与 EPG 电子节目单
- 🎬 通过 FFmpeg 支持 MP2/MP3 音频流
- 🌐 支持 RTP/UDP 组播流
- 🔧 通过设置菜单轻松配置
- 📱 针对 Android TV 遥控器优化

## 前置要求

- **必需**: RTP/UDP 组播流提供商
- **可选**: HTTP 流媒体服务器（如 [udpxy](https://github.com/pch/udpxy)）用于 HTTP 协议的 IPTV
- **可选**: EPG 提供商用于节目指南数据

## 编译说明

### FFmpeg AAR 配置

由于部分 IPTV 源使用 MP2/MP3 音频格式（Android 不原生支持），需要 FFmpeg 扩展支持。

1. 克隆 [AndroidX Media](https://github.com/androidx/media) 仓库：
   ```bash
   git clone https://github.com/androidx/media.git
   ```

2. 按照 `libraries/decoder_ffmpeg/README.md` 中的说明操作

3. 根据您的 IPTV 源选择所需的音频编码器（如 `mp3`、`aac`、`ac-3`）

4. 编译 FFmpeg 并生成 AAR 文件：
   ```bash
   ./gradlew extension-ffmpeg:assembleRelease
   ```

5. 将生成的 AAR 文件复制到 `app/libs/` 目录

### 构建应用

这是一个标准的 Android 应用程序。除 FFmpeg 配置外，无需特殊构建步骤。

## 使用说明

### 遥控器操作

| 按键 | 功能 |
|------|------|
| **确认 (OK)** | 打开播放列表 |
| **左/右** | 切换频道分组 |
| **上/下** | 选择频道 |
| **☰ MENU** (菜单) | 打开设置 |

### M3U 播放列表格式

配置 M3U URL 作为流媒体源。播放列表应遵循以下格式：

```m3u
#EXTM3U
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
#EXTINF:-1 tvg-id="1" group-title="分组",频道名称
http://192.168.1.1:4022/udp/239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="分组",频道名称
rtp://239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="分组",频道名称
udp://239.9.9.9:9999
```

**默认地址**: `http://openwrt.lan/iptv.m3u`

#### 支持的协议

- `http://` - HTTP 流媒体
- `rtp://` / `udp://` - RTP/UDP 组播（实验性）
  - 可在设置中配置组播接口
- `rtsp://` - RTSP 流媒体（支持 SMIL 文件）

#### SMIL 文件支持

频道 URL 可以引用 SMIL 文件以获取动态流地址：

```m3u
#EXTINF:-1 tvg-id="1" group-title="分组",频道名称
rtsp://server.example.com/stream.smil
```

当频道 URL 以 `.smil` 或 `.smi` 结尾时，播放器会自动下载并解析 SMIL 文件，提取实际的视频播放地址。

### EPG 电子节目单

配置可选的 EPG URL，指向 XMLTV 格式的文件：

- 在 M3U 文件中使用 `x-tvg-url` 指定 EPG 源
- 使用 `tvg-id` 匹配频道与 EPG 条目（暂不支持名称匹配）

## 已知限制

- ❌ 不支持数字键直接选台
- ⚠️ 组播功能仍处于实验阶段

## 配置选项

按 **☰ MENU** (菜单) 键访问设置页面，可配置：

- 自定义 M3U 播放列表地址
- 组播接口选择
- EPG 源地址
- 其他播放偏好设置

## 许可证

本项目采用 [MIT 许可证](LICENSE)。

---

📖 [English Version](README.md)

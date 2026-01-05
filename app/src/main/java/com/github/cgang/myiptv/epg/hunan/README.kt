/*
 * Hunan Telcom IPTV EPG Parser
 *
 * This module provides parsing support for the custom EPG format used by Hunan Telcom IPTV provider.
 * The format is different from standard XMLTV and contains specific elements and abbreviations.
 *
 * Key elements in the Hunan EPG format:
 * - <epg>: Root element
 * - <l>: List container
 * - <il>: Inner list container
 * - <i>: Individual channel item
 * - <id>: Channel ID
 * - <name>: Channel name
 * - <arg_list>: Arguments list containing additional channel info
 * - <c_no>: Channel number (user aware channel no)
 * - <channel_id>: Internal channel identifier
 * - <current_playbill>: Current program information
 * - <playbill_info>: Detailed program timing info
 * - <video_id>: Video identifier
 * - <day>: Program date (format: YYYYMMDD)
 * - <begin_time>: Program start time (format: HHmmss)
 * - <time_len>: Program duration in seconds
 *
 * The parser converts this format to the standard Program structure used by the app,
 * which is compatible with the existing XMLTV format.
 */
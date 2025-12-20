package com.github.cgang.myiptv.smil

/**
 * Exception thrown when parsing a SMIL URL fails
 */
class SmilUrlParseException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
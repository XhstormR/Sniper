package com.xhstormr.app

import javafx.beans.property.ReadOnlyObjectWrapper

class ReadOnlyHttpResponse(
    payload: String,
    statusCode: Int,
    contentLength: Long,
    val url: String
) {
    val payloadProperty = ReadOnlyObjectWrapper(payload)
    val statusCodeProperty = ReadOnlyObjectWrapper(statusCode)
    val contentLengthProperty = ReadOnlyObjectWrapper(contentLength)

    val payload: String
        get() = payloadProperty.get()

    val statusCode: Int
        get() = statusCodeProperty.get()

    val contentLength: Long
        get() = contentLengthProperty.get()
}

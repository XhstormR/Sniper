package com.xhstormr.app

import javafx.beans.property.SimpleObjectProperty

class SimpleHttpResponse(
    payload: String,
    statusCode: Int,
    contentLength: Long,
    val url: String
) {
    val payloadProperty = SimpleObjectProperty(payload)
    val statusCodeProperty = SimpleObjectProperty(statusCode)
    val contentLengthProperty = SimpleObjectProperty(contentLength)

    var payload: String
        get() = payloadProperty.get()
        set(value) = payloadProperty.set(value)

    var statusCode: Int
        get() = statusCodeProperty.get()
        set(value) = statusCodeProperty.set(value)

    var contentLength: Long
        get() = contentLengthProperty.get()
        set(value) = contentLengthProperty.set(value)
}

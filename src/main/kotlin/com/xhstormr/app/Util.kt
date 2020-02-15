package com.xhstormr.app

/**
 * 内联：获得带泛型的 Class，eg: Class<X<Y>>
 */
inline fun <reified T> clazz() = T::class.java

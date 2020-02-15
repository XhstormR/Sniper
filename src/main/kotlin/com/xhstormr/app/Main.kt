package com.xhstormr.app

import java.util.logging.LogManager
import javafx.application.Application

fun main(args: Array<String>) {
    LogManager.getLogManager().reset()

    Application.launch(clazz<App>(), *args)
}

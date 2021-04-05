package com.xhstormr.app

import javafx.application.Application
import java.util.logging.LogManager

fun main(args: Array<String>) {
    LogManager.getLogManager().reset()

    Application.launch(clazz<App>(), *args)
}

package com.xhstormr.app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class App : Application() {
    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(ClassLoader.getSystemResource("views/scene.fxml"))
        val image = Image(ClassLoader.getSystemResourceAsStream("icons/icon.jpg"))
        val scene = Scene(root)
        stage.title = "Sniper"
        stage.scene = scene
        stage.icons.add(image)
        stage.show()
    }
}

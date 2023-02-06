package com.example.imageeditor

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.stage.Screen
import javafx.stage.Stage
import org.opencv.core.Core

class Window {
    private var primaryScreenBounds = Screen.getPrimary().visualBounds
    private var root = AnchorPane()
    private val width = primaryScreenBounds.width
    private val height = primaryScreenBounds.height - 20
    private var scene = Scene(root, width, height)
    fun start(): Scene {
        root.style = "-fx-background-color: #2b2b2b;"
        addButtons()
        setStartEndNodes()

        return scene
    }

    private fun setStartEndNodes() {
        val start = ImgNode()
        val end = EndNode()

        start.layoutX = 50.0
        start.layoutY = 50.0
        end.layoutX = primaryScreenBounds.width - 250.0
        end.layoutY = primaryScreenBounds.height / 2

        root.children.add(start)
        root.children.add(end)
    }

    private fun addButtons(){
        val hbox = HBox()
        hbox.style = "-fx-background-color: #3c3f41;"
        hbox.prefWidth = primaryScreenBounds.width
        hbox.prefHeight = 55.0
        val buttonsName = arrayOf("Float", "Int", "String", "Image", "Add Text", "Grey", "Brightness", "Sepia", "Invert", "Blur", "Move", "Scale", "Rotate", "Add Image", "Merge")
        for (buttonTitle in buttonsName) {
            val button = Button(buttonTitle)
            button.style = "-fx-background-color: #3c3f41; -fx-font-size: 20px; -fx-font-family: Serif; -fx-text-fill: #afb1b3"
            button.prefWidth = 120.0
            button.prefHeight = 53.0
            button.onAction = EventHandler {
                val node = addNode(buttonTitle)
                node.layoutX += 50
                node.layoutY += 50
                root.children.add(node)
            }
            hbox.children.add(button)
        }

        hbox.layoutX = 0.0
        hbox.layoutY = primaryScreenBounds.height - 75.0
        root.children.add(hbox)

    }

    private fun addNode(str: String): DraggableNode {
        return when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
            "Image" -> ImgNode()
            "Add Text" -> AddTextPercentNode()
            "Grey" -> GreyNode()
            "Brightness" -> BrightnessNode()
            "Sepia" -> SepiaNode()
            "Invert" -> InvertNode()
            "Blur" -> GaussNode()
            "Move" -> TMovePercentNode()
            "Scale" -> TScalePercentNode()
            "Rotate" -> TRotateNode()
            "Merge" -> MergeNode()
            "Add Image" -> ImgNode()
            else -> IntNode()
        }
    }
}

class ImageEditor : Application() {
    override fun start(primaryStage: Stage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        primaryStage.scene = Window().start()
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ImageEditor::class.java)
        }
    }
}
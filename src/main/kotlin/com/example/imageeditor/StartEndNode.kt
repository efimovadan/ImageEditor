package com.example.imageeditor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException


class ImgNode : ImageNode() {
    override val nodeType: NodeTypes = NodeTypes.IMAGE

    @FXML
    var openButton: Button? = null

    private var imageMat: Mat? = null
    private var path: String? = null

    override fun getValue(): Mat? {
        return imageMat
    }

    fun getImage() {
        imageMat = Imgcodecs.imread(path)
        updateNode()
        imageView!!.isVisible = true
        outputLink?.kickAction()
    }

    override fun addInit() {
        openButton!!.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("PNG", "*.png"), FileChooser.ExtensionFilter("JPG", "*.jpg"))
            fileChooser.title = "Open Image"
            val file = fileChooser.showOpenDialog(scene.window)
            if (file != null) {
                path = file.absolutePath
                getImage()
            }
        }
    }

    override fun toData(): NodeData {
        val data = super.toData()
        data.data = path
        return data
    }

    override fun fromData(nodeData: NodeData) {
        super.fromData(nodeData)
        path = nodeData.data
        getImage()
    }

    init {
        init("StartNode.fxml")
    }
}

class EndNode : ImageNode() {
    @FXML
    var saveButton: Button? = null

    override fun getValue(): Mat? {
        return nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
    }

    override fun addInit() {
        rootPane!!.onDragDetected = null

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        saveButton!!.onAction = EventHandler {
            val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return@EventHandler

            val fileChooser = FileChooser()
            fileChooser.title = "Save Image"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("PNG", "*.png"), FileChooser.ExtensionFilter("JPG", "*.jpg"))
            val dir = fileChooser.showSaveDialog(scene.window)
            if (dir != null) {
                try {
                    Imgcodecs.imwrite(dir.absolutePath, mat)
                } catch (e: IOException) {
                    println(e)
                }
            }
        }
    }

    override fun updateNode() {
        goodNodes()
        val v = getValue()
        if (v != null) {
            imageView!!.isVisible = true
            imageView!!.image = Transfer.matToImage(v)
            saveButton!!.textFill = Paint.valueOf(Colors.ORANGE)
        } else {
            saveButton!!.textFill = Paint.valueOf(Colors.PURPLE)
        }
    }

    init {
        init("EndNode.fxml")
    }
}
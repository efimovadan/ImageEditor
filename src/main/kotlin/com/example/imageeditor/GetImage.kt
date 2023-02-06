package com.example.imageeditor

import javafx.scene.image.Image
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.io.ByteArrayInputStream

class Transfer {
    companion object {
        fun matToImage(mat: Mat): Image {
            val buffer = MatOfByte()
            Imgcodecs.imencode(".png", mat, buffer)

            return Image(ByteArrayInputStream(buffer.toArray()))
        }
    }
}
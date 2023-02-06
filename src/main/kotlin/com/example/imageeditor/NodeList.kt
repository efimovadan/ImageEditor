package com.example.imageeditor

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

abstract class InputNode : DraggableNode() {
    @FXML
    var valueField: TextField? = null

    init {
        init("InputNode.fxml")
    }

    override fun updateNode() {
        if (getValue() != null) {
            (outputLinkHandle!!.children.find { it is Circle } as Circle).fill = Paint.valueOf(Colors.ORANGE)
        } else {
            (outputLinkHandle!!.children.find { it is Circle } as Circle).fill = Paint.valueOf(Colors.PURPLE)
        }
    }

    override fun toData(): NodeData {
        val data = super.toData()
        data.data = valueField!!.text
        return data
    }

    override fun fromData(nodeData: NodeData) {
        super.fromData(nodeData)
        valueField!!.text = nodeData.data
    }
}

class IntNode : InputNode() {
    override val nodeType: NodeTypes = NodeTypes.INT
    override fun addInit() {
        valueField!!.text = ""
        titleBar!!.text = "Int"

        valueField!!.textProperty().addListener { _, _, _ ->
            updateNode()
            outputLink?.kickAction()
        }
    }

    override fun getValue(): Int? {
        return valueField!!.text.toIntOrNull()
    }

}

class FloatNode : InputNode() {

    override val nodeType: NodeTypes = NodeTypes.FLOAT

    override fun addInit() {
        valueField!!.text = ""
        titleBar!!.text = "Float"

        valueField!!.textProperty().addListener { _, _, _ ->
            updateNode()
            outputLink?.kickAction()
        }
    }

    override fun getValue(): Float? {
        return valueField!!.text.toFloatOrNull()
    }
}

class StringNode : InputNode() {
    override val nodeType: NodeTypes = NodeTypes.STRING
    override fun addInit() {
        valueField!!.text = ""
        titleBar!!.text = "String"

        valueField!!.textProperty().addListener { _, _, _ ->
            updateNode()
            outputLink?.kickAction()
        }
    }

    override fun getValue(): String {
        return valueField!!.text
    }
}

abstract class ImageNode : DraggableNode() {
    override val nodeType: NodeTypes = NodeTypes.IMAGE

    @FXML
    var firstLink: AnchorPane? = null

    @FXML
    var imageView: ImageView? = null
    override fun updateNode() {
        goodNodes()
        val v = getValue() as Mat?
        if (v != null) {
            imageView!!.isVisible = true
            imageView!!.image = Transfer.matToImage(v)
            (outputLinkHandle!!.children.find { it is Circle } as Circle).fill = Paint.valueOf(Colors.ORANGE)
        } else {
            (outputLinkHandle!!.children.find { it is Circle } as Circle).fill = Paint.valueOf(Colors.PURPLE)
        }
    }
}

class SepiaNode : ImageNode() {

    override fun addInit() {
        titleBar!!.text = "Sepia"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)

        val colMat = Mat(3, 3, CvType.CV_64FC1)
        val row = 0
        val col = 0
        colMat.put(
            row, col, 0.272, 0.534, 0.131, 0.349, 0.686, 0.168, 0.393, 0.769, 0.189
        )

        val mat2 = Mat()
        mat.copyTo(mat2)
        Core.transform(mat, mat2, colMat)

        goodNodes()
        return mat2
    }

    init {
        init("OneLinkNode.fxml")
    }

}

class InvertNode : ImageNode() {
    override fun addInit() {
        titleBar!!.text = "Invert"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)

        val mat2 = Mat()
        mat.copyTo(mat2)
        Core.bitwise_not(mat, mat2)

        goodNodes()
        return mat2
    }

    init {
        init("OneLinkNode.fxml")
    }

}

class GreyNode : ImageNode() {
    override fun addInit() {
        titleBar!!.text = "Grey"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)

        val mat2 = Mat()
        mat.copyTo(mat2)
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2GRAY)

        val mat3 = Mat()

        Core.merge(List(3) { mat2 }, mat3)

        goodNodes()
        return mat3
    }

    init {
        init("OneLinkNode.fxml")
    }

}

class BrightnessNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Bright"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "float"
    }


    override fun getValue(): Mat? {
        fun saturate(`val`: Double): Byte {
            var iVal = `val`.roundToInt()
            iVal = if (iVal > 255) 255 else if (iVal < 0) 0 else iVal
            return iVal.toByte()
        }

        val image = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val beta = nodes[Link.SECOND]!!.second?.getValue() as Float? ?: return errorNode(Link.SECOND)
        val alpha = 1.0

        val newImage = Mat()
        image.copyTo(newImage)

        val imageData = ByteArray(((image.total() * image.channels()).toInt()))
        image.get(0, 0, imageData)
        val newImageData = ByteArray((newImage.total() * newImage.channels()).toInt())
        for (y in 0 until image.rows()) {
            for (x in 0 until image.cols()) {
                for (c in 0 until image.channels()) {
                    var pixelValue = imageData[(y * image.cols() + x) * image.channels() + c].toDouble()
                    pixelValue = if (pixelValue < 0) pixelValue + 256 else pixelValue
                    newImageData[(y * image.cols() + x) * image.channels() + c] = saturate(alpha * pixelValue + beta)
                }
            }
        }
        newImage.put(0, 0, newImageData)

        goodNodes()
        return newImage
    }

    init {
        init("TwoLinksNode.fxml")
    }

}

class GaussNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Blur"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "int"
    }


    override fun getValue(): Mat? {
        val image = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        var kernelSize = nodes[Link.SECOND]!!.second?.getValue() as Int? ?: return errorNode(Link.SECOND)

        kernelSize = kernelSize * 2 + 1
        if (kernelSize <= 0 || kernelSize > 100)
            return null


        val newImage = Mat()
        image.copyTo(newImage)

        Imgproc.GaussianBlur(image, newImage, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)

        goodNodes()
        return newImage
    }

    init {
        init("TwoLinksNode.fxml")
    }

}

class TRotateNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Rotate"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_deg"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val deg = nodes[Link.SECOND]!!.second?.getValue() as Float? ?: return errorNode(Link.SECOND)

        val mat2 = Mat()
        mat.copyTo(mat2)

        val rotMat = Imgproc.getRotationMatrix2D(Point(mat.cols() / 2.0, mat.rows() / 2.0), deg.toDouble(), 1.0)

        Imgproc.warpAffine(mat, mat2, rotMat, Size(mat.cols().toDouble(), mat.rows().toDouble()))

        goodNodes()
        return mat2
    }

    init {
        init("TwoLinksNode.fxml")
    }
}
class TScalePercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "Scale"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes[Link.THIRD] = Triple(thirdLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "f_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val px = nodes[Link.SECOND]!!.second?.getValue() as Float? ?: return errorNode(Link.SECOND)
        val py = nodes[Link.THIRD]!!.second?.getValue() as Float? ?: return errorNode(Link.THIRD)

        val x = mat.cols() * px / 100
        val y = mat.rows() * py / 100

        if (x <= 0 || y <= 0)
            return null

        val mat2 = Mat()
        mat.copyTo(mat2)
        Imgproc.resize(mat, mat2, Size(x.toDouble(), y.toDouble()))

        goodNodes()
        return mat2
    }

    init {
        init("ThreeLinksNode.fxml")
    }

}


class TMovePercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "Move"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes[Link.THIRD] = Triple(thirdLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "f_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val px = nodes[Link.SECOND]!!.second?.getValue() as Float? ?: return errorNode(Link.SECOND)
        val py = nodes[Link.THIRD]!!.second?.getValue() as Float? ?: return errorNode(Link.THIRD)

        val mat2 = Mat()
        mat.copyTo(mat2)

        val moveMat = Mat(2, 3, CvType.CV_64FC1)
        val row = 0
        val col = 0
        moveMat.put(
            row, col, 1.0, 0.0, (mat.cols() * px / 100.0), 0.0, 1.0, (mat.rows() * py / 100.0)
        )

        Imgproc.warpAffine(mat, mat2, moveMat, Size(mat.cols().toDouble(), mat.rows().toDouble()))

        goodNodes()
        return mat2
    }

    init {
        init("ThreeLinksNode.fxml")
    }

}



class MergeNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null

    @FXML
    var fourthLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Merge"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.IMAGE)
        nodes[Link.THIRD] = Triple(thirdLink!!, null, NodeTypes.INT)
        nodes[Link.FOURTH] = Triple(fourthLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "Img1"
        (secondLink!!.children.find { it is Label } as Label).text = "Img2"
        (thirdLink!!.children.find { it is Label } as Label).text = "x"
        (fourthLink!!.children.find { it is Label } as Label).text = "y"
    }

    override fun getValue(): Mat? {
        val image = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val image2 = nodes[Link.SECOND]!!.second?.getValue() as Mat? ?: return errorNode(Link.SECOND)
        val sx = nodes[Link.THIRD]!!.second?.getValue() as Int? ?: return errorNode(Link.THIRD)
        val sy = nodes[Link.FOURTH]!!.second?.getValue() as Int? ?: return errorNode(Link.FOURTH)

        val newImage = Mat()
        image.copyTo(newImage)

        val imageData = ByteArray(((image.total() * image.channels()).toInt()))
        image.get(0, 0, imageData)
        val imageData2 = ByteArray(((image.total() * image.channels()).toInt()))
        image2.get(0, 0, imageData2)

        for (y in 0 until image2.rows()) {
            for (x in 0 until image2.cols()) {
                for (c in 0 until image2.channels()) {
                    if (0 <= sy + y && sy + y < image.rows() && 0 <= sx + x && sx + x < image.cols()) {
                        imageData[((sy + y) * image.cols() + sx + x) * image.channels() + c] =
                            imageData2[(y * image2.cols() + x) * image2.channels() + c]
                    }
                }
            }
        }
        newImage.put(0, 0, imageData)

        goodNodes()
        return newImage
    }

    init {
        init("FourLinksNode.fxml")
    }
}

class AddTextPercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null

    @FXML
    var fourthLink: AnchorPane? = null

    @FXML
    var fifthLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "AddText"

        nodes[Link.FIRST] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes[Link.SECOND] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes[Link.THIRD] = Triple(thirdLink!!, null, NodeTypes.FLOAT)
        nodes[Link.FOURTH] = Triple(fourthLink!!, null, NodeTypes.STRING)
        nodes[Link.FIFTH] = Triple(fifthLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "fl_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "fl_y"
        (fourthLink!!.children.find { it is Label } as Label).text = "str"
        (fifthLink!!.children.find { it is Label } as Label).text = "fl_scale"
    }

    override fun getValue(): Mat? {
        val mat = nodes[Link.FIRST]!!.second?.getValue() as Mat? ?: return errorNode(Link.FIRST)
        val px = nodes[Link.SECOND]!!.second?.getValue() as Float? ?: return errorNode(Link.SECOND)
        val py = nodes[Link.THIRD]!!.second?.getValue() as Float? ?: return errorNode(Link.THIRD)
        val str = nodes[Link.FOURTH]!!.second?.getValue() as String? ?: return errorNode(Link.FOURTH)
        val scale = nodes[Link.FIFTH]!!.second?.getValue() as Float? ?: return errorNode(Link.FIFTH)

        val mat2 = Mat()
        mat.copyTo(mat2)

        Imgproc.putText(
            mat2,
            str,
            Point(mat.cols() * px / 100.0, mat.rows() * py / 100.0),
            0,
            scale.toDouble(),
            Scalar(255.0, 255.0, 255.0),
            2
        )

        goodNodes()
        return mat2
    }

    init {
        init("FiveLinksNode.fxml")
    }
}

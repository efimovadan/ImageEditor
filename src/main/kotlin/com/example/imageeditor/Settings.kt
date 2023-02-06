package com.example.imageeditor

enum class NodeTypes {
    INT,
    FLOAT,
    STRING,
    IMAGE,
    NONE
}

class Colors {
    companion object {
        const val ORANGE = "#bc5a2b"
        const val PURPLE = "#b99bf8"
    }
}

class Link {
    companion object {
        const val FIRST = "firstLink"
        const val SECOND = "secondLink"
        const val THIRD = "thirdLink"
        const val FOURTH = "fourthLink"
        const val FIFTH = "fifthLink"
    }
}

package com.frc1678.match_collection

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min
import kotlin.math.sqrt

class HexagonShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density

    ): Outline {
        return Outline.Generic(
            path = drawCustomHexagonPath(size)
        )
    }

}

// Hexagon shape for Teleop
fun drawCustomHexagonPath(size: Size): Path {
    return Path().apply {
        val radius = min(size.width / 2f, size.height / 2f)
        val triangleHeight = (sqrt(3.0) * radius / 2)
        val centerX = size.width / 2
        val centerY = size.height / 2
        moveTo(x = centerX, y = centerY + radius)
        lineTo(x = (centerX - triangleHeight).toFloat(), y = centerY + radius / 2)
        lineTo(x = (centerX - triangleHeight).toFloat(), y = centerY - radius / 2)
        lineTo(x = centerX, y = centerY - radius)
        lineTo(x = (centerX + triangleHeight).toFloat(), y = centerY - radius / 2)
        lineTo(x = (centerX + triangleHeight).toFloat(), y = centerY + radius / 2)
        close()
    }
}

// Individual triangle shape for Auto
class TriangleShape(private val index: Int) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(
        Path().apply {

            val radius = min(size.width / 2.8f, size.height / 2.8f)
            val triangleHeight = (sqrt(3.0) * radius / 2)
            val centerX = size.width / 2
            val centerY = size.height / 2

            val coords = listOf(
                    Pair((centerX - triangleHeight).toFloat(), centerY - radius / 2),
                    Pair(centerX - triangleHeight.toFloat(), centerY + radius / 2),
                    Pair(centerX, centerY + radius),
                    Pair((centerX + triangleHeight).toFloat(), centerY + radius / 2),
                    Pair((centerX + triangleHeight).toFloat(), centerY - radius / 2),
                    Pair(centerX, centerY - radius),
                    Pair((centerX - triangleHeight).toFloat(), centerY - radius / 2),
                )
            moveTo(x = centerX, y = centerY)
            lineTo(x = coords[index].first, coords[index].second)
            lineTo(x = coords[index + 1].first, coords[index + 1].second)
            close()
        }
    )
}


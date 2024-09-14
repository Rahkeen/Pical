package dev.supergooey.pical.ui.theme

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.max

fun RoundedPolygon.getBounds() = calculateBounds().let { Rect(it[0], it[1], it[2], it[3]) }

class RoundedPolygonShape(
  private val polygon: RoundedPolygon,
  private var matrix: Matrix = Matrix()
) : Shape {
  private var path = Path()
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    path.rewind()
    path = polygon.toPath().asComposePath()
    matrix.reset()
    val bounds = polygon.getBounds()
    val maxDimension = max(bounds.width, bounds.height)
    matrix.scale(size.width / maxDimension, size.height / maxDimension)
    matrix.translate(-bounds.left, -bounds.top)

    path.transform(matrix)
    return Outline.Generic(path)
  }
}

class MorphPolygonShape(
  private val morph: Morph,
  private val percentage: Float
) : Shape {

  private val matrix = Matrix()
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    matrix.scale(size.width / 2f, size.height / 2f)
    matrix.translate(1f, 1f)
    val path = morph.toPath(percentage).asComposePath()
    path.transform(matrix)
    return Outline.Generic(path)
  }
}

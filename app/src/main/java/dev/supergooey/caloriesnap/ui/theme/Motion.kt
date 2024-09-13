package dev.supergooey.caloriesnap.ui.theme

import android.graphics.Path
import android.view.animation.PathInterpolator
import androidx.compose.animation.core.Easing

const val DURATION_EXTRA_LONG = 800

private val emphasizedPath = Path().apply {
  moveTo(0f, 0f)
  cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.04f)
  cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)
}

val emphasized = PathInterpolator(emphasizedPath)

val EmphasizedEasing = Easing { emphasized.getInterpolation(it) }
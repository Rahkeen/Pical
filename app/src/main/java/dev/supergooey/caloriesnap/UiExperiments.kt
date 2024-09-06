package dev.supergooey.caloriesnap

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.supergooey.caloriesnap.ui.theme.CoolMagenta
import dev.supergooey.caloriesnap.ui.theme.CoolPink
import dev.supergooey.caloriesnap.ui.theme.CoolPurple
import org.intellij.lang.annotations.Language
import kotlin.math.cos
import kotlin.math.sin

@Preview
@Composable
private fun BlobbyBackground() {
  var time by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(Unit) {
    do {
      withFrameMillis {
        time += 0.01f
      }
    } while (true)
  }
  Box(modifier = Modifier
    .fillMaxSize()
    .background(Color.White)) {
    Canvas(modifier = Modifier
      .blur(32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
      .fillMaxSize()) {
      val r1 = size.minDimension / 3f
      val x1 = size.center.x + (sin(time) * 100)
      val y1 = size.center.y + (cos(time) * 50)

      val r2 = size.minDimension / 3f
      val x2 = size.center.x + (sin(time*2f) * 120)
      val y2 = size.center.y - (cos(time*2f) * 140)

      val r3 = size.minDimension / 3f
      val x3 = size.center.x - (sin(time*1.5f) * 160)
      val y3 = size.center.y - (cos(time*1.5f) * 200)
      drawCircle(
        radius = r1,
        center = Offset(x1, y1),
        color = CoolPink.copy(alpha = 0.5f)
      )
      drawCircle(
        radius = r2,
        center = Offset(x2, y2),
        color = CoolMagenta.copy(alpha = 0.5f)
      )
      drawCircle(
        radius = r3,
        center = Offset(x3, y3),
        color = CoolPurple.copy(alpha = 0.5f)
      )
    }
  }
}

@Language("AGSL")
val magnifyShader = """
uniform shader image;
uniform float time;
uniform float2 size;
uniform float2 glass;
uniform float glassRadius;

float mapRange(float value, float inMin, float inMax, float outMin, float outMax) {
    return ((value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin);
}

float easeInQuart(float x) {
    return x * x * x * x;
}

float sdCircle(float2 uv, float2 center, float radius) {
    float d = distance(uv, center);
    return d - radius;
}

// simple barrel distortion
float2 distortion(float2 uv, float2 center, float amount) {
    float2 p = uv - center;
    return p * (1.0 - amount * dot(p, p)) + center;
}

// translate, scale, reset
float2 zoomed(float2 uv, float2 center, float amount) {
    return (uv - center) / amount + center;
}

float glow(float d, float radius, float spread) {
    return pow(radius / d, spread);
}

half4 main(float2 coord) {
    // normalized coords
    float2 uv = coord / size;
    half4 color = image.eval(coord);
    float x = 0.5 - sin(time*2) * 0.2;
    float y = 0.5 + cos(time*2) * 0.2;
    float2 center = float2(x,y);
    float d = sdCircle(uv, center, glassRadius);
    
    if (d <= 0.0) {
        uv = distortion(uv, center, -0.5);
        uv = zoomed(uv, center, 2.0);
        float2 newCoord = uv * size;
        return image.eval(newCoord);
    } else if (d > 0.0 && d <= 0.2 && glassRadius > 0.0) {
        // shadow
        float progress = mapRange(d, 0.0, 0.2, 1.0, 0.0);
        progress = easeInQuart(progress);
        float shadowAlpha = mapRange(progress, 1.0, 0.0, 0.5, 0.0);
        color = mix(color, half4(half3(0.0), 1.0), shadowAlpha);
    }
    return color;
}
""".trimIndent()

@Preview
@Composable
private fun MagnifyTest() {
  var time by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(Unit) {
    do {
      withFrameMillis {
        time += 0.01f
      }
    } while (true)
  }

  val shader = remember { RuntimeShader(magnifyShader) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(color = Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Image(
      modifier = Modifier
        .graphicsLayer {
          clip = true
          shader.setFloatUniform(
            "time",
            time
          )
          shader.setFloatUniform(
            "size",
            size.width,
            size.height
          )
          shader.setFloatUniform(
            "glass",
            0.5f,
            0.3f
          )
          shader.setFloatUniform(
            "glassRadius",
            0.3f,
          )
          renderEffect = RenderEffect.createRuntimeShaderEffect(
            shader,
            "image"
          ).asComposeRenderEffect()
        }
        .size(300.dp)
        .clip(RoundedCornerShape(16.dp)),
      painter = painterResource(R.drawable.bibimbap),
      contentScale = ContentScale.Crop,
      contentDescription = "Bibimbap"
    )
  }
}
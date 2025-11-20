package com.tuforums.preview.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.tuforums.preview.core.GenerationProgress
import com.tuforums.preview.core.PreviewGenerator
import com.tuforums.preview.image.DefaultImageProcessor
import com.tuforums.preview.image.ImageProcessor
import kotlinx.coroutines.launch
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Suppress("EXPERIMENTAL_API_USAGE")
fun main() = org.jetbrains.compose.desktop.application.dsl.ApplicationScope {
  Window(
    title = "Preview Generator",
    onCloseRequest = ::exitApplication
  ) {
    MaterialTheme {
      PreviewGeneratorUI()
    }
  }
}

@Composable
fun PreviewGeneratorUI() {
  var playerId by remember { mutableStateOf("693") }
  var backgroundImagePath by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var statusText by remember { mutableStateOf("Ready") }
  var previewImage by remember { mutableStateOf<ImageBitmap?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  
  val imageProcessor: ImageProcessor = remember { DefaultImageProcessor() }
  val previewGenerator = remember { PreviewGenerator(imageProcessor = imageProcessor) }
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Preview Generator",
      style = MaterialTheme.typography.h4,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    // Player ID input
    OutlinedTextField(
      value = playerId,
      onValueChange = { playerId = it },
      label = { Text("Player ID") },
      modifier = Modifier.fillMaxWidth(0.8f),
      enabled = !isLoading
    )

    // Background image path input (optional)
    OutlinedTextField(
      value = backgroundImagePath,
      onValueChange = { backgroundImagePath = it },
      label = { Text("Background Image Path (optional)") },
      modifier = Modifier.fillMaxWidth(0.8f),
      enabled = !isLoading
    )

    // Generate button
    Button(
      onClick = {
        val id = playerId.toIntOrNull()
        if (id == null) {
          errorMessage = "Invalid player ID"
          return@Button
        }
        
        errorMessage = null
        isLoading = true
        statusText = "Generating..."
        previewImage = null

        scope.launch {
          try {
            val result = previewGenerator.generatePreview(
              playerId = id,
              backgroundImageUrl = backgroundImagePath.takeIf { it.isNotBlank() }
            )

            result.onSuccess { imageData ->
              statusText = "Generated successfully!"
              
              // Convert ImageData to ImageBitmap
              try {
                previewImage = imageData.toComposeImageBitmap()
              } catch (e: Exception) {
                errorMessage = "Failed to display image: ${e.message}"
              }
            }.onFailure { exception ->
              errorMessage = "Generation failed: ${exception.message}"
              statusText = "Generation failed"
            }
          } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            statusText = "Error occurred"
          } finally {
            isLoading = false
          }
        }
      },
      enabled = !isLoading && playerId.isNotBlank(),
      modifier = Modifier.height(48.dp)
    ) {
      Icon(Icons.Default.Download, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text(if (isLoading) "Generating..." else "Generate Preview")
    }

    // Status text
    Text(
      text = statusText,
      style = MaterialTheme.typography.body2,
      color = if (errorMessage != null) MaterialTheme.colors.error else MaterialTheme.colors.onBackground
    )

    // Error message
    errorMessage?.let { error ->
      Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
      ) {
        Text(
          text = error,
          modifier = Modifier.padding(16.dp),
          color = MaterialTheme.colors.error
        )
      }
    }

    // Preview image
    previewImage?.let { image ->
      Card(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .weight(1f),
        elevation = 4.dp
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
          androidx.compose.foundation.Image(
            bitmap = image,
            contentDescription = "Generated Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }
      }

      // Save button
      Button(
        onClick = {
          val file = File("preview_output.png")
          try {
            val bufferedImage = image.toBufferedImage()
            ImageIO.write(bufferedImage, "png", file)
            statusText = "Saved to ${file.absolutePath}"
          } catch (e: Exception) {
            errorMessage = "Failed to save: ${e.message}"
          }
        }
      ) {
        Text("Save Image")
      }
    }

    Spacer(Modifier.height(16.dp))
  }
}

// Helper functions to convert between ImageData and ImageBitmap
fun com.tuforums.preview.image.ImageData.toComposeImageBitmap(): ImageBitmap {
  return try {
    val bufferedImage = ImageIO.read(this.pixels.inputStream())
    bufferedImage.toComposeImageBitmap()
  } catch (e: Exception) {
    // Fallback: create empty bitmap
    ImageBitmap(this.width, this.height)
  }
}

fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
  val width = this.width
  val height = this.height
  val imageBitmap = ImageBitmap(width, height)
  val pixelArray = IntArray(width * height)
  this.getRGB(0, 0, width, height, pixelArray, 0, width)
  // Convert ARGB to RGBA and write pixels
  val rgbaArray = ByteArray(width * height * 4)
  for (i in pixelArray.indices) {
    val pixel = pixelArray[i]
    rgbaArray[i * 4] = ((pixel shr 16) and 0xFF).toByte() // R
    rgbaArray[i * 4 + 1] = ((pixel shr 8) and 0xFF).toByte() // G
    rgbaArray[i * 4 + 2] = (pixel and 0xFF).toByte() // B
    rgbaArray[i * 4 + 3] = ((pixel shr 24) and 0xFF).toByte() // A
  }
  imageBitmap.writePixels(rgbaArray)
  return imageBitmap
}

fun ImageBitmap.toBufferedImage(): BufferedImage {
  val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  val pixelArray = IntArray(width * height)
  val rgbaArray = ByteArray(width * height * 4)
  this.readPixels(rgbaArray, 0, 0, width, height)
  // Convert RGBA to ARGB
  for (i in pixelArray.indices) {
    val r = rgbaArray[i * 4].toInt() and 0xFF
    val g = rgbaArray[i * 4 + 1].toInt() and 0xFF
    val b = rgbaArray[i * 4 + 2].toInt() and 0xFF
    val a = rgbaArray[i * 4 + 3].toInt() and 0xFF
    pixelArray[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
  }
  bufferedImage.setRGB(0, 0, width, height, pixelArray, 0, width)
  return bufferedImage
}

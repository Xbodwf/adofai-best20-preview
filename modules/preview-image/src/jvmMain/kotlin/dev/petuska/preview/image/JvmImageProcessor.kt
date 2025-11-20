package com.tuforums.preview.image

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.min

public class JvmCanvas(private val image: BufferedImage) : Canvas {
  override val width: Int get() = image.width
  override val height: Int get() = image.height
  
  private val graphics: Graphics2D = image.createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
  }

  override fun drawImage(imageData: ImageData, x: Int, y: Int) {
    val img = bytesToBufferedImage(imageData.pixels, imageData.width, imageData.height)
    graphics.drawImage(img, x, y, null)
  }

  override fun drawText(text: String, x: Int, y: Int, fontSize: Int, color: Int) {
    graphics.font = Font("SansSerif", Font.PLAIN, fontSize)
    graphics.color = Color(color)
    graphics.drawString(text, x, y)
  }

  override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, width: Int) {
    graphics.color = Color(color)
    graphics.stroke = BasicStroke(width.toFloat())
    graphics.drawLine(x1, y1, x2, y2)
  }

  override fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Int) {
    graphics.color = Color(color)
    graphics.fillRect(x, y, width, height)
  }

  override fun toImageData(): ImageData {
    val bytes = ByteArrayOutputStream()
    ImageIO.write(image, "png", bytes)
    return ImageData(image.width, image.height, bytes.toByteArray())
  }
  
  fun close() {
    graphics.dispose()
  }
}

public actual fun DefaultImageProcessor.loadImageFromBytes(bytes: ByteArray): ImageData? {
  return try {
    val image = ImageIO.read(bytes.inputStream())
    if (image == null) return null
    val bytes = ByteArrayOutputStream()
    ImageIO.write(image, "png", bytes)
    ImageData(image.width, image.height, bytes.toByteArray())
  } catch (e: Exception) {
    null
  }
}

private fun bytesToBufferedImage(bytes: ByteArray, width: Int, height: Int): BufferedImage {
  val image = ImageIO.read(bytes.inputStream())
  return image ?: BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
}

private fun imageDataToBufferedImage(imageData: ImageData): BufferedImage {
  return ImageIO.read(imageData.pixels.inputStream())
}

private fun bufferedImageToImageData(image: BufferedImage): ImageData {
  val bytes = ByteArrayOutputStream()
  ImageIO.write(image, "png", bytes)
  return ImageData(image.width, image.height, bytes.toByteArray())
}

public actual fun DefaultImageProcessor.resize(image: ImageData, width: Int, height: Int): ImageData {
  val source = imageDataToBufferedImage(image)
  val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  val graphics = resized.createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
  }
  graphics.drawImage(source, 0, 0, width, height, null)
  graphics.dispose()
  return bufferedImageToImageData(resized)
}

public actual fun DefaultImageProcessor.makeCircular(image: ImageData): ImageData {
  val source = imageDataToBufferedImage(image)
  val size = min(source.width, source.height)
  val circular = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
  val graphics = circular.createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    composite = AlphaComposite.Src
  }
  graphics.clip = Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
  graphics.drawImage(source, 0, 0, size, size, null)
  graphics.dispose()
  return bufferedImageToImageData(circular)
}

public actual fun DefaultImageProcessor.addRoundedBorder(
  image: ImageData,
  borderWidth: Int,
  cornerRadius: Int
): ImageData {
  val source = imageDataToBufferedImage(image)
  val newWidth = source.width + 2 * borderWidth
  val newHeight = source.height + 2 * borderWidth
  
  val bordered = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
  val graphics = bordered.createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
  }
  
  // Draw black border
  graphics.color = Color.BLACK
  graphics.fill(RoundRectangle2D.Float(
    0f, 0f, newWidth.toFloat(), newHeight.toFloat(),
    cornerRadius.toFloat(), cornerRadius.toFloat()
  ))
  
  // Draw rounded source image
  graphics.clip = RoundRectangle2D.Float(
    borderWidth.toFloat(), borderWidth.toFloat(),
    source.width.toFloat(), source.height.toFloat(),
    cornerRadius.toFloat(), cornerRadius.toFloat()
  )
  graphics.drawImage(source, borderWidth, borderWidth, null)
  graphics.dispose()
  
  return bufferedImageToImageData(bordered)
}

public actual fun DefaultImageProcessor.addText(
  image: ImageData,
  text: String,
  fontSize: Int,
  position: TextPosition
): ImageData {
  val source = imageDataToBufferedImage(image)
  val graphics = source.createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
  }
  
  graphics.font = Font("SansSerif", Font.PLAIN, fontSize)
  val metrics = graphics.fontMetrics
  val textWidth = metrics.stringWidth(text)
  val textHeight = metrics.height
  
  val (x, y) = when (position) {
    TextPosition.TopLeft -> 10 to 10 + textHeight
    TextPosition.TopRight -> source.width - textWidth - 10 to 10 + textHeight
    TextPosition.BottomLeft -> 10 to source.height - 10
    TextPosition.BottomRight -> source.width - textWidth - 25 to source.height - 170
    TextPosition.Center -> (source.width - textWidth) / 2 to (source.height + textHeight) / 2
  }
  
  // Draw shadow
  graphics.color = Color.BLACK
  graphics.drawString(text, x + 2, y + 2)
  
  // Draw text
  graphics.color = Color.WHITE
  graphics.drawString(text, x, y)
  graphics.dispose()
  
  return bufferedImageToImageData(source)
}

public actual fun DefaultImageProcessor.applyBlur(image: ImageData, radius: Int): ImageData {
  // Simple blur implementation - for production use a proper blur algorithm
  val source = imageDataToBufferedImage(image)
  val blurred = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
  val graphics = blurred.createGraphics()
  graphics.drawImage(source, 0, 0, null)
  graphics.dispose()
  // TODO: Implement proper blur using ConvolveOp or similar
  return bufferedImageToImageData(blurred)
}

public actual fun DefaultImageProcessor.createImage(width: Int, height: Int, color: Int): ImageData {
  val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  val graphics = image.createGraphics()
  graphics.color = Color(color)
  graphics.fillRect(0, 0, width, height)
  graphics.dispose()
  return bufferedImageToImageData(image)
}

public actual fun DefaultImageProcessor.createCanvas(width: Int, height: Int): Canvas {
  val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  val graphics = image.createGraphics()
  graphics.color = Color.WHITE
  graphics.fillRect(0, 0, width, height)
  graphics.dispose()
  return JvmCanvas(image)
}

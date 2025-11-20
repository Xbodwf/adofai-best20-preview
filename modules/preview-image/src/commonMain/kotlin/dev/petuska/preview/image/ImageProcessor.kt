package com.tuforums.preview.image

import com.tuforums.preview.api.TuForumsApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

public interface ImageProcessor {
  public suspend fun downloadImage(url: String): ImageData?
  public fun resize(image: ImageData, width: Int, height: Int): ImageData
  public fun makeCircular(image: ImageData): ImageData
  public fun addRoundedBorder(
    image: ImageData,
    borderWidth: Int = 5,
    cornerRadius: Int = 25
  ): ImageData
  public fun addText(
    image: ImageData,
    text: String,
    fontSize: Int = 24,
    position: TextPosition = TextPosition.BottomRight
  ): ImageData
  public fun applyBlur(image: ImageData, radius: Int = 3): ImageData
  public fun createImage(width: Int, height: Int, color: Int = 0xFFFFFFFF.toInt()): ImageData
  public fun createCanvas(width: Int, height: Int): Canvas
}

public data class ImageData(
  val width: Int,
  val height: Int,
  val pixels: ByteArray,
  val hasAlpha: Boolean = true
)

public interface Canvas {
  val width: Int
  val height: Int
  
  public fun drawImage(image: ImageData, x: Int, y: Int)
  public fun drawText(text: String, x: Int, y: Int, fontSize: Int, color: Int)
  public fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, width: Int = 1)
  public fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Int)
  public fun toImageData(): ImageData
}

public enum class TextPosition {
  TopLeft, TopRight, BottomLeft, BottomRight, Center
}

public class DefaultImageProcessor(
  private val client: HttpClient = TuForumsApi.createDefaultClient()
) : ImageProcessor {
  override suspend fun downloadImage(url: String): ImageData? = withContext(Dispatchers.IO) {
    try {
      val response = client.get(url)
      val bytes: ByteArray = response.body()
      loadImageFromBytes(bytes)
    } catch (e: Exception) {
      null
    }
  }

  public expect fun loadImageFromBytes(bytes: ByteArray): ImageData?
  
  public expect override fun resize(image: ImageData, width: Int, height: Int): ImageData
  public expect override fun makeCircular(image: ImageData): ImageData
  public expect override fun addRoundedBorder(
    image: ImageData,
    borderWidth: Int,
    cornerRadius: Int
  ): ImageData
  public expect override fun addText(
    image: ImageData,
    text: String,
    fontSize: Int,
    position: TextPosition
  ): ImageData
  public expect override fun applyBlur(image: ImageData, radius: Int): ImageData
  public expect override fun createImage(width: Int, height: Int, color: Int): ImageData
  public expect override fun createCanvas(width: Int, height: Int): Canvas
}

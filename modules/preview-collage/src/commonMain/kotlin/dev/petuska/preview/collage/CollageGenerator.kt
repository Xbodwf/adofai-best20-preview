package com.tuforums.preview.collage

import com.tuforums.preview.api.PassData
import com.tuforums.preview.api.PlayerData
import com.tuforums.preview.api.TuForumsApi
import com.tuforums.preview.image.ImageData
import com.tuforums.preview.image.ImageProcessor
import com.tuforums.preview.image.TextPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil
import kotlin.math.pow

public class CollageGenerator(
  private val imageProcessor: ImageProcessor,
  private val api: TuForumsApi,
  private val config: CollageConfig = CollageConfig()
) {
  public data class ProcessedImage(
    val image: ImageData,
    val index: Int,
    val x: Int,
    val y: Int,
    val text: String
  )

  public data class TopPass(
    val levelId: String,
    val scoreV2: Double,
    val accuracy: Double,
    val weightedScore: Double
  )

  public suspend fun getTopPasses(playerData: PlayerData): List<TopPass> {
    val seenLevelIds = mutableSetOf<String>()
    val topPasses = mutableListOf<TopPass>()
    var t = 0

    for (pass in playerData.passes.sortedByDescending { it.scoreV2.toDoubleOrNull() ?: 0.0 }) {
      if (pass.levelId in seenLevelIds) continue
      if (topPasses.size >= config.maxImages) break

      seenLevelIds.add(pass.levelId)
      val scoreV2 = pass.scoreV2.toDoubleOrNull() ?: 0.0
      val accuracy = pass.judgements.accuracy.toDoubleOrNull() ?: 0.0
      val weightedScore = 0.9.pow(t) * scoreV2

      topPasses.add(
        TopPass(
          levelId = pass.levelId,
          scoreV2 = scoreV2,
          accuracy = accuracy,
          weightedScore = weightedScore
        )
      )
      t++
    }

    return topPasses
  }

  public suspend fun processImages(topPasses: List<TopPass>): List<ProcessedImage> = coroutineScope {
    val borderedWidth = config.imageWidth + 2 * config.borderWidth
    val borderedHeight = config.imageHeight + 2 * config.borderWidth
    val rows = ceil(topPasses.size / 2.0).toInt()
    val startYPosition = 350

    topPasses.mapIndexed { index, pass ->
      async {
        try {
          val imageUrl = api.getLevelThumbnailUrl(pass.levelId)
          val imageData = imageProcessor.downloadImage(imageUrl) ?: return@async null

          val resized = imageProcessor.resize(imageData, config.imageWidth, config.imageHeight)
          
          val text = "XACC: ${String.format("%.2f", pass.accuracy * 100)}% / Score: ${String.format("%.2f", pass.scoreV2)}(+${String.format("%.2f", pass.weightedScore)})"
          val withText = imageProcessor.addText(
            resized,
            text,
            config.fontSize,
            TextPosition.BottomRight
          )
          
          val bordered = imageProcessor.addRoundedBorder(
            withText,
            config.borderWidth,
            config.cornerRadius
          )

          val row = index / 2
          val col = index % 2
          val x = config.gap + col * (borderedWidth + config.gap)
          val y = startYPosition + config.gap + row * (borderedHeight + config.gap)

          ProcessedImage(bordered, index, x, y, text)
        } catch (e: Exception) {
          null
        }
      }
    }.awaitAll().filterNotNull().sortedBy { it.index }
  }

  public fun createCollage(
    processedImages: List<ProcessedImage>,
    playerData: PlayerData,
    backgroundImage: ImageData? = null
  ): ImageData {
    if (processedImages.isEmpty()) {
      throw IllegalArgumentException("No processed images provided")
    }

    val borderedWidth = config.imageWidth + 2 * config.borderWidth
    val borderedHeight = config.imageHeight + 2 * config.borderWidth
    val rows = ceil(processedImages.size / 2.0).toInt()
    
    val canvasWidth = borderedWidth * 2 + config.gap * 3
    val canvasHeight = borderedHeight * rows + config.gap * (rows + 1) + 380

    val canvas = imageProcessor.createCanvas(canvasWidth, canvasHeight)

    // Draw background
    if (backgroundImage != null) {
      val blurredBg = imageProcessor.applyBlur(backgroundImage, config.blurRadius)
      // Resize and crop background to fit canvas
      // TODO: Implement background resizing and cropping
    }

    // Draw separator line
    canvas.drawLine(0, 275, canvasWidth, 275, 0xFFD3D3D3.toInt(), 2)

    // Draw title
    drawTitle(canvas, canvasWidth, 275, "Best 20")

    // Draw user info
    drawUserInfo(canvas, playerData, config)

    // Draw images
    for (processedImage in processedImages) {
      canvas.drawImage(processedImage.image, processedImage.x, processedImage.y)
    }

    // Draw timestamp
    drawTimestamp(canvas, canvasWidth, canvasHeight)

    return canvas.toImageData()
  }

  private fun drawTitle(canvas: com.tuforums.preview.image.Canvas, width: Int, separatorY: Int, title: String) {
    // Simplified title drawing - in real implementation, would use proper font metrics
    val x = (width - title.length * 24) / 2
    val y = separatorY + 10
    canvas.drawText(title, x, y, 48, 0xFFFFFFFF.toInt())
  }

  private fun drawUserInfo(
    canvas: com.tuforums.preview.image.Canvas,
    playerData: PlayerData,
    config: CollageConfig
  ) {
    val avatarSize = 120
    val avatarX = 40
    val avatarY = 40

    // Draw avatar (if available)
    playerData.avatarUrl?.let { url ->
      // TODO: Download and draw circular avatar
    }

    // Draw username
    canvas.drawText(playerData.name, avatarX + avatarSize + 25, avatarY + 25, 50, 0xFFFFFFFF.toInt())
    canvas.drawText("ID: ${playerData.id}", avatarX + avatarSize + 25, avatarY + 95, 35, 0xFFFFFFFF.toInt())
    canvas.drawText(
      "排名: ${playerData.stats.rankedScoreRank}",
      avatarX + avatarSize + 25,
      avatarY + 165,
      35,
      0xFFFFFFFF.toInt()
    )

    // Draw stats
    val statsX1 = canvas.width - 900
    val statsY = 40
    val statsText = listOf(
      "总通关数: ${playerData.stats.totalPasses}",
      "U级通关数: ${playerData.stats.universalPassCount}",
      "排位分数: ${String.format("%.2f", playerData.stats.rankedScore)}",
      "常规分数: ${String.format("%.2f", playerData.stats.generalScore)}"
    )
    
    statsText.forEachIndexed { index, text ->
      canvas.drawText(text, statsX1, statsY + index * 50, 35, 0xFFFFFFFF.toInt())
    }

    val statsX2 = canvas.width - 500
    val statsText2 = listOf(
      "平均X-ACC: ${String.format("%.2f", playerData.stats.averageXacc * 100)}%",
      "12K分数: ${String.format("%.2f", playerData.stats.score12K)}",
      "最高击破: ${playerData.stats.topDiff.name}",
      "12K最高击破: ${playerData.stats.top12kDiff.name}"
    )
    
    statsText2.forEachIndexed { index, text ->
      canvas.drawText(text, statsX2, statsY + index * 50, 35, 0xFFFFFFFF.toInt())
    }
  }

  private fun drawTimestamp(canvas: com.tuforums.preview.image.Canvas, width: Int, height: Int) {
    val timestamp = "Query at ${java.time.LocalDateTime.now()}       Created By _Achry_"
    val fontSize = 20
    val margin = 20
    // Simplified - in real implementation would use proper font metrics
    val x = width - timestamp.length * 10 - margin
    val y = height - fontSize - margin
    canvas.drawText(timestamp, x, y, fontSize, 0xFFFFFFFF.toInt())
  }
}

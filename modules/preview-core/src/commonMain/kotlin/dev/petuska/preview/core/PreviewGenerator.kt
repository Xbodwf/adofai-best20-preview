package com.tuforums.preview.core

import com.tuforums.preview.api.PlayerData
import com.tuforums.preview.api.TuForumsApi
import com.tuforums.preview.collage.CollageConfig
import com.tuforums.preview.collage.CollageGenerator
import com.tuforums.preview.image.ImageData
import com.tuforums.preview.image.ImageProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public class PreviewGenerator(
  private val api: TuForumsApi = TuForumsApi(),
  private val imageProcessor: ImageProcessor,
  private val config: CollageConfig = CollageConfig()
) {
  private val collageGenerator = CollageGenerator(imageProcessor, api, config)

  public suspend fun generatePreview(playerId: Int, backgroundImageUrl: String? = null): Result<ImageData> {
    return try {
      val playerData = api.getPlayerData(playerId)
      val topPasses = collageGenerator.getTopPasses(playerData)
      
      if (topPasses.isEmpty()) {
        return Result.failure(IllegalArgumentException("No passes found for player $playerId"))
      }

      val processedImages = collageGenerator.processImages(topPasses)
      
      if (processedImages.isEmpty()) {
        return Result.failure(IllegalArgumentException("Failed to process any images"))
      }

      val backgroundImage = backgroundImageUrl?.let { imageProcessor.downloadImage(it) }
      val collage = collageGenerator.createCollage(processedImages, playerData, backgroundImage)
      
      Result.success(collage)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  public fun generatePreviewFlow(playerId: Int, backgroundImageUrl: String? = null): Flow<GenerationProgress> {
    return flow {
      emit(GenerationProgress.DownloadingPlayerData)
      
      val playerData = api.getPlayerData(playerId)
      emit(GenerationProgress.ProcessingPasses)
      
      val topPasses = collageGenerator.getTopPasses(playerData)
      emit(GenerationProgress.DownloadingImages(topPasses.size))
      
      val processedImages = collageGenerator.processImages(topPasses)
      emit(GenerationProgress.GeneratingCollage)
      
      val backgroundImage = backgroundImageUrl?.let { imageProcessor.downloadImage(it) }
      val collage = collageGenerator.createCollage(processedImages, playerData, backgroundImage)
      
      emit(GenerationProgress.Completed(collage))
    }
  }
}

public sealed class GenerationProgress {
  public object DownloadingPlayerData : GenerationProgress()
  public object ProcessingPasses : GenerationProgress()
  public data class DownloadingImages(val total: Int) : GenerationProgress()
  public object GeneratingCollage : GenerationProgress()
  public data class Completed(val image: ImageData) : GenerationProgress()
  public data class Error(val exception: Throwable) : GenerationProgress()
}

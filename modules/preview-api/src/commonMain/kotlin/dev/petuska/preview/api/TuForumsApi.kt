package com.tuforums.preview.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

public class TuForumsApi(
  private val baseUrl: String = "https://api.tuforums.com/v2",
  private val client: HttpClient = createDefaultClient()
) {
  public companion object {
    public fun createDefaultClient(): HttpClient = HttpClient {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          isLenient = true
          coerceInputValues = true
        })
      }
    }
  }

  public suspend fun getPlayerData(playerId: Int): PlayerData = withContext(Dispatchers.IO) {
    client.get("$baseUrl/database/players/$playerId").body()
  }
  
  public fun getLevelThumbnailUrl(levelId: String): String {
    return "$baseUrl/media/thumbnail/level/$levelId"
  }
}

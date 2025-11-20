package com.tuforums.preview.api

import kotlinx.serialization.Serializable

@Serializable
public data class PlayerData(
  val id: Int,
  val name: String,
  val avatarUrl: String? = null,
  val stats: PlayerStats,
  val passes: List<PassData>
)

@Serializable
public data class PlayerStats(
  val totalPasses: Int,
  val universalPassCount: Int,
  val rankedScore: Double,
  val generalScore: Double,
  val averageXacc: Double,
  val score12K: Double,
  val rankedScoreRank: Int,
  val topDiff: Difficulty,
  val top12kDiff: Difficulty
)

@Serializable
public data class Difficulty(
  val name: String
)

@Serializable
public data class PassData(
  val levelId: String,
  val scoreV2: String,
  val judgements: Judgements
)

@Serializable
public data class Judgements(
  val accuracy: String
)

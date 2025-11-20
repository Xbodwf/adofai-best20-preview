package com.tuforums.preview.collage

public data class CollageConfig(
  val gap: Int = 25,
  val borderWidth: Int = 8,
  val cornerRadius: Int = 25,
  val fontSize: Int = 28,
  val imageWidth: Int = 800,
  val imageHeight: Int = 420,
  val maxImages: Int = 20,
  val blurRadius: Int = 3
)

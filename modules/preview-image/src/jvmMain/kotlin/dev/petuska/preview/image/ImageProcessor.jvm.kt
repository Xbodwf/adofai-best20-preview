package com.tuforums.preview.image

import java.io.ByteArrayInputStream

internal fun ByteArray.inputStream() = ByteArrayInputStream(this)

package com.example.musicactivity

import android.net.Uri
import android.provider.MediaStore

data class Music(
    val uri: Uri,
    val title: String,
    val artist: String,
    val time: Int
) {
    fun musicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, title)
    }
}

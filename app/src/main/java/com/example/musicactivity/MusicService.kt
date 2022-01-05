package com.example.musicactivity

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

private const val MUSIC_NOTIFICATION_ID = 100

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val CHANNEL_ID = "MusicChannel"
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicServiceBinder()
    private val builder = NotificationCompat.Builder(this, CHANNEL_ID)

    var onMusicChangedListener: OnMusicChangedListener? = null

    var music: Music? = null
        private set

    private fun initMusicPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.apply {
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initMusicPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    fun playMusic(music: Music) {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        initMusicPlayer()
        mediaPlayer?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, music.uri)
            this@MusicService.music = music
            prepareAsync()
        }

        builder.setContentTitle(music.title)
            .setContentTitle(music.artist)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
            ).priority = NotificationCompat.PRIORITY_DEFAULT

        startForeground(MUSIC_NOTIFICATION_ID, builder.build())
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
        onMusicChangedListener?.onMusicChanged(music)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopForeground(true)
        music = null
        onMusicChangedListener?.onMusicChanged(music)
    }

    inline fun setOnMusicChangedListener(
        crossinline onMusicChanged: (Music?) -> Unit
    ) {
        onMusicChangedListener = object : OnMusicChangedListener {
            override fun onMusicChanged(music: Music?) {
                onMusicChanged(music)
            }
        }
    }

    interface OnMusicChangedListener {
        fun onMusicChanged(music: Music?)
    }
}
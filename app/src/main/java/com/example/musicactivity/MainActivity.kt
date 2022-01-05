package com.example.musicactivity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val musicAdapter = MusicAdapter()
    private lateinit var musicService: MusicService
    private var bound = false
    private val CHANNEL_ID = "MusicChannel"

    private lateinit var nowPlayingTitle: TextView
    private lateinit var nowPlayingArtist: TextView

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicServiceBinder
            musicService = binder.getService()
            bound = true

            nowPlayingTitle.text = musicService?.music?.title ?: ""
            nowPlayingArtist.text = musicService?.music?.artist ?: ""
            musicService?.setOnMusicChangedListener {
                nowPlayingTitle.text = it?.title ?: ""
                nowPlayingArtist.text = it?.artist ?: ""
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    private val readPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val musicRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        nowPlayingTitle = findViewById(R.id.music_bar_title)
        nowPlayingArtist = findViewById(R.id.music_bar_artist)

        musicRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = musicAdapter
        }

        readPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onStart() {
        super.onStart()
        musicAdapter.setItemClickListener(object : MusicAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(v.context, MusicService::class.java)
                startService(intent)
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        })
    }

    private fun getMusicList(): List<Music> {
        val musicList = mutableListOf<Music>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION
        )

        val query = contentResolver.query(
            collection, projection, null, null, null
        )

        query?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val timeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                musicList += Music(
                    uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        it.getLong(idColumn)
                    ),
                    artist = it.getString(artistColumn),
                    title = it.getString(titleColumn),
                    time = it.getInt(timeColumn)
                )
            }
        }

        return musicList

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

}
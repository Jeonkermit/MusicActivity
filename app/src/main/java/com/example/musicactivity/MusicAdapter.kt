package com.example.musicactivity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class MusicAdapter() : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    val musicList = mutableListOf<Music>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var musicUri: Uri? = null
        val musicTitle = itemView.findViewById<TextView>(R.id.music_title)
        val musicArtist = itemView.findViewById<TextView>(R.id.music_artist)
        val musicTime = itemView.findViewById<TextView>(R.id.music_time)

        fun setMusic(music: Music) {
            musicUri = music.musicUri()
            musicTitle.text = music.title
            musicArtist.text = music.artist
            val time = SimpleDateFormat("mm:ss")
            musicTime.text = time.format(music.time)
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    private lateinit var itemClick: OnItemClickListener

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClick = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList.get(position)
        holder.setMusic(music)
        holder.itemView.setOnClickListener {
            itemClick.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}
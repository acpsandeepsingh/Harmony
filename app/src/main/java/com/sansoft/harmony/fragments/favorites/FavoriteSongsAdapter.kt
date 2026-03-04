package com.sansoft.harmony.fragments.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sansoft.harmony.R
import com.sansoft.harmony.database.favorite.model.FavoriteSong

class FavoriteSongsAdapter(
    private val onRemoveClickListener: (FavoriteSong) -> Unit
) : RecyclerView.Adapter<FavoriteSongHolder>() {

    private val songs = mutableListOf<FavoriteSong>()

    fun setSongs(songs: List<FavoriteSong>) {
        this.songs.clear()
        this.songs.addAll(songs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteSongHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_song_item, parent, false)
        return FavoriteSongHolder(view, onRemoveClickListener)
    }

    override fun onBindViewHolder(holder: FavoriteSongHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size
}

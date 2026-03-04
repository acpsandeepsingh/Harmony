package com.sansoft.harmony.fragments.favorites

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sansoft.harmony.R
import com.sansoft.harmony.database.favorite.model.FavoriteSong
import com.sansoft.harmony.util.TimeAgo.formatDuration
import com.sansoft.harmony.util.image.CoilHelper

class FavoriteSongHolder(
    itemView: View,
    private val onRemoveClickListener: (FavoriteSong) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    private val title: TextView = itemView.findViewById(R.id.title)
    private val uploader: TextView = itemView.findViewById(R.id.uploader)
    private val duration: TextView = itemView.findViewById(R.id.duration)
    private val removeButton: ImageButton = itemView.findViewById(R.id.remove_button)

    fun bind(song: FavoriteSong) {
        title.text = song.title
        uploader.text = song.uploader
        duration.text = formatDuration(song.duration)
        CoilHelper.loadThumbnail(thumbnail, song.thumbnailUrl)

        removeButton.setOnClickListener {
            onRemoveClickListener(song)
        }
    }
}

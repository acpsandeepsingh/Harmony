package com.sansoft.harmony.local.playlist

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sansoft.harmony.NewPipeDatabase
import com.sansoft.harmony.database.playlist.model.PlaylistEntity
import io.reactivex.rxjava3.schedulers.Schedulers

class FirebasePlaylistManager(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val localPlaylistManager = LocalPlaylistManager(NewPipeDatabase.getInstance(context))

    fun sync() {
        val userId = auth.currentUser?.uid ?: return

        // Push local playlists to Firebase
        localPlaylistManager.playlists
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                it.forEach { playlist ->
                    val playlistId = playlist.uid.toString()
                    val playlistRef = firestore.collection("users").document(userId)
                        .collection("playlists").document(playlistId)

                    playlistRef.set(playlist)
                }
            }

        // Fetch remote playlists from Firebase
        firestore.collection("users").document(userId).collection("playlists")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val playlist = document.toObject(PlaylistEntity::class.java)
                    // TODO: Add logic to update local database with fetched playlist
                }
            }
    }
}

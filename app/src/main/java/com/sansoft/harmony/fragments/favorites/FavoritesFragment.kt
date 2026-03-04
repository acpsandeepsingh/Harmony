package com.sansoft.harmony.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sansoft.harmony.R
import com.sansoft.harmony.database.AppDatabase

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: FavoriteSongsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = ViewModelFactory(database)
        viewModel = ViewModelProvider(this, factory).get(FavoritesViewModel::class.java)

        adapter = FavoriteSongsAdapter { song ->
            viewModel.removeFavorite(song)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.favorites_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.getFavorites().observe(viewLifecycleOwner) { songs ->
            adapter.setSongs(songs)
        }

        return view
    }
}

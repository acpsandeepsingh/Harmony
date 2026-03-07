package com.sansoft.harmony.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sansoft.harmony.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {

    public interface VideoClickListener {
        void onVideoClicked(@NonNull VideoItem item);
    }

    private final List<VideoItem> items = new ArrayList<>();
    private final VideoClickListener clickListener;

    public VideoAdapter(@NonNull final VideoClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitItems(@NonNull final List<VideoItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoHolder holder, final int position) {
        final VideoItem item = items.get(position);
        holder.title.setText(item.title + " • " + item.uploader);
        holder.itemView.setOnClickListener(v -> clickListener.onVideoClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VideoHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        VideoHolder(@NonNull final View itemView) {
            super(itemView);
            title = (TextView) itemView;
        }
    }
}

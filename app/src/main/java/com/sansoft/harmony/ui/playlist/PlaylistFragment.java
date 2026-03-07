package com.sansoft.harmony.ui.playlist;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlaylistFragment extends Fragment {

    public PlaylistFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull final android.view.LayoutInflater inflater,
                             @Nullable final android.view.ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final TextView textView = new TextView(requireContext());
        textView.setText("Playlist screen (native player flow)");
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}

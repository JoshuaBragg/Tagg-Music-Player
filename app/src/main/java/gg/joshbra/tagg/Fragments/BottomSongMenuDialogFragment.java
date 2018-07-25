package gg.joshbra.tagg.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import gg.joshbra.tagg.R;

public class BottomSongMenuDialogFragment extends BottomSheetDialogFragment {
    private BottomMenuListener listener;

    public final static String OPTION_UPDATE_TAGGS = "update_taggs";
    public final static String OPTION_PLAY = "play";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_menu_song, container, false);

        Bundle args = getArguments();

        TextView songNameTextView = view.findViewById(R.id.songNameBotMenu);

        try {
            songNameTextView.setText(args.getString("songName"));
        } catch (NullPointerException e) {}

        Button playBtn = view.findViewById(R.id.playBtn);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOptionSelected(OPTION_PLAY);
                dismiss();
            }
        });

        Button updateTaggBtn = view.findViewById(R.id.updateTaggBtn);

        updateTaggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOptionSelected(OPTION_UPDATE_TAGGS);
                dismiss();
            }
        });

        return view;
    }

    public void setListener(BottomMenuListener listener) {
        this.listener = listener;
    }

    public interface BottomMenuListener {
        void onOptionSelected(String type);
    }
}
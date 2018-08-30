package gg.joshbra.tagg.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongManager;

/**
 * Fragment for the menu that appears from bottom of screen for editing the Tagg options
 */
public class BottomTaggOptionFragment extends BottomSheetDialogFragment {

    public static final String TAGG_NAME = "tagg_name";

    private TaggOptionListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View hostView = inflater.inflate(R.layout.bottom_tagg_options, container, false);

        Bundle args = getArguments();

        final TextView taggNameTextView = hostView.findViewById(R.id.taggNameBotMenu);
        TextView taggDetailsTextView = hostView.findViewById(R.id.taggDetailsBotMenu);

        final String taggName = args.getString(TAGG_NAME);

        try {
            if (taggName != null) {
                taggNameTextView.setText(taggName);
                taggDetailsTextView.setText(SongManager.getSelf().getTaggDetailsString(taggName));
            }
        } catch (NullPointerException e) {}

        // Sets on click listener for delete tagg button
        hostView.findViewById(R.id.deleteTaggBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if (listener != null) {
                                    listener.deleteTagg();
                                }
                                dismiss();
                                break;
                        }
                    }
                };

                // Creates Dialog box for confirmation for deletion
                new AlertDialog.Builder(view.getContext(), R.style.Dialog)
                        .setTitle("Remove Tagg: " + taggName)
                        .setMessage("Are you sure? You cannot undo this action.")
                        .setPositiveButton("Yes, Delete Tagg", dialogClickListener)
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        });

        // Sets on click listener for rename tagg button
        hostView.findViewById(R.id.renameTaggBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText renameTaggEditText = new EditText(view.getContext());
                renameTaggEditText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTextSecondary));
                renameTaggEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                renameTaggEditText.requestFocus();

                final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                final AlertDialog alert = new AlertDialog.Builder(view.getContext(), R.style.Dialog)
                        .setView(renameTaggEditText)
                        .setTitle("Rename Tagg: " + taggName)
                        .setPositiveButton("Rename", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button addBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (listener != null) {
                                    if (listener.renameTagg(renameTaggEditText.getText().toString(), renameTaggEditText)) {
                                        taggNameTextView.setText(renameTaggEditText.getText().toString());

                                        imm.hideSoftInputFromWindow(renameTaggEditText.getWindowToken(), 0);

                                        alert.cancel();
                                    }
                                }
                            }
                        });
                    }
                });

                alert.show();
            }
        });

        return hostView;
    }


    /**
     * Interface to be created when using this fragment
     */
    public interface TaggOptionListener {
        void deleteTagg();
        boolean renameTagg(String newTaggName, EditText editText);
    }

    public void setListener(TaggOptionListener listener) {
        this.listener = listener;
    }
}

package gg.joshbra.tagg.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import gg.joshbra.tagg.Adapters.TaggAdapter;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongManager;
import gg.joshbra.tagg.TaggSelector;

public class BottomTaggSelectionFragment extends BottomSheetDialogFragment {
    private BottomTaggSelectionListener listener;
    private ArrayList<String> aTagg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View hostView = inflater.inflate(R.layout.bottom_tagg_selection, container, false);

        createCheckboxes(hostView);

        aTagg = SongManager.getSelf().getActiveTaggs();

        ImageButton addTaggBtn = hostView.findViewById(R.id.addTaggBtn);

        addTaggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText newTaggEditText = new EditText(view.getContext());
                newTaggEditText.setTextColor(getResources().getColor(R.color.colorTextSecondary));

                newTaggEditText.requestFocus();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                final AlertDialog alert = new AlertDialog.Builder(view.getContext(), R.style.Dialog)
                        .setView(newTaggEditText)
                        .setTitle("New Tagg:")
                        .setPositiveButton("Add", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button addBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String newTagg = newTaggEditText.getText().toString();

                                if (!Pattern.matches("^[\\w\\s]*$", newTagg)) {
                                    newTaggEditText.setError("Invalid tagg! Only letters and numbers accepted");
                                    return;
                                } else {
                                    newTaggEditText.setError(null);
                                }

                                SongManager.getSelf().addTagg(newTagg);
                                Collections.sort(SongManager.getSelf().getTaggs());

                                createCheckboxes(hostView);

                                alert.cancel();
                            }
                        });
                    }
                });

                alert.show();
            }
        });

        ImageButton editTaggBtn = hostView.findViewById(R.id.editTaggBtn);

        editTaggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView taggRV = hostView.findViewById(R.id.taggSelectRGroup);
                for (int i = 0; i < taggRV.getChildCount(); i++) {
                    TaggAdapter.TaggHolder holder = (TaggAdapter.TaggHolder) taggRV.findViewHolderForAdapterPosition(i);
                    ImageButton removeBtn = holder.getDeleteBtn();
                    TextView taggName = holder.getTaggName();
                    if (removeBtn.getVisibility() == View.GONE) {
                        removeBtn.setVisibility(View.VISIBLE);
                        setMargins(taggName, 0, 0, 24, 0);
                    } else {
                        removeBtn.setVisibility(View.GONE);
                        setMargins(taggName, 0, 0, 0, 0);
                    }
                }
            }
        });

        return hostView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.dismissed(aTagg);
    }

    private void createCheckboxes(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.taggSelectRGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<String> taggs = SongManager.getSelf().getTaggs();
        ArrayList<String> activeTaggs = SongManager.getSelf().getActiveTaggs();

        if (taggs.size() == 0) {
            TextView noTaggs = new TextView(getContext());
            noTaggs.setText("No Taggs exist");
            noTaggs.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            noTaggs.setPadding(0, 50, 0, 20);
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).addView(noTaggs);
            return;
        } else {
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).removeAllViews();
        }

        ArrayList<TaggSelector> selectors = new ArrayList<>();

        for (String t : taggs) {
            selectors.add(new TaggSelector(t, activeTaggs.contains(t)));
        }

        TaggAdapter taggAdapter = new TaggAdapter(view.getContext(), selectors, TaggAdapter.ACTIVATE_TYPE);

        recyclerView.setAdapter(taggAdapter);
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            final float scale = getContext().getResources().getDisplayMetrics().density;
            // convert the DP into pixel
            int l =  (int)(left * scale + 0.5f);
            int r =  (int)(right * scale + 0.5f);
            int t =  (int)(top * scale + 0.5f);
            int b =  (int)(bottom * scale + 0.5f);

            p.setMargins(l, t, r, b);
            view.requestLayout();
        }
    }

    public void setListener(BottomTaggSelectionListener listener) {
        this.listener = listener;
    }

    public interface BottomTaggSelectionListener {
        void dismissed(ArrayList<String> aTagg);
    }
}

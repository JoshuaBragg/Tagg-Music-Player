package gg.joshbra.tagg.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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

/**
 * Fragment for the menu that appears from bottom of screen when selecting Taggs
 */
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

        // Creates new Tagg
        addTaggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText newTaggEditText = new EditText(view.getContext());
                newTaggEditText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTextSecondary));
                newTaggEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                newTaggEditText.requestFocus();

                final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

                                if (newTagg.length() == 0) {
                                    newTaggEditText.setError("Tagg name must contain at least one character.");
                                    return;
                                } else {
                                    newTaggEditText.setError(null);
                                }

                                SongManager.getSelf().addTagg(newTagg);
                                Collections.sort(SongManager.getSelf().getTaggs());

                                createCheckboxes(hostView);

                                imm.hideSoftInputFromWindow(newTaggEditText.getWindowToken(), 0);

                                alert.cancel();
                            }
                        });
                    }
                });

                alert.show();
            }
        });

        ImageButton editTaggBtn = hostView.findViewById(R.id.editTaggBtn);

        // Enables the remove tagg button
        editTaggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView taggRV = hostView.findViewById(R.id.taggSelectRGroup);
                for (int i = 0; i < taggRV.getChildCount(); i++) {
                    TaggAdapter.TaggHolder holder = (TaggAdapter.TaggHolder) taggRV.findViewHolderForAdapterPosition(i);
                    ImageButton optionBtn = holder.getTaggOptionsBtn();
                    TextView taggName = holder.getTaggName();
                    if (optionBtn.getVisibility() == View.GONE) {
                        optionBtn.setVisibility(View.VISIBLE);
                        setMargins(taggName, 0, 0, 24, 0);
                    } else {
                        optionBtn.setVisibility(View.GONE);
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

    /**
     * Populates the recyclerView with the checkboxes
     * @param view The view that the recyclerView belongs to
     */
    private void createCheckboxes(final View view) {
        RecyclerView recyclerView = view.findViewById(R.id.taggSelectRGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<String> taggs = SongManager.getSelf().getTaggs();
        ArrayList<String> activeTaggs = SongManager.getSelf().getActiveTaggs();

        if (taggs.size() == 0) {
            // In this case there are no Taggs yet
            TextView noTaggs = new TextView(getContext());
            noTaggs.setText("No Taggs exist");
            noTaggs.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextSecondary));
            noTaggs.setPadding(0, 50, 0, 20);
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).addView(noTaggs);
            return;
        } else {
            // This is needed to get rid of anything that is in the view right now
            ((LinearLayout)view.findViewById(R.id.noTaggMessageSpace)).removeAllViews();
        }

        ArrayList<TaggSelector> selectors = new ArrayList<>();

        for (String t : taggs) {
            selectors.add(new TaggSelector(t, activeTaggs.contains(t)));
        }

        TaggAdapter taggAdapter = new TaggAdapter(view.getContext(), selectors, TaggAdapter.ACTIVATE_TYPE);

        taggAdapter.setListener(new TaggAdapter.TaggAdapterListener() {
            @Override
            public void updateCheckboxes() {
                createCheckboxes(view);
            }
        });

        recyclerView.setAdapter(taggAdapter);
    }

    /**
     * Programmatically change the margins of a view
     * @param view The view to change the margins of
     * @param left Left margin
     * @param top Top margin
     * @param right Right margin
     * @param bottom Bottom margin
     */
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

    /**
     * Interface to be created when using this fragment
     */
    public interface BottomTaggSelectionListener {
        void dismissed(ArrayList<String> aTagg);
    }
}

package gg.joshbra.tagg.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import gg.joshbra.tagg.Fragments.BottomTaggOptionFragment;
import gg.joshbra.tagg.R;
import gg.joshbra.tagg.SongManager;
import gg.joshbra.tagg.TaggSelector;

/**
 * RecyclerView Adapter that allows Tagg to create lists of Taggs
 */
public class TaggAdapter extends RecyclerView.Adapter<TaggAdapter.TaggHolder> {

    private ArrayList<TaggSelector> taggs;
    private Context context;
    private int type;
    private TaggAdapterListener listener;

    // The two types of TaggAdapters
    public static final int ACTIVATE_TYPE = 0, UPDATE_TYPE = 1;

    /**
     * Creates a TaggAdapter for use in a RecyclerView
     * @param context The context that contains the RecyclerView
     * @param taggs The arraylist of Taggs to display
     * @param type The type of Adapter this should be (ACTIVATE_TYPE or UPDATE_TYPE)
     */
    public TaggAdapter(Context context, ArrayList<TaggSelector> taggs, int type) {
        this.context = context;
        this.taggs = taggs;
        this.type = type;
    }

    @NonNull
    @Override
    public TaggHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.tagg_selection_row, parent, false);
        return new TaggHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaggHolder holder, final int position) {
        final TaggSelector tagg = taggs.get(position);

        holder.taggName.setText(tagg.getTaggName());
        holder.checkBox.setChecked(tagg.isChecked());

        // Two different listeners for each type of adapter
        if (type == ACTIVATE_TYPE) {
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        SongManager.getSelf().activateTagg(tagg.getTaggName());
                    } else {
                        SongManager.getSelf().deactivateTagg(tagg.getTaggName());
                    }
                }
            });
        } else if (type == UPDATE_TYPE) {
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    tagg.toggleChecked();
                }
            });
        }

        // Tagg options button on click listener
        holder.taggOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates bottomTaggOptionFragment
                BottomTaggOptionFragment bottomTaggOptionFragment = new BottomTaggOptionFragment();

                Bundle args = new Bundle();

                args.putString(BottomTaggOptionFragment.TAGG_NAME, holder.getTaggName().getText().toString());

                bottomTaggOptionFragment.setArguments(args);

                // Sets listener allowing the optionFragment to run code within this class
                bottomTaggOptionFragment.setListener(new BottomTaggOptionFragment.TaggOptionListener() {
                    @Override
                    public void deleteTagg() {
                        SongManager.getSelf().removeTagg(holder.taggName.getText().toString());
                        int pos = taggs.indexOf(new TaggSelector(holder.taggName.getText().toString(), false));
                        taggs.remove(new TaggSelector(holder.taggName.getText().toString(), false));
                        notifyItemRemoved(pos);
                        if (listener != null && taggs.size() == 0) {
                            listener.updateCheckboxes();
                        }
                    }

                    @Override
                    public boolean renameTagg(String newTaggName, EditText editText) {
                        if (newTaggName.length() == 0) {
                            editText.setError("Tagg name must contain at least one character.");
                            return false;
                        } else {
                            editText.setError(null);
                        }

                        SongManager.getSelf().renameTagg(holder.getTaggName().getText().toString(), newTaggName);

                        if (listener != null) {
                            listener.updateCheckboxes();
                        }

                        return true;
                    }
                });
                bottomTaggOptionFragment.show((context instanceof AppCompatActivity ? (AppCompatActivity)context : ((AppCompatActivity)((ContextThemeWrapper)context).getBaseContext())).getSupportFragmentManager(), "bottomTaggOptions");
            }
        });

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
    }

    /**
     * Gets the Taggs that are currently checked
     * @return ArrayList of Taggs that are currently checked
     */
    public ArrayList<String> getCheckedTaggs() {
        ArrayList<String> out = new ArrayList<>();

        for (TaggSelector taggSelector : taggs) {
            if (taggSelector.isChecked()) {
                out.add(taggSelector.getTaggName());
            }
        }

        return out;
    }

    @Override
    public int getItemCount() {
        return taggs.size();
    }

    /**
     * Interface to be created when using this Adapter
     */
    public interface TaggAdapterListener {
        void updateCheckboxes();
    }

    public void setListener(TaggAdapterListener listener) {
        this.listener = listener;
    }

    /**
     * A ViewHolder for RecyclerView to hold the information obtained from a TaggSelector
     */
    public class TaggHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView taggName;
        ImageButton taggOptionsBtn;
        View view;

        public TaggHolder(View itemView) {
            super(itemView);
            view = itemView;
            taggName = itemView.findViewById(R.id.taggNameTextView);
            checkBox = itemView.findViewById(R.id.taggCheckbox);
            taggOptionsBtn = itemView.findViewById(R.id.taggOptionBtn);
        }

        public ImageButton getTaggOptionsBtn() {
            return taggOptionsBtn;
        }

        public TextView getTaggName() {
            return taggName;
        }

        public View getView() {
            return view;
        }
    }
}

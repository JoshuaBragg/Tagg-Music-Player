package ca.bragg.tagg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class TaggAdapter extends RecyclerView.Adapter<TaggAdapter.TaggHolder> {

    private ArrayList<TaggSelector> taggs;
    private Context context;

    public TaggAdapter(Context context, ArrayList<TaggSelector> taggs) {
        this.context = context;
        this.taggs = taggs;
    }

    @NonNull
    @Override
    public TaggHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.tagg_selection_row, parent, false);
        return new TaggHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaggHolder holder, int position) {
        final TaggSelector tagg = taggs.get(position);

        final TaggAdapter inst = this;

        holder.taggName.setText(tagg.getTaggName());
        holder.checkBox.setChecked(tagg.isChecked());

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

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SongManager.getSelf().removeTagg(holder.taggName.getText().toString());
                int pos = taggs.indexOf(new TaggSelector(holder.taggName.getText().toString(), false));
                taggs.remove(new TaggSelector(holder.taggName.getText().toString(), false));
                notifyItemRemoved(pos);
            }
        });

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return taggs.size();
    }

    public class TaggHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView taggName;
        ImageButton deleteBtn;
        View view;

        public TaggHolder(View itemView) {
            super(itemView);
            view = itemView;
            taggName = itemView.findViewById(R.id.taggNameTextView);
            checkBox = itemView.findViewById(R.id.taggCheckbox);
            deleteBtn = itemView.findViewById(R.id.removeTaggBtn);
        }

        public ImageButton getDeleteBtn() {
            return deleteBtn;
        }

        public View getView() {
            return view;
        }
    }
}

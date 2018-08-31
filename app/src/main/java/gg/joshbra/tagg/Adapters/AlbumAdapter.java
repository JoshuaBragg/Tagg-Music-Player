package gg.joshbra.tagg.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;

import gg.joshbra.tagg.Activities.AlbumSongListActivity;
import gg.joshbra.tagg.Activities.RecentlyAddedActivity;
import gg.joshbra.tagg.AlbumInfo;
import gg.joshbra.tagg.Helpers.AlbumArtRetriever;
import gg.joshbra.tagg.Helpers.AlbumGridScaler;
import gg.joshbra.tagg.R;

/**
 * RecyclerView Adapter that allows Tagg to create lists of albums
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {
    private ArrayList<AlbumInfo> albums;
    private Context context;

    public AlbumAdapter(Context context, ArrayList<AlbumInfo> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.album_grid_item, parent, false);
        ViewGroup.LayoutParams layoutParams = myView.findViewById(R.id.albumItemContainer).getLayoutParams();
        layoutParams.width = ((Long)Math.round(AlbumGridScaler.getItemWidthPixels(myView.getContext()))).intValue();
        myView.findViewById(R.id.albumItemContainer).setLayoutParams(layoutParams);
        return new AlbumHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlbumHolder holder, final int position) {
        final AlbumInfo album = albums.get(position);

        // Set album and artist name
        holder.albumNameTextView.setText(album.getAlbumName());
        holder.albumArtistNameTextView.setText(album.getAlbumArtist());

        // Set album art if available
        String albumArtPath = AlbumArtRetriever.getAlbumArt(((Long)album.getAlbumID()).intValue());

        if (albumArtPath != null) {
            holder.albumArtImageView.setImageBitmap(BitmapFactory.decodeFile(albumArtPath));
        }

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch AlbumSongListActivity
                Intent intent = new Intent(context, AlbumSongListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(AlbumSongListActivity.ALBUM_NAME, album.getAlbumName());
                intent.putExtra(AlbumSongListActivity.ALBUM_ID, album.getAlbumID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    /**
     * A ViewHolder for RecyclerView to hold the information obtained from a AlbumInfo
     */
    public class AlbumHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView albumArtImageView;
        TextView albumNameTextView;
        TextView albumArtistNameTextView;

        public AlbumHolder(View itemView) {
            super(itemView);
            view = itemView;
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            albumArtistNameTextView = itemView.findViewById(R.id.albumArtistNameTextView);
        }

        public View getView() {
            return view;
        }
    }
}

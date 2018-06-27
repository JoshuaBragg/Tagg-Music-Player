package ca.bragg.tagg.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import ca.bragg.tagg.MaxDimensionRecycler;
import ca.bragg.tagg.R;
import ca.bragg.tagg.SongAdapter;
import ca.bragg.tagg.SongManager;
import ca.bragg.tagg.TaggAdapter;
import ca.bragg.tagg.TaggSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class TaggActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private SongManager songManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagg);
        
        songManager = SongManager.getSelf();

        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Taggs");

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = findViewById(R.id.taggFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ViewGroup container = (ViewGroup) getLayoutInflater().inflate(R.layout.tagg_selection_popup, null);

                createCheckboxes(container);

                // TODO: https://stackoverflow.com/questions/3221488/blur-or-dim-background-when-android-popupwindow-active

                popupWindow = new PopupWindow(container, (int) Math.round(Resources.getSystem().getDisplayMetrics().widthPixels * (2.0 / 3.0)), ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);

                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                popupWindow.setElevation(50);

                popupWindow.showAtLocation(findViewById(R.id.drawer), Gravity.CENTER, 0, 0);

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        updateSongRepeater();
                    }
                });

                ImageButton addTaggBtn = container.findViewById(R.id.addTaggBtn);

                addTaggBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final EditText newTaggEditText = new EditText(view.getContext());
                        newTaggEditText.setTextColor(getResources().getColor(R.color.colorTextSecondary));

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

                                        songManager.addTagg(newTagg);
                                        Collections.sort(songManager.getTaggs());

                                        createCheckboxes(container);

                                        alert.cancel();
                                    }
                                });
                            }
                        });

                        alert.show();
                    }
                });

                ImageButton editTaggBtn = container.findViewById(R.id.editTaggBtn);

                editTaggBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RecyclerView taggRV = container.findViewById(R.id.taggSelectRGroup);
                        for (int i = 0; i < taggRV.getChildCount(); i++) {
                            TaggAdapter.TaggHolder holder = (TaggAdapter.TaggHolder) taggRV.findViewHolderForAdapterPosition(i);
                            ImageButton removeBtn = holder.getDeleteBtn();
                            if (removeBtn.getVisibility() == View.INVISIBLE) {
                                removeBtn.setVisibility(View.VISIBLE);
                            } else {
                                removeBtn.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
            }
        });
    }

    private void createCheckboxes(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.taggSelectRGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<String> taggs = songManager.getTaggs();
        ArrayList<String> activeTaggs = songManager.getActiveTaggs();

        if (taggs.size() == 0) {
            TextView noTaggs = new TextView(this);
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

        TaggAdapter taggAdapter = new TaggAdapter(view.getContext(), selectors);

        recyclerView.setAdapter(taggAdapter);
    }

    private void updateSongRepeater() {
        // TODO: make repeater a fragment to be resused, all you would need to pass in is the array of songs to create adapter

        setTitle(songManager.getActiveTaggs().size() == 0 ? "Taggs" : "Taggs (" + songManager.getActiveTaggs().size() + ")");
        
        songManager.updateCurrSongs();

        songAdapter = new SongAdapter(this, songManager.getCurrSongs());

        recyclerView.setAdapter(songAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.songMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Songs", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, ca.bragg.tagg.Activities.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        else if (id == R.id.taggMenu) {
            item.setChecked(true);
            //Toast.makeText(this, "Taggs", Toast.LENGTH_LONG).show();
        }

        mDrawerLayout.closeDrawer(Gravity.START);

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NavigationView)findViewById(R.id.navView)).getMenu().getItem(1).setChecked(true);
    }
}

package com.bragg.tagg;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

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
                ViewGroup container = (ViewGroup) getLayoutInflater().inflate(R.layout.tagg_selection_popup, null);

                fillRadioGroup(container);

                popupWindow = new PopupWindow(container, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
            }
        });
    }

    private void fillRadioGroup(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.taggSelectRGroup);

        ArrayList<String> taggs = songManager.getTaggs();
        ArrayList<String> activeTaggs = songManager.getActiveTaggs();

        for (String s : taggs) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(s);
            checkBox.setTextColor(getResources().getColor(R.color.colorTextSecondary));

            if (activeTaggs.contains(s)) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        songManager.activateTagg(compoundButton.getText().toString());
                    } else {
                        songManager.deactivateTagg(compoundButton.getText().toString());
                    }
                }
            });

            linearLayout.addView(checkBox);
        }
    }

    private void updateSongRepeater() {
        // TODO: make repeater a fragment to be resused, all you would need to pass in is the array of songs to create adapter
        
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
            Toast.makeText(this, "Songs", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        else if (id == R.id.taggMenu) {
            item.setChecked(true);
            Toast.makeText(this, "Taggs", Toast.LENGTH_LONG).show();

//            Intent intent = new Intent(this, TaggActivity.class);
//            startActivity(intent);
            //overridePendingTransition(R.anim.slide_in_up, R.anim.empty_transition);
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

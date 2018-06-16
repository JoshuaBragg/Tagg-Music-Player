package com.bragg.tagg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String[] TABLE_NAMES = {"songs", "taggs"};
    private static final String[][] COL_NAMES = {{"song_name", "artist_name", "url"}, {}};

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAMES[0], null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAMES[0] + " (ID INTEGER PRIMARY KEY, " + COL_NAMES[0][0] + " TEXT, " + COL_NAMES[0][1] + " TEXT, " + COL_NAMES[0][2] + " TEXT)";
        Log.i("d", createTable);
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAMES[0]);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(String song_name, String artist_name, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAMES[0][0], song_name);
        contentValues.put(COL_NAMES[0][1], artist_name);
        contentValues.put(COL_NAMES[0][2], url);


        long result = db.insert(TABLE_NAMES[0], null, contentValues);

        return result != -1;
    }

    public boolean removeData(String song_name, String artist_name, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAMES[0], COL_NAMES[0][0] + " = '" + song_name + "' AND " + COL_NAMES[0][1] + " = '" + artist_name + "' AND " + COL_NAMES[0][2] + " = '" + url + "'", null) > 0;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAMES[0] + " ORDER BY " + COL_NAMES[0][0] + " ASC";
        Cursor data = db.rawQuery(q, null);
        return data;
    }
}

package gg.joshbra.tagg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String[] TABLE_NAMES;
    private static final String[][] COL_NAMES;

    // TODO: use selection/where args

    static {
        TABLE_NAMES = new String[]{"songs", "taggs", "tagg_map"};
        COL_NAMES = new String[][]{{"ID", "song_name", "artist_name", "url", "date_added"}, {"ID", "tagg_name"}, {"song_id", "tagg_id"}};
    }

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAMES[0], null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createSongTable = "CREATE TABLE " + TABLE_NAMES[0] + " (" + COL_NAMES[0][0] + " INTEGER PRIMARY KEY, " + COL_NAMES[0][1] + " TEXT, " + COL_NAMES[0][2] + " TEXT, " + COL_NAMES[0][3] + " TEXT, " + COL_NAMES[0][4] + " TEXT, UNIQUE (" + COL_NAMES[0][1] + ", " + COL_NAMES[0][2] + ", " + COL_NAMES[0][3] + ", " + COL_NAMES[0][4] + ") ON CONFLICT IGNORE)";
        sqLiteDatabase.execSQL(createSongTable);

        String createTaggTable = "CREATE TABLE " + TABLE_NAMES[1] + " (" + COL_NAMES[1][0] + " INTEGER PRIMARY KEY, " + COL_NAMES[1][1] + " TEXT, UNIQUE (" + COL_NAMES[1][1] + ") ON CONFLICT IGNORE)";
        sqLiteDatabase.execSQL(createTaggTable);

        String createMapTable = "CREATE TABLE " + TABLE_NAMES[2] + " (" + COL_NAMES[2][0] + " INTEGER, " + COL_NAMES[2][1] + " INTEGER, UNIQUE (" + COL_NAMES[2][0] + ", " + COL_NAMES[2][1] + ") ON CONFLICT IGNORE)";
        sqLiteDatabase.execSQL(createMapTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAMES[0]);
        onCreate(sqLiteDatabase);
    }

    public boolean addSong(String song_name, String artist_name, String url, String dateAdded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAMES[0][1], song_name);
        contentValues.put(COL_NAMES[0][2], artist_name);
        contentValues.put(COL_NAMES[0][3], url);
        contentValues.put(COL_NAMES[0][4], dateAdded);

        return db.insert(TABLE_NAMES[0], null, contentValues) > 0;
    }

    public boolean removeSong(String song_name, String artist_name, String url, String dateAdded) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAMES[0], COL_NAMES[0][1] + " = '" + song_name + "' AND " + COL_NAMES[0][2] + " = '" + artist_name + "' AND " + COL_NAMES[0][3] + " = '" + url + "' AND " + COL_NAMES[0][4] + " = '" + dateAdded + "'", null) > 0;
    }

    public boolean addTagg(String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAMES[1][1], tagg);

        return db.insert(TABLE_NAMES[1], null, contentValues) > 0;
    }

    public boolean removeTagg(String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAMES[1], COL_NAMES[1][1] + " = '" + tagg + "'", null) > 0 && removeTaggFromMap(tagg);
    }

    private boolean removeTaggFromMap(String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAMES[2], COL_NAMES[2][1] + " = '" + tagg + "'", null) > 0;
    }

    public boolean addSongTaggRelation(String song_name, String artist_name, String url, String dateAdded, String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT " + COL_NAMES[0][0] + " FROM " + TABLE_NAMES[0] + " WHERE " + COL_NAMES[0][1] + " = '" + song_name + "' AND " + COL_NAMES[0][2] + " = '" + artist_name + "' AND " + COL_NAMES[0][3] + " = '" + url + "' AND " + COL_NAMES[0][4] + " = '" + dateAdded + "'";
        Cursor data = db.rawQuery(q, null);

        int songID;

        if (data.moveToFirst()) {
            songID = data.getInt(0);
        } else { return false; }
        data.close();

        String qT = "SELECT " + COL_NAMES[1][0] + " FROM " + TABLE_NAMES[1] + " WHERE " + COL_NAMES[1][1] + " = '" + tagg + "'";
        Cursor dataT = db.rawQuery(qT, null);

        int taggID;

        if (dataT.moveToFirst()) {
            taggID = dataT.getInt(0);
        } else { return false; }
        dataT.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAMES[2][0], songID);
        contentValues.put(COL_NAMES[2][1], taggID);

        return db.insert(TABLE_NAMES[2], null, contentValues) > 0;
    }

    public boolean removeSongTaggRelation(String song_name, String artist_name, String url, String dateAdded, String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT " + COL_NAMES[0][0] + " FROM " + TABLE_NAMES[0] + " WHERE " + COL_NAMES[0][1] + " = '" + song_name + "' AND " + COL_NAMES[0][2] + " = '" + artist_name + "' AND " + COL_NAMES[0][3] + " = '" + url + "' AND " + COL_NAMES[0][4] + " = '" + dateAdded + "'";
        Cursor data = db.rawQuery(q, null);

        int songID;

        if (data.moveToFirst()) {
            songID = data.getInt(0);
        } else { return false; }
        data.close();

        String qT = "SELECT " + COL_NAMES[1][0] + " FROM " + TABLE_NAMES[1] + " WHERE " + COL_NAMES[1][1] + " = '" + tagg + "'";
        Cursor dataT = db.rawQuery(qT, null);

        int taggID;

        if (dataT.moveToFirst()) {
            taggID = dataT.getInt(0);
        } else { return false; }
        dataT.close();

        return db.delete(TABLE_NAMES[2], COL_NAMES[2][0] + " = '" + songID + "' AND " + COL_NAMES[2][1] + " = '" + taggID + "'", null) > 0;
    }

    public Cursor getSongs() {
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAMES[0] + " ORDER BY " + COL_NAMES[0][1] + " ASC";
        Cursor data = db.rawQuery(q, null);
        return data;
    }

    public Cursor getTaggs() {
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAMES[1] + " ORDER BY " + COL_NAMES[1][1] + " ASC";
        Cursor data = db.rawQuery(q, null);
        return data;
    }

    public Cursor getSongTaggRelation() {
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAMES[2];
        Cursor data = db.rawQuery(q, null);
        return data;
    }

    public ArrayList<String> getSongsRelatedTaggs(String song_name, String artist_name, String url,  String dateAdded) {
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT " + COL_NAMES[0][0] + " FROM " + TABLE_NAMES[0] + " WHERE " + COL_NAMES[0][1] + " = '" + song_name + "' AND " + COL_NAMES[0][2] + " = '" + artist_name + "' AND " + COL_NAMES[0][3] + " = '" + url + "' AND " + COL_NAMES[0][4] + " = '" + dateAdded + "'";
        Cursor data = db.rawQuery(q, null);

        int songID;

        if (data.moveToFirst()) {
            songID = data.getInt(0);
        } else { return new ArrayList<>(); }
        data.close();

        String q1 = "SELECT " + COL_NAMES[2][1] + " FROM " + TABLE_NAMES[2] + " WHERE " + COL_NAMES[2][0] + " = '" + songID + "'";
        Cursor data1 = db.rawQuery(q1, null);

        ArrayList<Integer> taggIDs = new ArrayList<>();

        if (!data1.moveToFirst()) {
            return new ArrayList<>();
        }

        do {
            taggIDs.add(data1.getInt(0));
        } while (data1.moveToNext());
        data1.close();

        String implodedArray = TextUtils.join(",", taggIDs);

        String q2 = "SELECT " + COL_NAMES[1][1] + " FROM " + TABLE_NAMES[1] + " WHERE " + COL_NAMES[1][0] + " IN (" + implodedArray + ")";
        Cursor data2 = db.rawQuery(q2, null);

        if (!data2.moveToFirst()) {
            return new ArrayList<>();
        }

        ArrayList<String> relatedTaggs = new ArrayList<>();

        do {
            relatedTaggs.add(data2.getString(0));
        } while (data2.moveToNext());
        data2.close();

        return relatedTaggs;
    }

    public ArrayList<String[]> getTaggsRelatedSongs(String tagg) {
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT " + COL_NAMES[1][0] + " FROM " + TABLE_NAMES[1] + " WHERE " + COL_NAMES[1][1] + " = '" + tagg + "'";
        Cursor data = db.rawQuery(q, null);

        int taggID;

        if (data.moveToFirst()) {
            taggID = data.getInt(0);
        } else { return new ArrayList<>(); }
        data.close();

        String q1 = "SELECT " + COL_NAMES[2][0] + " FROM " + TABLE_NAMES[2] + " WHERE " + COL_NAMES[2][1] + " = '" + taggID + "'";
        Cursor data1 = db.rawQuery(q1, null);

        ArrayList<Integer> songIDs = new ArrayList<>();

        if (!data1.moveToFirst()) {
            return new ArrayList<>();
        }

        do {
            songIDs.add(data1.getInt(0));
        } while (data1.moveToNext());
        data1.close();

        String implodedArray = TextUtils.join(",", songIDs);

        String q2 = "SELECT * FROM " + TABLE_NAMES[0] + " WHERE " + COL_NAMES[0][0] + " IN (" + implodedArray + ")";
        Cursor data2 = db.rawQuery(q2, null);

        if (!data2.moveToFirst()) {
            return new ArrayList<>();
        }

        ArrayList<String[]> relatedSongs = new ArrayList<>();

        do {
            relatedSongs.add(new String[] {data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4)});
        } while (data2.moveToNext());
        data2.close();

        return relatedSongs;
    }
}

package com.groupit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "GroupIt";
    private static final String TABLE_CONTACTS = "groups";
    private static final String KEY_ID = "groupID";
    private static final String KEY_TIME = "displayname";
    private static final String KEY_MSG = "groupcode";


    public GroupDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + "ID INTEGER PRIMARY KEY   AUTOINCREMENT," + KEY_ID + " TEXT," + KEY_TIME + " TEXT,"
                + KEY_MSG + " TEXT" + ")";

        String CREATE_DEFAULT_GROUP = "REPLACE INTO " + TABLE_CONTACTS + " (displayname, groupcode) VALUES ('GroupIt', 'GroupIt')";
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_DEFAULT_GROUP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    void addGroup(String group, String code) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, group);
        values.put(KEY_MSG, code);

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    public HashMap<String, String> getGroups() {
        HashMap<String, String> list = new HashMap<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                list.put(cursor.getString(cursor.getColumnIndex(KEY_TIME)),cursor.getString(cursor.getColumnIndex(KEY_MSG)));
            } while (cursor.moveToNext());
        }
        db.close();
        return list;
    }

    public String idToDisplay(String code) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        String temp = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex("groupcode")).equals(code)) {
                    temp =  cursor.getString(cursor.getColumnIndex("displayname"));
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return temp;
    }

    public void deleteGroup(String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, "groupcode = ?", new String[] { code });
        db.close();
    }

    public boolean groupExists(String code) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_MSG + " = ? ORDER BY " + KEY_MSG + " ASC";
        boolean exists = false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {code});

        if (cursor.getCount() > 0) {
            exists = true;
        }
        db.close();
        return exists;
    }
}

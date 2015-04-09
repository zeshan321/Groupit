package com.groupit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GroupIt";
    private static final String TABLE_CONTACTS = "messages";
    private static final String KEY_ID = "groupID";
    private static final String KEY_TIME = "time";
    private static final String KEY_MSG = "message";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + "ID INTEGER PRIMARY KEY   AUTOINCREMENT," + KEY_ID + " TEXT," + KEY_TIME + " TEXT,"
                + KEY_MSG + " TEXT" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

     void addMessage(String group, String message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, group);
        values.put(KEY_MSG, message);

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
     }

    public List<String> getMessages(int limit) {
        List<String> list = new ArrayList<>();

        try {
            String selectQuery = "SELECT * FROM ( SELECT * FROM " + TABLE_CONTACTS + " WHERE groupID =? ORDER BY ID DESC LIMIT " + limit + " ) sub ORDER BY id ASC";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, new String[] {MessageActivity.currentGroup});

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(cursor.getColumnIndex("message"));
                    list.add(name);
                    cursor.moveToNext();
                }
            }

            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            return list;
        }
        return list;
    }

    public void deleteMessage(String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, "message = ?", new String[] { json });
        db.close();
    }

    public int getCount() {
        String countQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE groupID=?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[] {MessageActivity.currentGroup});

        int cnt = cursor.getCount();

        cursor.close();
        db.close();
        return cnt;
    }
}

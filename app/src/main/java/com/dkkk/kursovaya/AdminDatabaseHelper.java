package com.dkkk.kursovaya;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class AdminDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "admins.db";
    private static final int DB_VERSION = 1;

    public AdminDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE admins (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT, password TEXT);");

        insertAdmin(db, "admin1", "pass1");
        insertAdmin(db, "admin2", "pass2");
        insertAdmin(db, "admin3", "pass3");
        insertAdmin(db, "admin4", "pass4");
        insertAdmin(db, "admin5", "pass5");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void insertAdmin(SQLiteDatabase db, String login, String password) {
        ContentValues values = new ContentValues();
        values.put("login", login);
        values.put("password", password);
        db.insert("admins", null, values);
    }

    public boolean checkAdmin(String login, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM admins WHERE login = ? AND password = ?", new String[]{login, password});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}


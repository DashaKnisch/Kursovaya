package com.dkkk.kursovaya;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SessionDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sessions.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "sessions";
    private static final String COL_ID = "id";
    private static final String COL_MOVIE_NAME = "movie_name";
    private static final String COL_DATE = "session_date";
    private static final String COL_TIME = "session_time";
    private static final String COL_HALL = "hall_number";

    public SessionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_MOVIE_NAME + " TEXT," +
                COL_DATE + " TEXT," +
                COL_TIME + " TEXT," +
                COL_HALL + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление сеанса
    public boolean addSession(Session session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MOVIE_NAME, session.getMovieName());
        values.put(COL_DATE, session.getSessionDate());
        values.put(COL_TIME, session.getSessionTime());
        values.put(COL_HALL, session.getHallNumber());

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    // Получение всех сеансов
    public ArrayList<Session> getAllSessions() {
        ArrayList<Session> sessionsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                Session session = new Session(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MOVIE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_HALL))
                );
                sessionsList.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sessionsList;
    }

    public boolean deleteSessionById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

}

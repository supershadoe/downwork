package dev.shadoe.downwork;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public class DownworkApp extends Application {
    private SQLiteDatabase db;
    private SharedPreferences prefs;
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        db = openOrCreateDatabase("DownworkDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Users(uid INTEGER, mail_id VARCHAR(100), pwd VARCHAR(100), user_name VARCHAR(100), user_type INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Skills(uid INTEGER, skill VARCHAR(100))");
        db.execSQL("CREATE TABLE IF NOT EXISTS Services(uid INTEGER, service_name VARCHAR(100), service_desc VARCHAR(300), rate DECIMAL(10, 2))");
        db.execSQL("CREATE TABLE IF NOT EXISTS Portfolio(uid INTEGER, about VARCHAR(2000), star_rating INTEGER)");
        prefs = getSharedPreferences("DownworkPrefs", Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(db);
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }
}
package com.example.iotapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SSHServerDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static class InstanceHolder {
        private static Context mContext;
        private static final SSHServerDbHelper instance = new SSHServerDbHelper(mContext.getApplicationContext());
        private InstanceHolder(Context context)
        {
            mContext = context;
        }
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SSHServerContract.SSHServer.TABLE_NAME + " (" +
                    SSHServerContract.SSHServer._ID + " INTEGER PRIMARY KEY," +
                    SSHServerContract.SSHServer.COLUMN_NAME_TITLE + " TEXT UNIQUE," +
                    SSHServerContract.SSHServer.COLUMN_NAME_USERNAME + " TEXT," +
                    SSHServerContract.SSHServer.COLUMN_NAME_IP + " TEXT," +
                    SSHServerContract.SSHServer.COLUMN_NAME_PORT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SSHServerContract.SSHServer.TABLE_NAME;

    private static SSHServerDbHelper getInstance(Context context) {
        final InstanceHolder instanceHolder = new InstanceHolder(context);
        return instanceHolder.instance;
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SSHServers.db";

    public SSHServerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
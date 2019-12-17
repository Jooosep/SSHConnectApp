package com.example.iotapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseProcessor {
    private SQLiteDatabase mDatabase;
    private SSHServerDbHelper mSQLHelper;
    private Context mContext;

    public DatabaseProcessor(Context context) {
        mContext = context;
        mSQLHelper = new SSHServerDbHelper(mContext);
    }

    public void open() throws SQLException {
        mDatabase = mSQLHelper.getWritableDatabase();
    }

    public void close() {
        mDatabase.close();
        mSQLHelper.close();
    }

    public void insertSomethingIntoDb(ContentValues values) {
        mDatabase.insertOrThrow(SSHServerContract.SSHServer.TABLE_NAME, null, values);
    }
    public void update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        mDatabase.update(table, values, whereClause, whereArgs);
    }
    public void delete(String serverName)
    {
        mDatabase.delete(SSHServerContract.SSHServer.TABLE_NAME, SSHServerContract.SSHServer.COLUMN_NAME_TITLE + "=?", new String[]{serverName});
    }
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        Cursor cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        return cursor;
    }
}

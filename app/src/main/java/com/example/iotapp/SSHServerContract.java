package com.example.iotapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class SSHServerContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SSHServerContract() {}

    /* Inner class that defines the table contents */
    public static class SSHServer implements BaseColumns {
        public static final String TABLE_NAME = "sshserver";
        public static final String COLUMN_NAME_TITLE = "name";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_IP = "ip";
        public static final String COLUMN_NAME_PORT = "port";
    }


}

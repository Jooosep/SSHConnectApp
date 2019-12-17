package com.example.iotapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    String mServerName = "";
    Button mSaveBtn;
    Button mLoadBtn;
    Button mConnectBtn;
    DatabaseProcessor mDbProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        mDbProcessor = new DatabaseProcessor(getApplicationContext());
        mDbProcessor.open();
        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mLoadBtn = (Button) findViewById(R.id.load_btn);
        mConnectBtn = (Button) findViewById(R.id.connect_btn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            //start execution of ssh commands
            @Override
            public void onClick(final View v) {

                final Pair<String[], String> fields = getTextFields(true);
                if (fields.first == null) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            snackBarMessage(fields.second, 20000);
                            return null;
                        }
                    }.execute();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Name the server: ");

                    // Set up the input
                    final EditText input = new EditText(v.getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            mServerName = input.getText().toString();

                            if (!mServerName.equals("")) {
                                Log.wtf(TAG, "serverName wasnt empty");
                                Log.wtf(TAG, "name: " +  mServerName);
                                // Create a new map of values, where column names are the keys
                                ContentValues values = new ContentValues();
                                Log.wtf(TAG, "serverName: " + mServerName);
                                values.put(SSHServerContract.SSHServer.COLUMN_NAME_TITLE, mServerName);
                                values.put(SSHServerContract.SSHServer.COLUMN_NAME_USERNAME, fields.first[0]);
                                values.put(SSHServerContract.SSHServer.COLUMN_NAME_IP, fields.first[2]);
                                values.put(SSHServerContract.SSHServer.COLUMN_NAME_PORT, fields.first[3]);

                                // Insert the new row, returning the primary key value of the new row
                                try {
                                    mDbProcessor.insertSomethingIntoDb(values);
                                } catch (SQLiteConstraintException e) {
                                    Log.wtf(TAG, e.toString());
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(v.getContext());
                                    builder2.setTitle("Name already exists");

                                    builder2.setPositiveButton("replace", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog2, int which) {
                                            // define the new value you want
                                            ContentValues newValues = new ContentValues();
                                            newValues.put(SSHServerContract.SSHServer.COLUMN_NAME_TITLE, mServerName);
                                            newValues.put(SSHServerContract.SSHServer.COLUMN_NAME_USERNAME, fields.first[0]);
                                            newValues.put(SSHServerContract.SSHServer.COLUMN_NAME_IP, fields.first[2]);
                                            newValues.put(SSHServerContract.SSHServer.COLUMN_NAME_PORT, fields.first[3]);
                                            String whereClause = SSHServerContract.SSHServer.COLUMN_NAME_TITLE + " == ?";
                                            String[] whereArgs = new String[]{
                                                    mServerName
                                            };
                                            mDbProcessor.update(SSHServerContract.SSHServer.TABLE_NAME, newValues, whereClause, whereArgs);
                                            dialog2.cancel();
                                            dialog.cancel();
                                        }
                                    });
                                    builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog2, int which) {
                                            dialog2.cancel();
                                        }
                                    });
                                    builder2.show();
                                }
                            }

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
            }
        });
        mLoadBtn.setOnClickListener(new View.OnClickListener() {
            //start execution of ssh commands
            @Override
            public void onClick(final View v){


                String[] projection = {SSHServerContract.SSHServer.COLUMN_NAME_TITLE};
                Cursor cursor = mDbProcessor.query(SSHServerContract.SSHServer.TABLE_NAME, projection, null, null, null, null, null, null);
                //Log.wtf(TAG, "cursor getCount: " +  Integer.toString(cursor.getCount()));
                final ArrayList<String> serverNames = new ArrayList<String>();
                while(cursor.moveToNext()) {
                    String serverName = cursor.getString(0);
                    serverNames.add(serverName);
                }
                cursor.close();

                if(!serverNames.isEmpty()) {

                    final FragmentManager fm = getSupportFragmentManager();
                    final String tag = "LOAD_LIST";

                    RecyclerViewClickListener clickListener = new RecyclerViewClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            String[] projection = {SSHServerContract.SSHServer.COLUMN_NAME_USERNAME,
                                    SSHServerContract.SSHServer.COLUMN_NAME_IP,
                                    SSHServerContract.SSHServer.COLUMN_NAME_PORT};
                            String selection = SSHServerContract.SSHServer.COLUMN_NAME_TITLE + " = ?";
                            String[] selectionArgs = {(String) serverNames.get(position)};
                            Cursor cursor = mDbProcessor.query(SSHServerContract.SSHServer.TABLE_NAME, projection, selection, selectionArgs, null, null, null, null);
                            String[] columnNames = cursor.getColumnNames();
                            //Log.wtf(TAG,"columnName at 0: " + Integer.toString(columnNames.length));
                            String[] fields = new String[3];
                            Integer it = 0;
                            if(cursor.moveToFirst()) {
                                for (String str : columnNames) {
                                    Log.wtf(TAG, str);
                                    Log.wtf(TAG, Integer.toString(cursor.getColumnIndex(str)));
                                    Integer index = cursor.getColumnIndex(str);
                                    fields[it] = cursor.getString(index);
                                    it++;
                                }
                                loadTextFields(fields);
                            }
                            Fragment frag = fm.findFragmentByTag(tag);
                            if (frag != null) {
                                DialogFragment df = (DialogFragment) frag;
                                df.dismiss();
                            }
                        }
                        @Override
                        public boolean onLongClick(View view, final int position) {


                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("Remove server?");


                            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String serverName = serverNames.get(position);
                                    mDbProcessor.delete(serverName);
                                    dialog.dismiss();
                                }});

                            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                });

                            builder.show();
                            return true;
                        }
                    };

                    LoadListAdapter adapter = new LoadListAdapter(serverNames, clickListener);

                    final LoadListFragment llf=new LoadListFragment(adapter, v.getContext());

                    llf.show(fm, tag);
                }
            }
        });

        mConnectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        Pair<String[], String> fields = getTextFields(false);
                        if (fields.first == null)
                        {
                            snackBarMessage(fields.second, 20000);
                        }
                        else {
                            try {
                                executeSSHcommand(fields.first[0], fields.first[1], fields.first[2], Integer.parseInt(fields.first[3]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

    public void onDestroy() {

        super.onDestroy();
        mDbProcessor.close();
    }

    void snackBarMessage(String message, Integer duration)
    {
        Snackbar.make(this.findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG)
                .setDuration(duration).setAction("Action", null).show();
        return;
    }

    Pair<String[], String> getTextFields(boolean excludePw)
    {
        String errorMessage;
        EditText mEditUser = findViewById(R.id.edit_user);
        EditText mEditPw = findViewById(R.id.edit_pw);
        EditText mEditIp = findViewById(R.id.edit_ip);
        EditText mEditPort = findViewById(R.id.edit_port);
        if (TextUtils.isEmpty(mEditUser.getText()))
        {
            errorMessage = "Username field is required";
        }
        else if (!excludePw && TextUtils.isEmpty(mEditPw.getText()))
        {
            errorMessage = "Password field is required";
        }
        else if (TextUtils.isEmpty(mEditIp.getText()))
        {
            errorMessage = "IP field is required";
        }
        else if (TextUtils.isEmpty(mEditPort.getText()))
        {
            errorMessage = "Port field is required";
        }
        else {
            return new Pair(new String[]{
                    mEditUser.getText().toString(),
                    mEditPw.getText().toString(),
                    mEditIp.getText().toString(),
                    mEditPort.getText().toString()
            }, null);
        }
        return new Pair(null, errorMessage);
    }
    public void loadTextFields(String[] fields)
    {
        EditText mEditUser = findViewById(R.id.edit_user);
        EditText mEditPw = findViewById(R.id.edit_pw);
        EditText mEditIp = findViewById(R.id.edit_ip);
        EditText mEditPort = findViewById(R.id.edit_port);

        mEditUser.setText(fields[0]);
        mEditPw.setText("");
        mEditIp.setText(fields[1]);
        mEditPort.setText(fields[2]);
        return;
    }
    public void executeSSHcommand(String user, String pw, String host, int port){

        try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(pw);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            Channel channel = (ChannelExec) session.openChannel("shell");
            channel.connect();
            try{
                Thread.sleep(1000);
            }catch(Exception ee){}
            channel.disconnect();
            session.disconnect();

            ConnectInfo sshConnect = new ConnectInfo(user, pw, host, port);
            Intent intent = new Intent(this, ShellActivity.class);
            EditText editText = (EditText) findViewById(R.id.shell_input);
            String[] connectInfo = {user, pw, host};
            intent.putExtra("CONNECT", sshConnect);
            startActivity(intent);
        }
        catch(Exception e)
        {
            Log.wtf(TAG, e.toString());
            Snackbar.make(this.findViewById(android.R.id.content),
                    "Error Connecting: " + e.getMessage(),
                    Snackbar.LENGTH_LONG)
                    .setDuration(20000).setAction("Action", null).show();
        }

    }
}

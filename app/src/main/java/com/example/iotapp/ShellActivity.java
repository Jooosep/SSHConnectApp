package com.example.iotapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ShellActivity extends AppCompatActivity {



    private static final String TAG = "ShellActivity";

    private ByteArrayOutputStream mOut;

    private String mInputString;
    private EditText mShellInput;
    TextView mShellTextView;
    Channel mChannel;
    Session mSession;
    private static Session session;
    private static ChannelShell channel;
    private static String username = "";
    private static String password = "";
    private static String hostname = "";
    private static AppState shellState = new AppState("");
    private static String lastCommand = "";
    private static List<String> savedCommands = new ArrayList<String>();


    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            SpannableStringBuilder textBuilder = new SpannableStringBuilder();
            SpannableString str1= colorString(shellState.getShellExchangeString());
            textBuilder.append(str1);
            mShellTextView.setText(textBuilder, TextView.BufferType.SPANNABLE);
            handler.postDelayed(periodicUpdate, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ssh_shell_activity);
        mInputString = "";

        mShellInput = findViewById(R.id.shell_input);
        mShellTextView = findViewById(R.id.shell_text);
        final Intent intent = getIntent();
        ConnectInfo sshConnect = intent.getParcelableExtra("CONNECT");
        username = sshConnect.getUser();
        password = sshConnect.getPw();
        hostname = sshConnect.getHost();


        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                List<String> commands = new ArrayList<String>();
                commands.add("");

                executeCommands(commands);
                return null;
            }
        }.execute(0);


        ImageButton sendToShell = (ImageButton) findViewById(R.id.send_to_shell);
        sendToShell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shellCommand();
            }
        });

        handler.post(periodicUpdate);

    }

    public SpannableString colorString(String str) {

        str = str.replaceAll("\\u001B\\[01;3\\dm", "");

        str = str.replaceAll("\\u001B\\[0m", "");

        SpannableString spanStr = new SpannableString(str);
        return spanStr;
    }

    public void shellCommand()
    {
        final String str = mShellInput.getText().toString();
        if(str.trim() != "") {
            new AsyncTask<Integer, Void, Void>() {
                @Override
                protected Void doInBackground(Integer... params) {
                    String[] words = str.split(" ");
                    if(words.length == 1)
                    {
                        if(words[0].equals("cd"))
                        {
                            savedCommands.clear();
                        }
                        if(words[0].equals("exit"))
                        {
                            close();
                            switchToMain("disconnected from session");
                        }

                    }
                    else if(str.equals("cd .."))
                    {
                        savedCommands.add(str);
                    }
                    else if (words.length > 1) {
                        if (words[0].equals("cd") && words.length == 2)
                        {
                            savedCommands.add(str);
                        }
                    }
                    List<String> commands = new ArrayList<String>();

                    if(savedCommands.size() > 0)
                    {
                        commands.addAll(savedCommands);
                    }
                    commands.add(str);
                    lastCommand = str;
                    executeCommands(commands);
                    return null;
                }

            }.execute();
        }
        mShellInput.getText().clear();

        mShellTextView.setText(shellState.getShellExchangeString());
        Integer i = 0;
    }
    public void switchToMain(String message)
    {
        // show the error in the UI
        Log.wtf(TAG, message);

        Intent newIntent = new Intent(this, MainActivity.class);
        String errorMessage = "Check WIFI or Server! Error : " + message;
        newIntent.putExtra("ERRORMESSAGE", errorMessage);
        startActivity(newIntent);

    }
    private static Session getSession(){
        if(session == null || !session.isConnected()){

            session = connect(hostname,username,password);
        }
        return session;
    }

    private static Channel getChannel(){
        if(channel == null || !channel.isConnected()){
            try{
                channel = (ChannelShell) getSession().openChannel("shell");
                channel.connect();

            }catch(Exception e){
                System.out.println("Error while opening channel: "+ e);

            }
        }
        return channel;
    }

    private static Session connect(String hostname, String username, String password){

        JSch jSch = new JSch();

        try {

            session = jSch.getSession(username, hostname, 22);
            session.setConfig("StrictHostKeyChecking", "no");

            session.setPassword(password);

            System.out.println("Connecting SSH to " + hostname + " - Please wait for few seconds... ");
            session.connect();
            System.out.println("Connected!");
        }catch(Exception e){
            System.out.println("An error occurred while connecting to "+hostname+": "+e);
        }

        return session;

    }

    private static void executeCommands(List<String> commands){

        try{
            Channel channel=getChannel();

            System.out.println("Sending commands...");
            sendCommands(channel, commands);

            readChannelOutput(channel);
            System.out.println("Finished sending commands!");

        }catch(Exception e){
            System.out.println("An error occurred during executeCommands: "+e);
        }
    }

    private static void sendCommands(Channel channel, List<String> commands){

        try{
            PrintStream out = new PrintStream(channel.getOutputStream());
            //out.println("#!/bin/bash");
            for(String command : commands){
                out.println(command);
            }
            out.println("exit");
            out.flush();
        }catch(Exception e){
            System.out.println("Error while sending commands: "+ e);
        }

    }

    private static void readChannelOutput(Channel channel){

        byte[] buffer = new byte[1024];

        try{
            InputStream in = channel.getInputStream();
            String line = "";
            while (true){
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    if (lastCommand.equals("ls")) {
                        line = line.trim().replaceAll(" +", " ");
                        line = line.replaceAll(" ", "\\\n");
                    }
                    System.out.println(line);
                    shellState.setValue(line);
                }

                if(line.contains("logout")){
                    break;
                }

                if (channel.isClosed()){
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee){}
            }
        }catch(Exception e){
            System.out.println("Error while reading channel output: "+ e);
        }

    }

    public static void close(){
        channel.disconnect();
        session.disconnect();
        System.out.println("Disconnected channel and session");
    }
}

package com.example.iotapp;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectInfo implements Parcelable {

    String User;
    String Pw;
    String Host;
    Integer Port;

    public ConnectInfo(String user, String pw, String host, int port)
    {
        User = user;
        Pw = pw;
        Host = host;
        Port = port;
    }
    public ConnectInfo(Parcel parcel)
    {
        this.User = parcel.readString();
        this.Pw = parcel.readString();
        this.Host = parcel.readString();
        this.Port = parcel.readInt();
    }

    public static final Creator<ConnectInfo> CREATOR = new Creator<ConnectInfo>() {
        @Override
        public ConnectInfo createFromParcel(Parcel in) {
            return new ConnectInfo(in);
        }

        @Override
        public ConnectInfo[] newArray(int size) {
            return new ConnectInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.User);
        parcel.writeString(this.Pw);
        parcel.writeString(this.Host);
        parcel.writeInt(this.Port);
    }

    public String getUser() {
        return User;
    }

    public String getPw() {
        return Pw;
    }

    public String getHost() {
        return Host;
    }

    public Integer getPort() {
        return Port;
    }
}

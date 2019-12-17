package com.example.iotapp;

import android.view.View;

public interface RecyclerViewClickListener {

    void onClick(View view, int position);
    boolean onLongClick(View view, int position);
}

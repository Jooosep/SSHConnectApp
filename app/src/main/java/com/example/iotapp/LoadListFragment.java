package com.example.iotapp;


import android.content.Context;

import android.view.View;

import androidx.annotation.Nullable;

import androidx.fragment.app.DialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.ViewGroup;

public class LoadListFragment extends DialogFragment {
    RecyclerView rv;
    LoadListAdapter mAdapter;
    Context mContext;

    public LoadListFragment(LoadListAdapter adapter, Context context)
    {
        mAdapter = adapter;
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView=inflater.inflate(R.layout.frag_layout,container);
        rv=(RecyclerView) rootView.findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        rv.setAdapter(mAdapter);

        this.getDialog().setTitle("Load server");

        return rootView;
    }

}

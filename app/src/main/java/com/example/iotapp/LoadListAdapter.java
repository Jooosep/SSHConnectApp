package com.example.iotapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class LoadListAdapter extends RecyclerView.Adapter<LoadListAdapter.ViewHolder> {

    private List<String> mServers;
    private RecyclerViewClickListener mListener;

    public LoadListAdapter(List<String> servers, RecyclerViewClickListener listener) {
        mListener = listener;
        mServers = servers;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public TextView serverNameTextView;
        public CardView serverNameCardView;
        public ViewHolder(View itemView, RecyclerViewClickListener listener) {

            super(itemView);
            mListener = listener;
            serverNameTextView = itemView.findViewById(R.id.server_name);
            serverNameCardView = itemView.findViewById(R.id.server_name_card);
            serverNameCardView.setOnClickListener(this);
            serverNameCardView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }
        @Override
        public boolean onLongClick(View view) {
            mListener.onLongClick(view, getAdapterPosition());
            return true;
        }

    }
    @Override
    public LoadListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_model,parent,false);

        ViewHolder viewHolder = new ViewHolder(view, mListener);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(LoadListAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        String serverName = mServers.get(position);

        TextView textView = viewHolder.serverNameTextView;
        textView.setText(serverName);

    }

    @Override
    public int getItemCount() {
        return mServers.size();
    }

}

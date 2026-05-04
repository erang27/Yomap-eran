package com.example.yomap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Predicate;

public class myAdapter<T> extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(int position);
    }
    public interface OnItemLongClickListener {
        void onClick(View view, int position);
    }

    private ArrayList<T> items;
    private OnItemClickListener listenerShort;
    private OnItemLongClickListener listenerLong;
    private Predicate<T> isManager;

    public myAdapter(ArrayList<T> items, OnItemClickListener listenerShort, OnItemLongClickListener listenerLong) {
        this.items = items;
        this.listenerShort = listenerShort;
        this.listenerLong = listenerLong;
        isManager = null;
    }
    public myAdapter(ArrayList<T> items, OnItemClickListener listenerShort, OnItemLongClickListener listenerLong, Predicate<T> isManager) {
        this(items, listenerShort, listenerLong);
        this.isManager=isManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new ViewHolder(view, listenerShort);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        T item = items.get(position);
        String displayText = item+""; //will work regardless of item type as long the item has tostring
        holder.textView.setText(displayText);
        if (isManager==null || !isManager.test(item)) holder.textView.setBackgroundColor(Color.WHITE);
        else holder.textView.setBackgroundColor(Color.CYAN);
        // Regular click
        holder.textView.setOnClickListener(v -> {
            if (listenerShort != null) {
                listenerShort.onClick(position);
            }
        });

        // Long click
        holder.textView.setOnLongClickListener(v -> {
            if (listenerLong != null) {
                listenerLong.onClick(v, position);
                return true; // consume the long click
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            textView = itemView.findViewById(android.R.id.text1);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(getAdapterPosition());
                }
            });
        }
    }
}

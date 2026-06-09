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

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        T item = items.get(position);
        String displayText = item+""; //will work regardless of item type as long the item has tostring
        holder.textView.setText(displayText);
        if (isManager==null || !isManager.test(item)) {
            holder.textView.setBackgroundColor(Color.TRANSPARENT);
        }
        else holder.textView.setBackgroundColor(Color.CYAN);
        holder.itemView.setOnClickListener(v -> {
            if (listenerShort != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listenerShort.onClick(pos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listenerLong != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listenerLong.onClick(v, pos);
                    return true;
                }
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
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
package com.example.hw9.functionalities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hw9.R;

public class CustomAdapter extends BaseAdapter {
    Context context;
    String stats[];
    LayoutInflater inflter;
    public CustomAdapter(Context applicationContext, String[] stats) {
        this.context = applicationContext;
        this.stats = stats;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return stats.length;
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.stat_line, null); // inflate the layout
        TextView stat = view.findViewById(R.id.stat_line);
        stat.setText(stats[i]);
        return view;
    }
}

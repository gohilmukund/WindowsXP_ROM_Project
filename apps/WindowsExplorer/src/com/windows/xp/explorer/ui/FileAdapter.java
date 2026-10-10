package com.windows.xp.explorer.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.windows.xp.explorer.R;
import com.windows.xp.explorer.model.FileItem;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    public static final int MODE_ICONS = 0;
    public static final int MODE_DETAILS = 1;

    private Context context;
    private List<FileItem> items = new ArrayList<FileItem>();
    private int viewMode = MODE_ICONS;
    private int selectedPosition = -1;
    private LayoutInflater inflater;

    public FileAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setItems(List<FileItem> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<FileItem>();
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setViewMode(int mode) {
        this.viewMode = mode;
        notifyDataSetChanged();
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public FileItem getSelectedItem() {
        if (selectedPosition >= 0 && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public FileItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileItem item = items.get(position);
        boolean isSelected = (position == selectedPosition);

        if (viewMode == MODE_ICONS) {
            ViewHolderGrid holder;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderGrid)) {
                convertView = inflater.inflate(R.layout.item_file_grid, parent, false);
                holder = new ViewHolderGrid();
                holder.icon = (ImageView) convertView.findViewById(R.id.img_file_icon);
                holder.name = (TextView) convertView.findViewById(R.id.tv_file_name);
                holder.sub = (TextView) convertView.findViewById(R.id.tv_file_sub);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderGrid) convertView.getTag();
            }

            holder.icon.setImageResource(item.getIconResId());
            holder.name.setText(item.getName());
            holder.sub.setText(item.getSubText());

            if (isSelected) {
                convertView.setBackgroundColor(Color.parseColor("#316AC5"));
                holder.name.setTextColor(Color.WHITE);
                holder.sub.setTextColor(Color.parseColor("#D0D0FF"));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.name.setTextColor(Color.BLACK);
                holder.sub.setTextColor(Color.parseColor("#777777"));
            }

            return convertView;
        } else {
            ViewHolderDetail holder;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderDetail)) {
                convertView = inflater.inflate(R.layout.item_file_detail, parent, false);
                holder = new ViewHolderDetail();
                holder.icon = (ImageView) convertView.findViewById(R.id.img_detail_icon);
                holder.name = (TextView) convertView.findViewById(R.id.tv_detail_name);
                holder.size = (TextView) convertView.findViewById(R.id.tv_detail_size);
                holder.type = (TextView) convertView.findViewById(R.id.tv_detail_type);
                holder.date = (TextView) convertView.findViewById(R.id.tv_detail_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderDetail) convertView.getTag();
            }

            holder.icon.setImageResource(item.getIconResId());
            holder.name.setText(item.getName());
            holder.size.setText(item.isDirectory() ? "" : FileItem.formatSize(item.getSize()));
            holder.type.setText(item.getTypeDescription());
            holder.date.setText(item.getFormattedDate());

            if (isSelected) {
                convertView.setBackgroundColor(Color.parseColor("#316AC5"));
                holder.name.setTextColor(Color.WHITE);
                holder.size.setTextColor(Color.WHITE);
                holder.type.setTextColor(Color.WHITE);
                holder.date.setTextColor(Color.WHITE);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.name.setTextColor(Color.BLACK);
                holder.size.setTextColor(Color.parseColor("#555555"));
                holder.type.setTextColor(Color.parseColor("#555555"));
                holder.date.setTextColor(Color.parseColor("#555555"));
            }

            return convertView;
        }
    }

    private static class ViewHolderGrid {
        ImageView icon;
        TextView name;
        TextView sub;
    }

    private static class ViewHolderDetail {
        ImageView icon;
        TextView name;
        TextView size;
        TextView type;
        TextView date;
    }
}

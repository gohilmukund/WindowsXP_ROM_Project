package com.windows.xp.explorer.ui;

import android.view.View;
import android.widget.TextView;
import com.windows.xp.explorer.R;
import com.windows.xp.explorer.model.FileItem;

public class TaskPaneController {

    public interface OnTaskPaneActionListener {
        void onNavigatePlace(String targetPath);
        void onNewFolderAction();
        void onViewSystemInfoAction();
        void onSearchAction();
    }

    private View rootView;
    private OnTaskPaneActionListener listener;

    private View bodySystemTasks;
    private View bodyOtherPlaces;
    private View bodyDetails;

    private TextView tvDetailName;
    private TextView tvDetailType;
    private TextView tvDetailInfo;

    public TaskPaneController(View rootView, final OnTaskPaneActionListener listener) {
        this.rootView = rootView;
        this.listener = listener;

        initViews();
        setupListeners();
    }

    private void initViews() {
        bodySystemTasks = rootView.findViewById(R.id.body_system_tasks);
        bodyOtherPlaces = rootView.findViewById(R.id.body_other_places);
        bodyDetails = rootView.findViewById(R.id.body_details);

        tvDetailName = (TextView) rootView.findViewById(R.id.tv_detail_name);
        tvDetailType = (TextView) rootView.findViewById(R.id.tv_detail_type);
        tvDetailInfo = (TextView) rootView.findViewById(R.id.tv_detail_info);
    }

    private void setupListeners() {
        // Collapsible headers
        rootView.findViewById(R.id.header_system_tasks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(bodySystemTasks);
            }
        });

        rootView.findViewById(R.id.header_other_places).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(bodyOtherPlaces);
            }
        });

        rootView.findViewById(R.id.header_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibility(bodyDetails);
            }
        });

        // Quick Places
        rootView.findViewById(R.id.place_my_computer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNavigatePlace("computer://");
            }
        });

        rootView.findViewById(R.id.place_my_documents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNavigatePlace("/sdcard/Documents");
            }
        });

        rootView.findViewById(R.id.place_my_pictures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNavigatePlace("/sdcard/Pictures");
            }
        });

        rootView.findViewById(R.id.place_my_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNavigatePlace("/sdcard/Music");
            }
        });

        rootView.findViewById(R.id.place_downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNavigatePlace("/sdcard/Download");
            }
        });

        // Actions
        rootView.findViewById(R.id.action_new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onNewFolderAction();
            }
        });

        rootView.findViewById(R.id.action_view_system_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onViewSystemInfoAction();
            }
        });

        rootView.findViewById(R.id.action_search_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onSearchAction();
            }
        });
    }

    private void toggleVisibility(View v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void updateDetails(FileItem item, String currentPathTitle) {
        if (item != null) {
            tvDetailName.setText(item.getName());
            tvDetailType.setText(item.getTypeDescription());
            if (item.isDirectory()) {
                tvDetailInfo.setText("Folder: " + item.getPath());
            } else {
                tvDetailInfo.setText("Size: " + FileItem.formatSize(item.getSize()) + "\nModified: " + item.getFormattedDate());
            }
        } else {
            tvDetailName.setText(currentPathTitle);
            tvDetailType.setText("System Folder");
            tvDetailInfo.setText("Select an item to view its details.");
        }
    }
}

package com.windows.xp.launcher.window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;
import com.windows.xp.launcher.explorer.fs.FileOperationsManager;
import com.windows.xp.launcher.explorer.fs.MimeTypeResolver;
import com.windows.xp.launcher.explorer.model.FileItem;
import com.windows.xp.launcher.explorer.ui.FileAdapter;
import com.windows.xp.launcher.explorer.ui.TaskPaneController;

import java.io.File;
import java.util.List;
import java.util.Stack;

public class ExplorerWindowView extends LinearLayout {

    public interface FileActionListener {
        void onOpenTextFile(File file);
    }

    public interface OnTitleChangeListener {
        void onTitleChange(String title, int iconRes);
    }

    private EditText etAddress;
    private GridView gridFiles;
    private ListView listFiles;
    private TextView tvEmpty;
    private View scrollTaskPane;
    private TextView tvStatusLeft;
    private TextView tvStatusRight;
    private ImageView imgTbBack;
    private ImageView imgTbForward;

    private FileOperationsManager fileManager;
    private FileAdapter fileAdapter;
    private TaskPaneController taskPaneController;

    private final Stack<String> backStack = new Stack<String>();
    private final Stack<String> forwardStack = new Stack<String>();
    private String currentPath = FileOperationsManager.VIRTUAL_MY_COMPUTER;

    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    private FileActionListener fileActionListener;
    private OnTitleChangeListener titleChangeListener;

    public ExplorerWindowView(Context context) {
        super(context);
        init(context);
    }

    public ExplorerWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setFileActionListener(FileActionListener l) {
        this.fileActionListener = l;
    }

    public void setOnTitleChangeListener(OnTitleChangeListener l) {
        this.titleChangeListener = l;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_explorer_window, this, true);

        fileManager = new FileOperationsManager();
        fileAdapter = new FileAdapter(context);

        initViews();
        setupToolbar();
        setupTaskPane();
        setupLists();

        navigateTo(FileOperationsManager.VIRTUAL_MY_COMPUTER, false);
    }

    private void initViews() {
        etAddress = (EditText) findViewById(R.id.et_address);
        gridFiles = (GridView) findViewById(R.id.grid_files);
        listFiles = (ListView) findViewById(R.id.list_files);
        tvEmpty = (TextView) findViewById(R.id.tv_empty);
        scrollTaskPane = findViewById(R.id.scroll_taskpane);

        tvStatusLeft = (TextView) findViewById(R.id.tv_status_left);
        tvStatusRight = (TextView) findViewById(R.id.tv_status_right);
        imgTbBack = (ImageView) findViewById(R.id.img_tb_back);
        imgTbForward = (ImageView) findViewById(R.id.img_tb_forward);
    }

    private void setupToolbar() {
        findViewById(R.id.btn_tb_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        findViewById(R.id.btn_tb_forward).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

        findViewById(R.id.btn_tb_up).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });

        findViewById(R.id.btn_tb_folders).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scrollTaskPane.getVisibility() == View.VISIBLE) {
                    scrollTaskPane.setVisibility(View.GONE);
                } else {
                    scrollTaskPane.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.btn_tb_views).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleViewMode();
            }
        });

        findViewById(R.id.btn_tb_newfolder).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                promptNewFolder();
            }
        });

        findViewById(R.id.btn_tb_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDelete();
            }
        });

        findViewById(R.id.btn_go).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etAddress.getText().toString().trim();
                resolveAndNavigateAddress(input);
            }
        });

        etAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String input = etAddress.getText().toString().trim();
                    resolveAndNavigateAddress(input);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupTaskPane() {
        taskPaneController = new TaskPaneController(this, new TaskPaneController.OnTaskPaneActionListener() {
            @Override
            public void onNavigatePlace(String path) {
                navigateTo(path, true);
            }

            @Override
            public void onNewFolderAction() {
                promptNewFolder();
            }

            @Override
            public void onSearchAction() {
                Toast.makeText(getContext(), "Search companion: All local drives indexed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewSystemInfoAction() {
                Toast.makeText(getContext(), "Opening System Properties...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLists() {
        gridFiles.setAdapter(fileAdapter);
        listFiles.setAdapter(fileAdapter);

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleItemClick(position);
            }
        };

        gridFiles.setOnItemClickListener(itemClickListener);
        listFiles.setOnItemClickListener(itemClickListener);
    }

    private void handleItemClick(int position) {
        long now = System.currentTimeMillis();
        boolean isDoubleClick = (position == lastClickPosition && (now - lastClickTime) < 600);
        lastClickTime = now;
        lastClickPosition = position;

        fileAdapter.setSelectedPosition(position);
        FileItem item = fileAdapter.getItem(position);
        taskPaneController.updateDetails(item, currentPath.equals(FileOperationsManager.VIRTUAL_MY_COMPUTER) ? "My Computer" : new File(currentPath).getName());

        // Single click immediately opens folders and drives for smooth tablet touch experience
        if (item.isDirectory() || item.isDrive() || isDoubleClick) {
            openItem(item);
        }
    }

    private void openItem(FileItem item) {
        if (item.isDirectory() || item.isDrive()) {
            navigateTo(item.getPath(), true);
        } else {
            launchFile(item);
        }
    }

    private void launchFile(FileItem item) {
        File file = new File(item.getPath());
        if (!file.exists()) {
            Toast.makeText(getContext(), "File does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ext = MimeTypeResolver.getExtension(file.getName()).toLowerCase();

        if (ext.equals("txt") || ext.equals("log") || ext.equals("cfg") || ext.equals("ini") || ext.equals("bat")) {
            if (fileActionListener != null) {
                fileActionListener.onOpenTextFile(file);
                return;
            }
        }

        Intent launchIntent = null;
        if (ext.equals("txt") || ext.equals("log") || ext.equals("cfg") || ext.equals("ini")) {
            launchIntent = new Intent();
            launchIntent.setClassName("com.windows.xp.notepad", "com.windows.xp.notepad.NotepadActivity");
            launchIntent.setDataAndType(Uri.fromFile(file), "text/plain");
        } else if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp") || ext.equals("gif")) {
            launchIntent = new Intent();
            launchIntent.setClassName("com.windows.xp.mspaint", "com.windows.xp.mspaint.MSPaintActivity");
            launchIntent.setDataAndType(Uri.fromFile(file), "image/*");
        } else if (ext.equals("mp3") || ext.equals("wav") || ext.equals("ogg") || ext.equals("m4a")) {
            launchIntent = new Intent();
            launchIntent.setClassName("com.windows.xp.wmp", "com.windows.xp.wmp.WMPActivity");
            launchIntent.setDataAndType(Uri.fromFile(file), "audio/*");
        } else if (ext.equals("apk")) {
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.setDataAndType(Uri.fromFile(file), MimeTypeResolver.getMimeType(file));
        }

        try {
            getContext().startActivity(launchIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No application found to open " + file.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    public void navigateTo(String path, boolean pushBack) {
        if (pushBack && currentPath != null && !currentPath.equals(path)) {
            backStack.push(currentPath);
            forwardStack.clear();
        }
        this.currentPath = path;
        updateNavButtonsState();

        if (FileOperationsManager.VIRTUAL_MY_COMPUTER.equals(path)) {
            loadMyComputer();
        } else {
            loadDirectory(new File(path));
        }
    }

    private void loadMyComputer() {
        if (titleChangeListener != null) {
            titleChangeListener.onTitleChange("My Computer", R.drawable.ic_my_computer);
        }
        etAddress.setText("My Computer");

        List<FileItem> drives = fileManager.getDrives();
        fileAdapter.setItems(drives);

        tvEmpty.setVisibility(View.GONE);
        tvStatusLeft.setText(drives.size() + " objects");

        String freeC = fileManager.formatSize(fileManager.getDriveFreeSpace("/sdcard"));
        tvStatusRight.setText("Free Space (C:): " + freeC);

        taskPaneController.updateMyComputerContext();
    }

    private void loadDirectory(File dir) {
        String title = dir.getName();
        if (title.isEmpty()) title = dir.getAbsolutePath();
        if (titleChangeListener != null) {
            titleChangeListener.onTitleChange(title, R.drawable.ic_folder);
        }

        String winPath = fileManager.toWindowsPath(dir.getAbsolutePath());
        etAddress.setText(winPath);

        List<FileItem> items = fileManager.getDirectoryContents(dir);
        fileAdapter.setItems(items);

        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        tvStatusLeft.setText(items.size() + " objects");
        long totalBytes = 0;
        for (FileItem item : items) {
            totalBytes += item.getSize();
        }
        tvStatusRight.setText("Folder Size: " + fileManager.formatSize(totalBytes));

        taskPaneController.updateFolderContext(dir);
    }

    private void goBack() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentPath);
            navigateTo(backStack.pop(), false);
        }
    }

    private void goForward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentPath);
            navigateTo(forwardStack.pop(), false);
        }
    }

    private void goUp() {
        if (FileOperationsManager.VIRTUAL_MY_COMPUTER.equals(currentPath)) {
            return;
        }

        File f = new File(currentPath);
        File parent = f.getParentFile();

        if (parent == null || currentPath.equals("/sdcard") || currentPath.equals("/storage/emulated/0") || currentPath.equals("/")) {
            navigateTo(FileOperationsManager.VIRTUAL_MY_COMPUTER, true);
        } else {
            navigateTo(parent.getAbsolutePath(), true);
        }
    }

    private void updateNavButtonsState() {
        imgTbBack.setAlpha(backStack.isEmpty() ? 0.35f : 1.0f);
        imgTbForward.setAlpha(forwardStack.isEmpty() ? 0.35f : 1.0f);
    }

    private void toggleViewMode() {
        if (gridFiles.getVisibility() == View.VISIBLE) {
            gridFiles.setVisibility(View.GONE);
            listFiles.setVisibility(View.VISIBLE);
            fileAdapter.setViewMode(FileAdapter.VIEW_MODE_DETAILS);
        } else {
            listFiles.setVisibility(View.GONE);
            gridFiles.setVisibility(View.VISIBLE);
            fileAdapter.setViewMode(FileAdapter.VIEW_MODE_TILES);
        }
    }

    private void resolveAndNavigateAddress(String input) {
        if (input.equalsIgnoreCase("My Computer") || input.equalsIgnoreCase("computer://")) {
            navigateTo(FileOperationsManager.VIRTUAL_MY_COMPUTER, true);
            return;
        }
        String resolved = fileManager.toAndroidPath(input);
        File target = new File(resolved);
        if (target.exists() && target.isDirectory()) {
            navigateTo(target.getAbsolutePath(), true);
        } else {
            Toast.makeText(getContext(), "Cannot find '" + input + "'. Check the spelling and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptNewFolder() {
        if (FileOperationsManager.VIRTUAL_MY_COMPUTER.equals(currentPath)) {
            Toast.makeText(getContext(), "Cannot create folders in My Computer.", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(getContext());
        input.setHint("New Folder");
        new AlertDialog.Builder(getContext())
                .setTitle("New Folder")
                .setMessage("Enter folder name:")
                .setView(input)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) name = "New Folder";
                        boolean ok = fileManager.createFolder(currentPath, name);
                        if (ok) {
                            navigateTo(currentPath, false);
                        } else {
                            Toast.makeText(getContext(), "Failed to create folder.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptDelete() {
        int pos = fileAdapter.getSelectedPosition();
        if (pos < 0 || pos >= fileAdapter.getCount()) {
            Toast.makeText(getContext(), "Please select an item to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        final FileItem item = fileAdapter.getItem(pos);
        if (item.isDrive()) {
            Toast.makeText(getContext(), "Cannot delete system drives.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm File Delete")
                .setMessage("Are you sure you want to send '" + item.getName() + "' to the Recycle Bin?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean ok = fileManager.deleteFileOrFolder(new File(item.getPath()));
                        if (ok) {
                            Toast.makeText(getContext(), "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                            navigateTo(currentPath, false);
                        } else {
                            Toast.makeText(getContext(), "Cannot delete file: Permission denied.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}

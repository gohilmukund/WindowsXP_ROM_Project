package com.windows.xp.explorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.windows.xp.explorer.fs.FileOperationsManager;
import com.windows.xp.explorer.fs.MimeTypeResolver;
import com.windows.xp.explorer.model.FileItem;
import com.windows.xp.explorer.ui.FileAdapter;
import com.windows.xp.explorer.ui.TaskPaneController;

import java.io.File;
import java.util.List;
import java.util.Stack;

public class ExplorerActivity extends Activity implements TaskPaneController.OnTaskPaneActionListener {

    private static final String ACTION_MINIMIZE = "com.windows.xp.ACTION_MINIMIZE";

    private FileOperationsManager fileManager;
    private FileAdapter fileAdapter;
    private TaskPaneController taskPaneController;

    private String currentPath = FileOperationsManager.VIRTUAL_MY_COMPUTER;
    private Stack<String> backStack = new Stack<String>();
    private Stack<String> forwardStack = new Stack<String>();

    private TextView tvWindowTitle;
    private ImageView imgWindowIcon;
    private EditText etAddress;
    private GridView gridFiles;
    private ListView listFiles;
    private TextView tvEmpty;
    private View scrollTaskPane;

    private TextView tvStatusLeft;
    private TextView tvStatusRight;
    private ImageView imgTbBack;
    private ImageView imgTbForward;

    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        fileManager = new FileOperationsManager();
        fileAdapter = new FileAdapter(this);

        initViews();
        setupToolbar();
        setupLists();

        taskPaneController = new TaskPaneController(findViewById(android.R.id.content), this);

        // Check incoming intent target path
        String initialPath = FileOperationsManager.VIRTUAL_MY_COMPUTER;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("path")) {
                initialPath = intent.getStringExtra("path");
            } else if (intent.getData() != null && intent.getData().getPath() != null) {
                initialPath = intent.getData().getPath();
            }
        }

        navigateTo(initialPath, false);
    }

    private void initViews() {
        tvWindowTitle = (TextView) findViewById(R.id.tv_window_title);
        imgWindowIcon = (ImageView) findViewById(R.id.img_window_icon);
        etAddress = (EditText) findViewById(R.id.et_address);
        gridFiles = (GridView) findViewById(R.id.grid_files);
        listFiles = (ListView) findViewById(R.id.list_files);
        tvEmpty = (TextView) findViewById(R.id.tv_empty);
        scrollTaskPane = findViewById(R.id.scroll_taskpane);

        tvStatusLeft = (TextView) findViewById(R.id.tv_status_left);
        tvStatusRight = (TextView) findViewById(R.id.tv_status_right);
        imgTbBack = (ImageView) findViewById(R.id.img_tb_back);
        imgTbForward = (ImageView) findViewById(R.id.img_tb_forward);

        // Title bar controls
        findViewById(R.id.btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ExplorerActivity.this, "Window Maximized", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupToolbar() {
        findViewById(R.id.btn_tb_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        findViewById(R.id.btn_tb_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

        findViewById(R.id.btn_tb_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });

        findViewById(R.id.btn_tb_folders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scrollTaskPane.getVisibility() == View.VISIBLE) {
                    scrollTaskPane.setVisibility(View.GONE);
                } else {
                    scrollTaskPane.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.btn_tb_views).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleViewMode();
            }
        });

        findViewById(R.id.btn_tb_newfolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptNewFolder();
            }
        });

        findViewById(R.id.btn_tb_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDelete();
            }
        });

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etAddress.getText().toString().trim();
                resolveAndNavigateAddress(input);
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
        boolean isDoubleClick = (position == lastClickPosition && (now - lastClickTime) < 500);
        lastClickTime = now;
        lastClickPosition = position;

        fileAdapter.setSelectedPosition(position);
        FileItem item = fileAdapter.getItem(position);
        taskPaneController.updateDetails(item, tvWindowTitle.getText().toString());

        if (isDoubleClick || item.isDirectory() || item.isDrive()) {
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
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ext = MimeTypeResolver.getExtension(file.getName()).toLowerCase();
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
        } else if (ext.equals("html") || ext.equals("htm")) {
            launchIntent = new Intent();
            launchIntent.setClassName("com.windows.xp.internetexplorer", "com.windows.xp.internetexplorer.BrowserActivity");
            launchIntent.setData(Uri.fromFile(file));
        } else if (ext.equals("apk")) {
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.setDataAndType(Uri.fromFile(file), MimeTypeResolver.getMimeType(file));
        }

        try {
            startActivity(launchIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No Windows program associated with " + ext.toUpperCase(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateTo(String path, boolean addToHistory) {
        if (addToHistory && currentPath != null) {
            backStack.push(currentPath);
            forwardStack.clear();
        }

        currentPath = path;
        List<FileItem> items = fileManager.scanDirectory(path);
        fileAdapter.setItems(items);

        updateNavUI(path, items.size());
    }

    private void updateNavUI(String path, int count) {
        boolean isComputer = (path == null || path.equals(FileOperationsManager.VIRTUAL_MY_COMPUTER));

        if (isComputer) {
            tvWindowTitle.setText(R.string.my_computer);
            imgWindowIcon.setImageResource(R.drawable.ic_my_computer);
            etAddress.setText("My Computer");
        } else {
            File f = new File(path);
            tvWindowTitle.setText(f.getName().isEmpty() ? path : f.getName());
            imgWindowIcon.setImageResource(MimeTypeResolver.getIconForFile(f));
            etAddress.setText(toWindowsPath(path));
        }

        tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        tvStatusLeft.setText(count + " object" + (count == 1 ? "" : "s"));
        tvStatusRight.setText(fileManager.getFreeSpaceSummary(path));

        imgTbBack.setAlpha(backStack.isEmpty() ? 0.4f : 1.0f);
        imgTbForward.setAlpha(forwardStack.isEmpty() ? 0.4f : 1.0f);

        taskPaneController.updateDetails(null, tvWindowTitle.getText().toString());
    }

    private String toWindowsPath(String unixPath) {
        if (unixPath.startsWith("/sdcard")) {
            return unixPath.replaceFirst("/sdcard", "C:").replace('/', '\\');
        }
        if (unixPath.startsWith("/storage/emulated/0")) {
            return unixPath.replaceFirst("/storage/emulated/0", "C:").replace('/', '\\');
        }
        if (unixPath.startsWith("/storage/extSdCard")) {
            return unixPath.replaceFirst("/storage/extSdCard", "D:").replace('/', '\\');
        }
        if (unixPath.startsWith("/system")) {
            return unixPath.replaceFirst("/system", "S:").replace('/', '\\');
        }
        return unixPath.replace('/', '\\');
    }

    private void resolveAndNavigateAddress(String input) {
        if (input.equalsIgnoreCase("My Computer") || input.equalsIgnoreCase("computer://")) {
            navigateTo(FileOperationsManager.VIRTUAL_MY_COMPUTER, true);
            return;
        }

        String unix = input.replace('\\', '/');
        if (unix.startsWith("C:/") || unix.equals("C:")) {
            unix = unix.replaceFirst("C:", "/sdcard");
        } else if (unix.startsWith("D:/") || unix.equals("D:")) {
            unix = unix.replaceFirst("D:", "/storage/extSdCard");
        } else if (unix.startsWith("S:/") || unix.equals("S:")) {
            unix = unix.replaceFirst("S:", "/system");
        }

        File target = new File(unix);
        if (target.exists() && target.isDirectory()) {
            navigateTo(target.getAbsolutePath(), true);
        } else {
            Toast.makeText(this, "Windows cannot find '" + input + "'.", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBack() {
        if (!backStack.isEmpty()) {
            String prev = backStack.pop();
            forwardStack.push(currentPath);
            navigateTo(prev, false);
        }
    }

    private void goForward() {
        if (!forwardStack.isEmpty()) {
            String next = forwardStack.pop();
            backStack.push(currentPath);
            navigateTo(next, false);
        }
    }

    private void goUp() {
        if (currentPath == null || currentPath.equals(FileOperationsManager.VIRTUAL_MY_COMPUTER)) {
            return;
        }

        File parent = new File(currentPath).getParentFile();
        if (parent == null || currentPath.equals("/sdcard") || currentPath.equals("/storage/emulated/0") ||
            currentPath.equals("/storage/extSdCard") || currentPath.equals("/system")) {
            navigateTo(FileOperationsManager.VIRTUAL_MY_COMPUTER, true);
        } else {
            navigateTo(parent.getAbsolutePath(), true);
        }
    }

    private void toggleViewMode() {
        if (fileAdapter.getViewMode() == FileAdapter.MODE_ICONS) {
            fileAdapter.setViewMode(FileAdapter.MODE_DETAILS);
            gridFiles.setVisibility(View.GONE);
            listFiles.setVisibility(View.VISIBLE);
            Toast.makeText(this, "View: Details", Toast.LENGTH_SHORT).show();
        } else {
            fileAdapter.setViewMode(FileAdapter.MODE_ICONS);
            listFiles.setVisibility(View.GONE);
            gridFiles.setVisibility(View.VISIBLE);
            Toast.makeText(this, "View: Icons", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptNewFolder() {
        if (currentPath.equals(FileOperationsManager.VIRTUAL_MY_COMPUTER)) {
            Toast.makeText(this, "Cannot create folders in My Computer.", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText et = new EditText(this);
        et.setText("New Folder");
        et.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("New Folder")
                .setMessage("Enter new folder name:")
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = et.getText().toString().trim();
                        if (!name.isEmpty()) {
                            boolean ok = fileManager.createFolder(currentPath, name);
                            if (ok) {
                                navigateTo(currentPath, false);
                            } else {
                                Toast.makeText(ExplorerActivity.this, "Unable to create folder.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptDelete() {
        final FileItem item = fileAdapter.getSelectedItem();
        if (item == null) {
            Toast.makeText(this, "Select an item to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (item.isDrive()) {
            Toast.makeText(this, "Cannot delete drive.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to send '" + item.getName() + "' to the Recycle Bin?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean ok = fileManager.deleteItem(item.getPath());
                        if (ok) {
                            Toast.makeText(ExplorerActivity.this, "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                            navigateTo(currentPath, false);
                        } else {
                            Toast.makeText(ExplorerActivity.this, "Failed to delete item.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void minimizeWindow() {
        Intent minIntent = new Intent(ACTION_MINIMIZE);
        minIntent.putExtra("package", getPackageName());
        sendBroadcast(minIntent);
        moveTaskToBack(true);
    }

    // TaskPaneController callbacks
    @Override
    public void onNavigatePlace(String targetPath) {
        navigateTo(targetPath, true);
    }

    @Override
    public void onNewFolderAction() {
        promptNewFolder();
    }

    @Override
    public void onViewSystemInfoAction() {
        new AlertDialog.Builder(this)
                .setTitle("System Properties")
                .setMessage("System:\n  Microsoft Windows XP Professional\n  Version 2002 Service Pack 3\n  Luna Custom ROM v1.1\n\nComputer:\n  Samsung Galaxy Tab 3 7.0\n  Marvell PXA986 Dual-Core 1.2 GHz\n  1.00 GB of RAM")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onSearchAction() {
        Toast.makeText(this, "Search Companion activated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (!backStack.isEmpty()) {
                goBack();
                return true;
            }
            if (!currentPath.equals(FileOperationsManager.VIRTUAL_MY_COMPUTER)) {
                goUp();
                return true;
            }
            minimizeWindow();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}

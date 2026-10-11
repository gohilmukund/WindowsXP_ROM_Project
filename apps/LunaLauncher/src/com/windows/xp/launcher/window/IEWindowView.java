package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.windows.xp.launcher.R;

public class IEWindowView extends LinearLayout {

    private static final String DEFAULT_URL = "https://www.google.com";

    private WebView webView;
    private EditText etAddress;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private ImageView btnForward;
    private ImageView btnStop;
    private ImageView btnRefresh;
    private ImageView btnHome;
    private ImageView btnGo;

    public interface OnTitleUpdateListener {
        void onTitleUpdated(String title);
    }
    private OnTitleUpdateListener titleListener;

    public IEWindowView(Context context) {
        super(context);
        init(context);
    }

    public void setOnTitleUpdateListener(OnTitleUpdateListener listener) {
        this.titleListener = listener;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFFECE9D8); // Authentic Windows XP dialog / toolbar gray

        // 1. Classic XP Menu Bar (File, Edit, View, Favorites, Tools, Help)
        LinearLayout menuBar = new LinearLayout(context);
        menuBar.setOrientation(HORIZONTAL);
        menuBar.setBackgroundColor(0xFFECE9D8);
        menuBar.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        String[] menus = new String[]{"File", "Edit", "View", "Favorites", "Tools", "Help"};
        for (String m : menus) {
            TextView tv = new TextView(context);
            tv.setText(m);
            tv.setTextSize(11);
            tv.setTextColor(0xFF000000);
            tv.setPadding(dpToPx(5), dpToPx(1), dpToPx(5), dpToPx(1));
            menuBar.addView(tv);
        }
        addView(menuBar);

        // Divider
        View div1 = new View(context);
        div1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)));
        div1.setBackgroundColor(0xFFD4D0C8);
        addView(div1);

        // 2. Navigation Toolbar (Back, Forward, Stop, Refresh, Home)
        LinearLayout toolBar = new LinearLayout(context);
        toolBar.setOrientation(HORIZONTAL);
        toolBar.setGravity(Gravity.CENTER_VERTICAL);
        toolBar.setBackgroundColor(0xFFECE9D8);
        toolBar.setPadding(dpToPx(4), dpToPx(3), dpToPx(4), dpToPx(3));

        btnBack = createToolButton(context, R.drawable.tb_back, "Back");
        btnForward = createToolButton(context, R.drawable.tb_forward, "Forward");
        btnStop = createToolButton(context, R.drawable.tb_stop, "Stop");
        btnRefresh = createToolButton(context, R.drawable.tb_refresh, "Refresh");
        btnHome = createToolButton(context, R.drawable.tb_home, "Home");

        toolBar.addView(btnBack);
        toolBar.addView(btnForward);
        toolBar.addView(btnStop);
        toolBar.addView(btnRefresh);
        toolBar.addView(btnHome);

        addView(toolBar);

        // Divider
        View div2 = new View(context);
        div2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)));
        div2.setBackgroundColor(0xFFD4D0C8);
        addView(div2);

        // 3. Address Bar
        LinearLayout addressBar = new LinearLayout(context);
        addressBar.setOrientation(HORIZONTAL);
        addressBar.setGravity(Gravity.CENTER_VERTICAL);
        addressBar.setBackgroundColor(0xFFECE9D8);
        addressBar.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));

        TextView tvAddressLabel = new TextView(context);
        tvAddressLabel.setText("Address ");
        tvAddressLabel.setTextColor(0xFF444444);
        tvAddressLabel.setTextSize(11);
        addressBar.addView(tvAddressLabel);

        etAddress = new EditText(context);
        LayoutParams etParams = new LayoutParams(0, dpToPx(24), 1.0f);
        etAddress.setLayoutParams(etParams);
        etAddress.setBackgroundColor(Color.WHITE);
        etAddress.setTextSize(12);
        etAddress.setTextColor(Color.BLACK);
        etAddress.setSingleLine(true);
        etAddress.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        etAddress.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.addView(etAddress);

        btnGo = new ImageView(context);
        int goSize = dpToPx(22);
        LayoutParams goParams = new LayoutParams(goSize, goSize);
        goParams.leftMargin = dpToPx(4);
        btnGo.setLayoutParams(goParams);
        btnGo.setImageResource(R.drawable.tb_go);
        btnGo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addressBar.addView(btnGo);

        addView(addressBar);

        // 4. Loading Progress Bar
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(3)));
        progressBar.setMax(100);
        progressBar.setVisibility(GONE);
        addView(progressBar);

        // 5. WebView Content
        webView = new WebView(context);
        LayoutParams webParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        webView.setLayoutParams(webParams);
        configureWebView();
        addView(webView);

        setupActions();

        // Load initial homepage
        loadUrl(DEFAULT_URL);
    }

    private ImageView createToolButton(Context context, int iconRes, String desc) {
        ImageView iv = new ImageView(context);
        int btnSize = dpToPx(24);
        LayoutParams lp = new LayoutParams(btnSize, btnSize);
        lp.rightMargin = dpToPx(4);
        iv.setLayoutParams(lp);
        iv.setImageResource(iconRes);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setContentDescription(desc);
        iv.setBackgroundResource(R.drawable.menu_item_selector);
        return iv;
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 5.1; rv:52.0) Gecko/20100101 Firefox/52.0");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(VISIBLE);
                etAddress.setText(url);
                updateNavButtons();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(GONE);
                updateNavButtons();
                String title = view.getTitle();
                if (title != null && !title.isEmpty() && titleListener != null) {
                    titleListener.onTitleUpdated(title + " - Microsoft Internet Explorer");
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title != null && !title.isEmpty() && titleListener != null) {
                    titleListener.onTitleUpdated(title + " - Microsoft Internet Explorer");
                }
            }
        });
    }

    private void setupActions() {
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) webView.goBack();
            }
        });

        btnForward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) webView.goForward();
            }
        });

        btnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
                progressBar.setVisibility(GONE);
            }
        });

        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        btnHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(DEFAULT_URL);
            }
        });

        btnGo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submitUrl();
            }
        });

        etAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    submitUrl();
                    return true;
                }
                return false;
            }
        });
    }

    private void submitUrl() {
        String url = etAddress.getText().toString().trim();
        if (url.isEmpty()) return;
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
            if (url.contains(".") && !url.contains(" ")) {
                url = "https://" + url;
            } else {
                url = "https://www.google.com/search?q=" + url;
            }
        }
        loadUrl(url);
    }

    public void loadUrl(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    private void updateNavButtons() {
        btnBack.setEnabled(webView.canGoBack());
        btnBack.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        btnForward.setEnabled(webView.canGoForward());
        btnForward.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }

    public void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}

package com.windows.xp.internetexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class IEActivity extends Activity {

    private WebView webView;
    private EditText urlInput;
    private TextView titleText;
    private TextView statusText;
    private ProgressBar progressBar;
    private ImageView globeIcon;
    private RotateAnimation globeAnimation;

    private static final String DEFAULT_HOME = "https://www.google.com";
    private static final String MODERN_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ie);

        initViews();
        setupGlobeAnimation();
        setupWebView();
        setupToolbarActions();
        setupWindowControls();

        // Handle incoming intent URL or default
        Intent intent = getIntent();
        String targetUrl = DEFAULT_HOME;
        if (intent != null && intent.getData() != null) {
            targetUrl = intent.getData().toString();
        }
        loadUrl(targetUrl);
    }

    private void initViews() {
        webView = (WebView) findViewById(R.id.ie_webview);
        urlInput = (EditText) findViewById(R.id.ie_url_input);
        titleText = (TextView) findViewById(R.id.ie_title_text);
        statusText = (TextView) findViewById(R.id.ie_status_text);
        progressBar = (ProgressBar) findViewById(R.id.ie_progress);
        globeIcon = (ImageView) findViewById(R.id.ie_globe);
    }

    private void setupGlobeAnimation() {
        globeAnimation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        globeAnimation.setInterpolator(new LinearInterpolator());
        globeAnimation.setDuration(1200);
        globeAnimation.setRepeatCount(Animation.INFINITE);
    }

    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUserAgentString(MODERN_UA);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlInput.setText(url);
                statusText.setText("Opening page " + url + "...");
                progressBar.setVisibility(View.VISIBLE);
                globeIcon.startAnimation(globeAnimation);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlInput.setText(url);
                statusText.setText("Done");
                progressBar.setVisibility(View.GONE);
                globeIcon.clearAnimation();
                String title = view.getTitle();
                if (title != null && !title.isEmpty()) {
                    titleText.setText(title + " - Microsoft Internet Explorer");
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Ignore TLS certificate mismatch/expiration on older KitKat root stores
                handler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                statusText.setText("Navigation canceled: " + description);
                progressBar.setVisibility(View.GONE);
                globeIcon.clearAnimation();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                    globeIcon.clearAnimation();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (title != null && !title.isEmpty()) {
                    titleText.setText(title + " - Microsoft Internet Explorer");
                }
            }
        });
    }

    private void setupToolbarActions() {
        findViewById(R.id.ie_tool_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) webView.goBack();
            }
        });

        findViewById(R.id.ie_tool_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) webView.goForward();
            }
        });

        findViewById(R.id.ie_tool_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
                globeIcon.clearAnimation();
                progressBar.setVisibility(View.GONE);
                statusText.setText("Stopped");
            }
        });

        findViewById(R.id.ie_tool_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        findViewById(R.id.ie_tool_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(DEFAULT_HOME);
            }
        });

        findViewById(R.id.ie_btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateFromInput();
            }
        });

        urlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    navigateFromInput();
                    return true;
                }
                return false;
            }
        });

        // Favorites menu
        findViewById(R.id.menu_favorites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFavoritesDialog();
            }
        });

        // File menu
        findViewById(R.id.menu_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDialog();
            }
        });
    }

    private void setupWindowControls() {
        findViewById(R.id.ie_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ie_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.ie_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(IEActivity.this, "Window already maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Hardware back button minimizes active window to taskbar
            minimizeWindow();
        }
    }

    private void minimizeWindow() {
        sendBroadcast(new Intent("com.windows.xp.ACTION_MINIMIZE"));
        moveTaskToBack(true);
    }

    private void navigateFromInput() {
        String input = urlInput.getText().toString().trim();
        if (input.isEmpty()) return;

        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            if (input.contains(".") && !input.contains(" ")) {
                input = "https://" + input;
            } else {
                input = "https://www.google.com/search?q=" + Uri.encode(input);
            }
        }
        loadUrl(input);
    }

    private void loadUrl(String url) {
        urlInput.setText(url);
        webView.loadUrl(url);
    }

    private void showFavoritesDialog() {
        final String[] titles = new String[]{"Instagram", "YouTube", "Google", "Wikipedia"};
        final String[] urls = new String[]{
                "https://www.instagram.com",
                "https://m.youtube.com",
                "https://www.google.com",
                "https://en.m.wikipedia.org"
        };

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Favorites");
        b.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadUrl(urls[which]);
            }
        });
        b.show();
    }

    private void showFileDialog() {
        final String[] options = new String[]{"New Window", "Open Home Page", "Close"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("File");
        b.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) loadUrl(DEFAULT_HOME);
                else if (which == 1) loadUrl(DEFAULT_HOME);
                else if (which == 2) finish();
            }
        });
        b.show();
    }
}

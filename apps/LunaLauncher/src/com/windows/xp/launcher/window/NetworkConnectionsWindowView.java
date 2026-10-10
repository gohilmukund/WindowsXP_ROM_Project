package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnectionsWindowView extends LinearLayout {

    public interface OnDialogCloseListener {
        void onClose();
    }

    private OnDialogCloseListener closeListener;
    private WifiManager wifiManager;

    private TextView tvStatus;
    private TextView tvSsid;
    private TextView tvSpeed;
    private TextView tvSignal;
    private TextView tvIpAddress;
    private Button btnToggleWifi;
    private LinearLayout layoutScanResults;
    private ListView listWifiAps;
    private WifiScanAdapter scanAdapter;

    public NetworkConnectionsWindowView(Context context) {
        super(context);
        init(context);
    }

    public NetworkConnectionsWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setOnDialogCloseListener(OnDialogCloseListener l) {
        this.closeListener = l;
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFFECE9D8);
        setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(10));

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Header: Wireless Network Connection Status
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dpToPx(8));

        ImageView icon = new ImageView(context);
        icon.setLayoutParams(new LayoutParams(dpToPx(40), dpToPx(40)));
        icon.setImageResource(R.drawable.ic_ie);
        header.addView(icon);

        LinearLayout titleText = new LinearLayout(context);
        titleText.setOrientation(VERTICAL);
        titleText.setPadding(dpToPx(10), 0, 0, 0);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Wireless Network Connection Status");
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextSize(13);
        tvTitle.setTextColor(Color.BLACK);
        titleText.addView(tvTitle);

        TextView tvSub = new TextView(context);
        tvSub.setText("Broadcom 802.11a/b/g/n Wireless Adapter");
        tvSub.setTextSize(10);
        tvSub.setTextColor(0xFF555555);
        titleText.addView(tvSub);

        header.addView(titleText);
        addView(header);

        // Group Box: Connection Properties
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(VERTICAL);
        box.setBackgroundColor(Color.WHITE);
        box.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        LayoutParams boxParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        box.setLayoutParams(boxParams);

        tvStatus = addPropertyRow(box, "Status:");
        tvSsid = addPropertyRow(box, "Network Name (SSID):");
        tvSpeed = addPropertyRow(box, "Speed:");
        tvSignal = addPropertyRow(box, "Signal Strength:");
        tvIpAddress = addPropertyRow(box, "IPv4 Address:");

        // Scan Results List Container (toggled on demand)
        layoutScanResults = new LinearLayout(context);
        layoutScanResults.setOrientation(VERTICAL);
        layoutScanResults.setVisibility(GONE);

        TextView tvAvailable = new TextView(context);
        tvAvailable.setText("\nAvailable Wireless Networks:");
        tvAvailable.setTypeface(null, Typeface.BOLD);
        tvAvailable.setTextSize(11);
        tvAvailable.setTextColor(0xFF00138C);
        layoutScanResults.addView(tvAvailable);

        listWifiAps = new ListView(context);
        listWifiAps.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(120)));
        scanAdapter = new WifiScanAdapter(context);
        listWifiAps.setAdapter(scanAdapter);
        layoutScanResults.addView(listWifiAps);

        box.addView(layoutScanResults);
        addView(box);

        // Buttons Bar: View Wireless Networks, Disable/Enable, Close
        LinearLayout btnBar = new LinearLayout(context);
        btnBar.setOrientation(HORIZONTAL);
        btnBar.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        btnBar.setPadding(0, dpToPx(8), 0, 0);

        Button btnViewNetworks = new Button(context);
        btnViewNetworks.setText("View Available Networks");
        btnViewNetworks.setTextSize(11);
        btnViewNetworks.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAvailableNetworks();
            }
        });
        btnBar.addView(btnViewNetworks);

        btnToggleWifi = new Button(context);
        btnToggleWifi.setTextSize(11);
        btnToggleWifi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleWifiState();
            }
        });
        LayoutParams toggleLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toggleLp.leftMargin = dpToPx(6);
        btnToggleWifi.setLayoutParams(toggleLp);
        btnBar.addView(btnToggleWifi);

        Button btnClose = new Button(context);
        btnClose.setText("Close");
        btnClose.setTextSize(11);
        LayoutParams closeLp = new LayoutParams(dpToPx(70), ViewGroup.LayoutParams.WRAP_CONTENT);
        closeLp.leftMargin = dpToPx(6);
        btnClose.setLayoutParams(closeLp);
        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (closeListener != null) closeListener.onClose();
            }
        });
        btnBar.addView(btnClose);

        addView(btnBar);

        refreshWifiInfo();
    }

    private TextView addPropertyRow(LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(HORIZONTAL);
        row.setPadding(0, dpToPx(3), 0, dpToPx(3));

        TextView tvLabel = new TextView(parent.getContext());
        tvLabel.setText(label);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setTextSize(11);
        tvLabel.setTextColor(Color.BLACK);
        tvLabel.setLayoutParams(new LayoutParams(dpToPx(160), ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(tvLabel);

        TextView tvVal = new TextView(parent.getContext());
        tvVal.setTextSize(11);
        tvVal.setTextColor(0xFF333333);
        row.addView(tvVal);

        parent.addView(row);
        return tvVal;
    }

    public void refreshWifiInfo() {
        if (wifiManager == null) return;

        boolean isEnabled = wifiManager.isWifiEnabled();
        btnToggleWifi.setText(isEnabled ? "Disable" : "Enable");

        if (!isEnabled) {
            tvStatus.setText("Disconnected (Wi-Fi Turned Off)");
            tvStatus.setTextColor(0xFF880000);
            tvSsid.setText("Not Connected");
            tvSpeed.setText("0 Mbps");
            tvSignal.setText("No Signal");
            tvIpAddress.setText("0.0.0.0");
            return;
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getNetworkId() != -1) {
            tvStatus.setText("Connected");
            tvStatus.setTextColor(0xFF006600);

            String ssid = info.getSSID();
            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            tvSsid.setText(ssid != null ? ssid : "Connected");

            tvSpeed.setText(info.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);

            int rssi = info.getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, 5);
            String bars = "■".substring(0, Math.max(1, level)) + " (Excellent)";
            tvSignal.setText(level + "/5 Bars " + bars);

            int ip = info.getIpAddress();
            String ipStr = String.format("%d.%d.%d.%d",
                    (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
            tvIpAddress.setText(ipStr);
        } else {
            tvStatus.setText("Scanning for networks...");
            tvStatus.setTextColor(0xFF888800);
            tvSsid.setText("Not Connected");
            tvSpeed.setText("0 Mbps");
            tvSignal.setText("No Signal");
            tvIpAddress.setText("0.0.0.0");
        }
    }

    private void toggleWifiState() {
        if (wifiManager != null) {
            boolean current = wifiManager.isWifiEnabled();
            wifiManager.setWifiEnabled(!current);
            Toast.makeText(getContext(), current ? "Disabling Wi-Fi..." : "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshWifiInfo();
                }
            }, 1200);
        }
    }

    private void toggleAvailableNetworks() {
        if (layoutScanResults.getVisibility() == VISIBLE) {
            layoutScanResults.setVisibility(GONE);
        } else {
            layoutScanResults.setVisibility(VISIBLE);
            if (wifiManager != null) {
                wifiManager.startScan();
                List<ScanResult> results = wifiManager.getScanResults();
                scanAdapter.setResults(results != null ? results : new ArrayList<ScanResult>());
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class WifiScanAdapter extends BaseAdapter {
        private final Context ctx;
        private final List<ScanResult> items = new ArrayList<ScanResult>();

        WifiScanAdapter(Context ctx) {
            this.ctx = ctx;
        }

        void setResults(List<ScanResult> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int pos) { return items.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View cv, ViewGroup p) {
            final ScanResult res = items.get(pos);
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(HORIZONTAL);
            row.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            row.setBackgroundResource(R.drawable.menu_item_selector);
            row.setClickable(true);
            row.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(ctx, "Connecting to " + res.SSID + " (" + res.capabilities + ")", Toast.LENGTH_SHORT).show();
                }
            });

            TextView tv = new TextView(ctx);
            int level = WifiManager.calculateSignalLevel(res.level, 5);
            tv.setText("📶 [" + level + "/5] " + res.SSID + " (" + (res.capabilities.contains("WPA") ? "Secure" : "Open") + ")");
            tv.setTextSize(11);
            tv.setTextColor(Color.BLACK);
            row.addView(tv);

            return row;
        }
    }
}

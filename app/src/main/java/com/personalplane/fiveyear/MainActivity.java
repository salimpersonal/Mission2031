package com.personalplane.fiveyear;

import android.app.Activity;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Fully offline WebView wrapper for the bundled personal planner. */
public class MainActivity extends Activity {
    private static final int REQUEST_BACKUP_EXPORT = 71;
    private static final int REQUEST_BACKUP_IMPORT = 72;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingBackup;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xff0b1020);
        getWindow().setNavigationBarColor(0xff0b1020);

        webView = new WebView(this);
        webView.setBackgroundColor(0xff0b1020);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadsImagesAutomatically(true);

        // Only the packaged local HTML is allowed inside the WebView.
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                return !isLocalAsset(u);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !url.startsWith("file:///android_asset/");
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/json");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent,"JSON ব্যাকআপ নির্বাচন করুন"), REQUEST_BACKUP_IMPORT);
                    return true;
                } catch (Exception err) {
                    fileCallback.onReceiveValue(null);
                    fileCallback = null;
                    Toast.makeText(MainActivity.this,"ফাইল নির্বাচন করা যাচ্ছে না",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            @Override public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("হ্যাঁ", (dialog, which) -> result.confirm())
                    .setNegativeButton("না", (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel())
                    .show();
                return true;
            }
        });
        webView.addJavascriptInterface(new BackupBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private boolean isLocalAsset(Uri uri) {
        return "file".equals(uri.getScheme()) && uri.toString().startsWith("file:///android_asset/");
    }

    public class BackupBridge {
        @JavascriptInterface public void saveBackup(String json) {
            runOnUiThread(() -> {
                pendingBackup = json;
                Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                save.addCategory(Intent.CATEGORY_OPENABLE);
                save.setType("application/json");
                save.putExtra(Intent.EXTRA_TITLE, "Mission2031-backup.json");
                try { startActivityForResult(save, REQUEST_BACKUP_EXPORT); }
                catch (Exception e) { pendingBackup = null; Toast.makeText(MainActivity.this,"ব্যাকআপ সেভ করা যাচ্ছে না",Toast.LENGTH_LONG).show(); }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_BACKUP_IMPORT) {
            if (fileCallback != null) {
                Uri[] uris = (resultCode == RESULT_OK && data != null && data.getData() != null)
                    ? new Uri[]{data.getData()} : null;
                fileCallback.onReceiveValue(uris);
                fileCallback = null;
            }
        } else if (requestCode == REQUEST_BACKUP_EXPORT) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingBackup != null) {
                try (OutputStream output = getContentResolver().openOutputStream(data.getData())) {
                    if (output == null) throw new Exception("Empty file stream");
                    output.write(pendingBackup.getBytes(StandardCharsets.UTF_8));
                    output.flush();
                    Toast.makeText(this,"ব্যাকআপ সংরক্ষিত হয়েছে",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this,"ব্যাকআপ সেভ ব্যর্থ",Toast.LENGTH_LONG).show();
                }
            }
            pendingBackup = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null) {
            webView.evaluateJavascript("(function(){if(document.querySelector('.modal')){closeModal();return 'handled'} if(ui.tab!=='home'){navigate('home');return 'handled'}return 'exit'})()", value -> {
                if ("\"exit\"".equals(value)) MainActivity.super.onBackPressed();
            });
        } else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}

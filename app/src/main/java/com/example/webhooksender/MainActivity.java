package com.example.webhooksender;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    private EditText etBaseUrl, etPushKey, etTitle, etContent;
    private TextView tvResult;
    private Button btnSend;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etBaseUrl = findViewById(R.id.etBaseUrl);
        etPushKey = findViewById(R.id.etPushKey);
        etTitle   = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvResult  = findViewById(R.id.tvResult);
        btnSend   = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWebhook();
            }
        });
    }

    private void sendWebhook() {
        final String baseUrl = etBaseUrl.getText().toString().trim();
        final String pushKey = etPushKey.getText().toString().trim();
        final String title   = etTitle.getText().toString().trim();
        final String content = etContent.getText().toString().trim();

        if (baseUrl.isEmpty()) {
            tvResult.setText("请先填写 URL");
            return;
        }

        btnSend.setEnabled(false);
        tvResult.setText("发送中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                HttpURLConnection conn = null;
                try {
                    String sep = baseUrl.contains("?") ? "&" : "?";
                    String fullUrl = baseUrl + sep
                            + "push_key=" + URLEncoder.encode(pushKey, "UTF-8")
                            + "&title=" + URLEncoder.encode(title, "UTF-8")
                            + "&content=" + URLEncoder.encode(content, "UTF-8");

                    conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    int code = conn.getResponseCode();
                    InputStream is = (code >= 200 && code < 400)
                            ? conn.getInputStream() : conn.getErrorStream();

                    StringBuilder sb = new StringBuilder();
                    if (is != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line).append('\n');
                        br.close();
                    }
                    result = "HTTP " + code + "\n" + sb.toString().trim();
                } catch (Exception e) {
                    result = "发送失败：" + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }

                final String finalResult = result;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(finalResult);
                        btnSend.setEnabled(true);
                    }
                });
            }
        }).start();
    }
}

package com.example.im2073;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SessionActivity extends AppCompatActivity {

    public static String currentSessionId = null;
    int userId;

    EditText etSessionId;
    Button btnJoin;
    String SERVLET_URL = "http://10.0.2.2:9999/clicker/getsession";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.session_main);

        userId = getIntent().getIntExtra("USER_ID", 0);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSessionId = findViewById(R.id.etSessionId);
        btnJoin = findViewById(R.id.btnJoin);

        btnJoin.setOnClickListener(v -> checkSession());
    }

    String getSession() {
        try {
            String sessionid = etSessionId.getText().toString();
            URL url = new URL(SERVLET_URL + "?session_id=" + sessionid);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = reader.readLine();
            reader.close();

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    void checkSession() {
        new Thread(() -> {
            try {
                String outcome = getSession();

                runOnUiThread(() -> {
                    if (outcome != null && outcome.equals("active")) {
                        currentSessionId = etSessionId.getText().toString();

                        android.content.Intent intent = new android.content.Intent(SessionActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                    } else {
                        etSessionId.setError("Invalid Session Code");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
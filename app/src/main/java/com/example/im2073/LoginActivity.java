package com.example.im2073;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    public static String SESSION_COOKIE = null;

    EditText etusername, etpassword;
    Button btnSubmit;

    String SERVLET_URL = "http://10.0.2.2:9999/clicker/loginstudent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etusername = findViewById(R.id.etusername);
        etpassword = findViewById(R.id.etpassword);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> checkPassword());
    }

    String getPassword() {
        try {
            // Get the username from the UI
            String username = etusername.getText().toString();

            // Append it as a query parameter
            URL url = new URL(SERVLET_URL + "?username=" + username);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String password = reader.readLine();
            reader.close();

            return password;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    void checkPassword() {
        String typedPassword = etpassword.getText().toString();

        new Thread(() -> {
            String result = getPassword(); // Now looks like "1,mypassword"

            runOnUiThread(() -> {
                if (result != null && result.contains(",")) {
                    // Split the response
                    String[] parts = result.split(",");
                    int realUserId = Integer.parseInt(parts[0]);
                    String passwordFromDb = parts[1];

                    if (passwordFromDb.equals(typedPassword)) {
                        Intent intent = new Intent(LoginActivity.this, SessionActivity.class);

                        // PASS THE INT, NOT THE USERNAME STRING
                        intent.putExtra("USER_ID", realUserId);

                        startActivity(intent);
                        finish();
                    } else {
                        etpassword.setError("Wrong password!");
                    }
                } else {
                    etusername.setError("User not found or Error");
                }
            });
        }).start();
    }
}
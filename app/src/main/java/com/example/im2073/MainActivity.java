package com.example.im2073;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class MainActivity extends AppCompatActivity {

    Button btnA, btnB, btnC, btnD;

    boolean hasAnswered = false;

    String currentQuestionID = "";
    int userId;

    String SERVLET_URL = "http://10.0.2.2:9999/clicker/select";
    String GET_URL = "http://10.0.2.2:9999/clicker/getquestion";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getIntExtra("USER_ID", 0);

        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);

        btnA.setOnClickListener(v -> handleClick(btnA, "A"));
        btnB.setOnClickListener(v -> handleClick(btnB, "B"));
        btnC.setOnClickListener(v -> handleClick(btnC, "C"));
        btnD.setOnClickListener(v -> handleClick(btnD, "D"));

        startChecking();
    }

    void handleClick(Button clickedButton, String answer){
        if(hasAnswered) return;

        hasAnswered = true;

        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);

        btnA.setAlpha(0.3f);
        btnB.setAlpha(0.3f);
        btnC.setAlpha(0.3f);
        btnD.setAlpha(0.3f);

        clickedButton.setAlpha(1f);

        sendAnswer(answer);
    }

    void sendAnswer(String answer) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVLET_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String sessionId = SessionActivity.currentSessionId;

                if (currentQuestionID == null || currentQuestionID.isEmpty()) {
                    Log.e("ERROR", "No question ID yet!");
                    return;
                }

                // ADDED: question_id=currentQuestionID
                String data = "choice=" + answer +
                        "&session=" + sessionId +
                        "&question_id=" + currentQuestionID;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    void resetButtons() {

        hasAnswered = false;

        btnA.setEnabled(true);
        btnB.setEnabled(true);
        btnC.setEnabled(true);
        btnD.setEnabled(true);

        btnA.setAlpha(1f);
        btnB.setAlpha(1f);
        btnC.setAlpha(1f);
        btnD.setAlpha(1f);
    }

    void startChecking() {

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);

                    String newQuestionId = getQuestionFromServer();

                    if (newQuestionId != null && !newQuestionId.equals(currentQuestionID)) {

                        currentQuestionID = newQuestionId;

                        runOnUiThread(() -> resetButtons());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    String getQuestionFromServer() {
        try {
            // Get the session ID from SessionActivity
            String sessionId = SessionActivity.currentSessionId;

            // Append it as a query parameter
            URL url = new URL(GET_URL + "?session_id=" + sessionId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String questionId = reader.readLine();
            reader.close();

            return questionId;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
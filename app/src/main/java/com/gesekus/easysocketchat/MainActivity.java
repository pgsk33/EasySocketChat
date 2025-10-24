package com.gesekus.easysocketchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import socketio.Socket;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private EditText editTextMessage;
    private TextView textView;
    private Button buttonSend;

    // ExecutorService to run network operations on a background thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler to post results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextMessage = findViewById(R.id.editTextText);
        textView = findViewById(R.id.textView);
        buttonSend = findViewById(R.id.button); // Make sure you have a Button with id "button" in your layout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect to the server in the background
        connectToServer();

        // Set up the button click listener
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
    }

    private void connectToServer() {
        executorService.execute(() -> {
            try {
                // Assuming socketio.Socket has this constructor.
                // Replace "10.5.148.212" with your server IP if it changes.
                socket = new Socket("192.168.178.32", 7777);
                if (socket.connect()) {
                    updateUI("Verbunden mit dem Server");
                }else{
                    updateUI("Verbindung fehlgeschlagen");
                }
            } catch (IOException e) {
                e.printStackTrace();
                updateUI("Verbindung fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    private void sendMessage(final String message) {
        executorService.execute(() -> {
            if (socket != null) {
                try {
                    socket.write(String.format("%s\n", message));
                    String answer = socket.readLine();

                    updateUI("Server Antwort: " + answer);

                    if (message.contains(" over") || (answer != null && answer.contains(" over"))) {
                        socket.close();
                        updateUI("Verbindung geschlossen.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    updateUI("Fehler bei der Kommunikation: " + e.getMessage());
                }
            } else {
                updateUI("Nicht verbunden.");
            }
        });
    }

    private void updateUI(final String text) {
        mainThreadHandler.post(() -> textView.setText(text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.execute(() -> {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }
}

package com.gesekus.easysocketchat;

import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import socketio.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String SAVED_PREFERENCES = "saved_preferences";
    private Socket socket;
    private EditText editTextMessage;
    private String ip;
    private String port;
    private Button buttonSend;
    private boolean connected = false;
    List<MessageItem> messageItemList = new ArrayList<>();
    RecyclerView recyclerView;
    MessageRecyclerAdapter adapter;
    SharedPreferences sharedPreferences;
    
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
        buttonSend = findViewById(R.id.button); // Make sure you have a Button with id "button" in your layout


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageRecyclerAdapter(messageItemList);
        recyclerView.setAdapter(adapter);

        editTextMessage.setText("0.0.0.0:7777");


        sharedPreferences = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        ip = sharedPreferences.getString("ip", "0.0.0.0:77 77");
        if (ip == null || ip.isEmpty()) {
            ip = "IP:Port";
        }
        editTextMessage.setText(ip);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the button click listener
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString();
            if (!message.isEmpty() && connected) {
                sendMessage(message);
            } else if (!connected) {
                try {
                    String[] data = message.split(":");
                    ip = data[0];
                    port = data[1];
                } catch (Exception e) {
                    updateUI("Please enter a valid IP and Port. They have to be separated by a :", true);
                }
                connectToServer();
            }
        });
    }

    private void connectToServer() {
        executorService.execute(() -> {
            try {
                // Assuming socketio.Socket has this constructor.
                // Replace "10.5.148.212" with your server IP if it changes.
                socket = new Socket(ip, 7777);
                if (socket.connect()) {
                    updateUI("Verbunden mit dem Server IP: " + ip + " on Port: " + port, true);
                    sharedPreferences.edit().putString("ip", ip).apply();
                    editTextMessage.setText("");
                    connected = true;
                }else{
                    updateUI("Verbindung fehlgeschlagen", true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                updateUI("Verbindung fehlgeschlagen: " + e.getMessage(), true);
            }
        });
    }

    private void sendMessage(final String message) {
        executorService.execute(() -> {
            if (socket != null) {
                try {
                    socket.write(String.format("%s\n", message));
                    updateUI(message, false);
                    editTextMessage.setText("");
                    String answer = socket.readLine();

                    updateUI(answer, true);

                    if (message.contains(" over") || (answer != null && answer.contains(" over"))) {
                        socket.close();
                        updateUI("Verbindung geschlossen.", true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    updateUI("Fehler bei der Kommunikation: " + e.getMessage(), true);
                    connected = false;
                }
            } else {
                updateUI("Nicht verbunden.", true);
                connected = false;
                connectToServer();
            }
        });
    }

    private void updateUI(final String text, boolean fromServer) {
        mainThreadHandler.post(() -> {
            messageItemList.add(new MessageItem(text, java.time.LocalDateTime.now(), fromServer));
            adapter.notifyItemInserted(messageItemList.size() - 1);
            recyclerView.scrollToPosition(messageItemList.size() - 1);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.execute(() -> {
            if (socket != null) {
                try {
                    socket.write(" over\n");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }
}

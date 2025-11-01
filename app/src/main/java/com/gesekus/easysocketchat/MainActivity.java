package com.gesekus.easysocketchat;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
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
import java.util.concurrent.atomic.AtomicBoolean;

import socketio.Socket;

public class MainActivity extends AppCompatActivity {
    public String name;
    public static final String SAVED_PREFERENCES = "saved_preferences";
    private Socket socket;
    private EditText editTextMessage;
    private boolean settingsAreVisible = true;
    private TextView textViewName;
    private String ip;
    private String port;
    private Button buttonSend;
    private Button settingsButton;
    private boolean connected = false;
    List<MessageItem> messageItemList = new ArrayList<>();
    RecyclerView recyclerView;
    MessageRecyclerAdapter adapter;
    MessageThread messageThread;
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
        textViewName = findViewById(R.id.nameText);
        buttonSend = findViewById(R.id.button); // Make sure you have a Button with id "button" in your layout
        settingsButton = findViewById(R.id.settingsButton);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageRecyclerAdapter(messageItemList);
        recyclerView.setAdapter(adapter);

        if (settingsAreVisible) {
            textViewName.setVisibility(View.VISIBLE);
        } else {
            textViewName.setVisibility(View.GONE);
        }

        editTextMessage.setText("0.0.0.0:7777");


        sharedPreferences = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        ip = sharedPreferences.getString("ip", "0.0.0.0:7777");
        name = sharedPreferences.getString("name", "Name");
        if (ip == null || ip.isEmpty()) {
            ip = "IP:Port";
        }
        textViewName.setText(name);
        editTextMessage.setText(ip);

        scanForOpenSockets();


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
        settingsButton.setOnClickListener(v -> {
            if (settingsAreVisible) {
                settingsAreVisible = false;
                textViewName.setVisibility(View.GONE);
                name = textViewName.getText().toString();
                sharedPreferences.edit().putString("name", name).apply();
            } else {
                settingsAreVisible = true;
                textViewName.setVisibility(View.VISIBLE);
            }
        });
    }

    public boolean scanForOpenSockets() {
        ExecutorService scannerExecutor = Executors.newFixedThreadPool(50);
        AtomicBoolean serverFound = new AtomicBoolean(false);

        executorService.execute(() -> {
            try {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                String localIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                if (localIp != null) {
                    if (localIp.startsWith("10.5.")) {
                        String subnetPrefix = "10.5";
                        for (int j = 0; j <= 255; j++) {
                            if (serverFound.get()) {
                                break;
                            }
                            for (int i = 1; i < 255; i++) {
                                if (serverFound.get()) {
                                    break;
                                }
                                final String host = subnetPrefix + "." + j + "." + i;
                                scannerExecutor.execute(() -> {
                                    if (serverFound.get()) {
                                        return;
                                    }
                                    try {
                                        Socket testSocket = new Socket(host, 7777);
                                        if (testSocket.connect()) {
                                            if (serverFound.compareAndSet(false, true)) {
                                                testSocket.close();
                                                ip = host;
                                                port = "7777";
                                                scannerExecutor.shutdownNow();
                                                connectToServer();
                                            } else {
                                                testSocket.close();
                                            }
                                        }
                                    } catch (IOException e) {
                                        // Port is not open or host not reachable, continue scanning
                                    }
                                });
                            }
                        }
                    } else {
                        String subnet = localIp.substring(0, localIp.lastIndexOf("."));
                        for (int i = 1; i < 255; i++) {
                            final String host = subnet + "." + i;
                            scannerExecutor.execute(() -> {
                                if (serverFound.get()) {
                                    return;
                                }
                                try {
                                    Socket testSocket = new Socket(host, 7777);
                                    if (testSocket.connect()) {
                                        if (serverFound.compareAndSet(false, true)) {
                                            testSocket.close();
                                            ip = host;
                                            port = "7777";
                                            scannerExecutor.shutdownNow();
                                            connectToServer();
                                        } else {
                                            testSocket.close();
                                        }
                                    }
                                } catch (IOException e) {
                                    // Port is not open or host not reachable, continue scanning
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                scannerExecutor.shutdown();
            }
        });
        return connected;
    }

    private void connectToServer() {
        executorService.execute(() -> {
            try {
                // Assuming socketio.Socket has this constructor.
                // Replace "10.5.148.212" with your server IP if it changes.
                socket = new Socket(ip, Integer.parseInt(port));
                if (socket.connect()) {
                    updateUI("Verbunden mit dem Server IP: " + ip + " on Port: " + port, true);
                    sharedPreferences.edit().putString("ip", ip + ":" + port).apply();
                    connected = true;

                    mainThreadHandler.post(() -> {
                        editTextMessage.setText("");
                        name = textViewName.getText().toString();
                        sharedPreferences.edit().putString("name", name).apply();
                        textViewName.setVisibility(View.GONE);
                    });

                    messageThread = new MessageThread();
                    messageThread.start();

                } else {
                    updateUI("Verbindung fehlgeschlagen", true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                updateUI("Verbindung fehlgeschlagen: " + e.getMessage(), true);
            }
        });
    }

    public class MessageThread extends Thread {
        @Override
        public void run() {
            while (true) {
                receiveMessage("");
            }
        }
    }

    public void receiveMessage(String answer) {
        try {
            answer = socket.readLine();
            String[] answerArray = answer.split("&&%%user=");
            String user = answerArray[1];
            answer = answerArray[0];
            updateUIChat(answer, true, user);
        } catch (IOException e) {
            Log.d("MainActivity", "Error receiving message: " + e.getMessage());
        }
        if (answer.contains(" over")) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.d("MainActivity", "Error closing socket: " + e.getMessage());
            }
            updateUI("Verbindung geschlossen.", true);
        }
    }

    private void sendMessage(final String message) {

        executorService.execute(() -> {
            if (socket != null) {
                try {
                    socket.write(String.format(message + "&&%%user=" + name + "\n"));
                    updateUIChat(message, false, name);
                    mainThreadHandler.post(() -> editTextMessage.setText(""));

                    if (message.contains(" over")) {
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

    private void updateUIChat(final String text, boolean fromServer, String user) {
        mainThreadHandler.post(() -> {
            messageItemList.add(new MessageItem(text, java.time.LocalDateTime.now(), fromServer, user));
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
                    messageThread.interrupt();
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

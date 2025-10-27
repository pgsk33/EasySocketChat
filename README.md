# Easy Socket Chat

A simple Android chat application that demonstrates client-server communication using sockets.

## Features

*   **Connect to a server:** Easily connect to any server by providing an IP address and port number.
*   **Real-time messaging:** Send and receive messages in real-time.
*   **Chat history:** Displays the conversation in a clean and scrollable view.
*   **Connection persistence:** Remembers the last used server address for quick reconnection.

## How to Use

1.  Launch the application.
2.  In the input field, enter the server's IP address and port number in the format `ip:port`.
3.  Tap the "Send" button to establish a connection.
4.  Once connected, you can start sending and receiving messages.
5.  To disconnect, send a message containing " over".

## Project Structure

*   `MainActivity.java`: The main entry point of the application, handling UI and socket connection logic.
*   `MessageRecyclerAdapter.java`: Manages the display of chat messages in the `RecyclerView`.
*   `MessageItem.java`: A data class representing a single chat message.

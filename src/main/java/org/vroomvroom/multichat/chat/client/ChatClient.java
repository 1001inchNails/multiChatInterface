package org.vroomvroom.multichat.chat.client;

import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private final int port;
    private final ClientTabController controller;
    private final int clientId;  // Tab number
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Thread receiveThread;
    private volatile boolean isConnected = false;

    public ChatClient( int port, ClientTabController controller, int clientId) {
        this.port = port;
        this.controller = controller;
        this.clientId = clientId;
    }

    public void start() {
        try {
            // hardcoded localhost
            socket = new Socket("localhost",port);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send client ID  to server as first message
            // this is all to avoid clients having a different number on tab title and inner client
            output.println("CLIENT_ID:" + clientId);

            isConnected = true;

            // start thread
            receiveThread = new Thread(this::receiveMessages);
            receiveThread.setDaemon(true);
            receiveThread.start();

            System.out.println("DEBUG: Sent CLIENT_ID:" + clientId + " to server");

        } catch (IOException e) {
            Platform.runLater(() ->
                    controller.displayMessage("SYSTEM", "Connection failed: " + e.getMessage())
            );
            disconnect();
        }
    }

    private void receiveMessages() {
        try {
            // message input loop
            String message;
            while (isConnected && (message = input.readLine()) != null) {
                final String msg = message;
                Platform.runLater(() -> controller.handleServerMessage(msg));
            }
        } catch (IOException e) {
            if (isConnected) {
                Platform.runLater(() ->
                        controller.displayMessage("SYSTEM", "Connection lost: " + e.getMessage())
                );
            }
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (output != null && isConnected) {
            output.println(message);
        }
    }

    public void disconnect() {
        isConnected = false;

        if (output != null) {
            output.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Well, shit");
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.out.println("Thats awkward...");
        }

        Platform.runLater(() -> {
            controller.displayMessage("SYSTEM", "Disconnected from server");
            controller.updateStatus("Disconnected", "red");
        });
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int getClientId() {
        return clientId;
    }
}
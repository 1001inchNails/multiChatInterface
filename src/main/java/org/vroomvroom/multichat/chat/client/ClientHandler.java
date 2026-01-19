package org.vroomvroom.multichat.chat.client;

import org.vroomvroom.multichat.chat.server.ServerTabController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private int clientId = 0;
    private final CopyOnWriteArrayList<ClientHandler> clients;
    private final ServerTabController controller;
    private PrintWriter output;
    private BufferedReader input;
    private String clientAddress;
    private boolean isConnected = true;

    public ClientHandler(Socket socket, CopyOnWriteArrayList<ClientHandler> clients, ServerTabController controller) {
        this.socket = socket;
        this.clients = clients;
        this.controller = controller;
        this.clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {

            // in/out setting
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // setting ID
            // chatClient, line 34
            controller.logMessage("Waiting for client ID from: " + clientAddress);
            String firstMessage = input.readLine();
            controller.logMessage("Received from client: " + firstMessage);

            if (firstMessage != null && firstMessage.startsWith("CLIENT_ID:")) {
                try {
                    String idStr = firstMessage.substring(10).trim();
                    this.clientId = Integer.parseInt(idStr);
                    controller.logMessage("Successfully parsed client ID: " + clientId + " from " + clientAddress);
                } catch (NumberFormatException e) {
                    controller.logMessage("Invalid client ID format: " + firstMessage + ". Using fallback.");
                    this.clientId = clients.size() + 1;
                }
            } else {
                // use sequential ID if client doesn't send ID
                // shouldnt actually happen, but you know...
                this.clientId = clients.size() + 1;
                controller.logMessage("No CLIENT_ID received. Assigned ID: " + clientId + " to " + clientAddress);
            }

            output.println("SERVER: Welcome to MultiChat! You are client #" + clientId);
            broadcast("Client #" + clientId + " (" + clientAddress + ") has joined the chat", null);

            controller.logMessage("Client #" + clientId + " connected from " + clientAddress);

            String message;
            while (isConnected && (message = input.readLine()) != null) {
                String formattedMessage = "Client #" + clientId + ": " + message;
                controller.logMessage(formattedMessage);
                broadcast(formattedMessage, this);
            }

        } catch (IOException e) {
            if (isConnected) {
                controller.logMessage("Error with client #" + clientId + ": " + e.getMessage());
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
        if (!isConnected) return;

        isConnected = false;
        clients.remove(this);
        controller.decrementClientCount();

        broadcast("SERVER: Client #" + clientId + " has left the chat", null);
        controller.logMessage("Client #" + clientId + " disconnected");

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            controller.logMessage("Error closing socket for client #" + clientId + ": " + e.getMessage());
        }
    }

    private void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientAddress() {
        return clientAddress;
    }
}
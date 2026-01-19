package org.vroomvroom.multichat.chat.server;

import org.vroomvroom.multichat.chat.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer implements Runnable {

    private final int port;
    private final int maxClients;
    private final ServerTabController controller;
    private final CopyOnWriteArrayList<ClientHandler> clients;
    private ServerSocket serverSocket;
    private ExecutorService clientPool;
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    public ChatServer(int port, int maxClients, ServerTabController controller) {
        this.port = port;
        this.maxClients = maxClients;
        this.controller = controller;
        this.clients = new CopyOnWriteArrayList<>();
    }

    @Override
    public void run() {
        try {

            // init setting
            serverSocket = new ServerSocket(port);
            clientPool = Executors.newFixedThreadPool(maxClients);

            controller.logMessage("Server socket created successfully");
            controller.logMessage("Waiting for client connections...");

            // client creation loop
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    // loop break
                    if (!isRunning.get()) {
                        break;
                    }

                    // client creation process
                    ClientHandler handler = new ClientHandler(
                            clientSocket,
                            clients,
                            controller
                    );

                    clients.add(handler);
                    controller.incrementClientCount();

                    // execute handler in thread pool
                    clientPool.execute(handler);

                } catch (IOException e) {
                    if (isRunning.get()) {
                        controller.logMessage("Error accepting client: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            controller.logMessage("Failed to start server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        isRunning.set(false);

        // close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                controller.logMessage("Error closing server socket: " + e.getMessage());
            }
        }

        // disconnect clients
        for (ClientHandler client : clients) {
            client.disconnect();
        }
        clients.clear();

        // shutdown thread pool
        if (clientPool != null) {
            clientPool.shutdown();
        }

        controller.logMessage("Server shutdown complete");
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
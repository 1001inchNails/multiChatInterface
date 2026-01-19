package org.vroomvroom.multichat.chat.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTabController {

    private BorderPane root;
    @FXML
    private Button startServerButton;
    @FXML
    private Button stopServerButton;
    @FXML
    private TextField portField;
    @FXML
    private TextField maxClientsField;
    @FXML
    private TextArea logTextArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label clientCountLabel;

    private ChatServer chatServer;
    private ExecutorService serverExecutor;
    private AtomicInteger connectedClients = new AtomicInteger(0);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ServerTabController() {
        createUI();
        initialize();
    }

    private void createUI() {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // top controls
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        startServerButton = new Button("Start Server");
        startServerButton.setPrefWidth(100);
        startServerButton.setOnAction(e -> startServer());

        stopServerButton = new Button("Stop Server");
        stopServerButton.setPrefWidth(100);
        stopServerButton.setDisable(true);
        stopServerButton.setOnAction(e -> stopServer());

        portField = new TextField("8080");
        portField.setPrefWidth(80);

        maxClientsField = new TextField("10");
        maxClientsField.setPrefWidth(80);

        statusLabel = new Label("Status: Stopped");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        topBox.getChildren().addAll(
                startServerButton, stopServerButton,
                new Label("Port:"), portField,
                new Label("Max Clients:"), maxClientsField,
                statusLabel
        );

        root.setTop(topBox);

        // log area
        VBox centerBox = new VBox(5);
        centerBox.getChildren().add(new Label("Server Log:"));

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        logTextArea.setStyle("-fx-font-family: 'Monospaced';");
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

        centerBox.getChildren().add(logTextArea);
        root.setCenter(centerBox);

        // info
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        clientCountLabel = new Label("0");
        clientCountLabel.setStyle("-fx-font-weight: bold;");

        Button clearButton = new Button("Clear Log");
        clearButton.setPrefWidth(80);
        clearButton.setOnAction(e -> clearLog());

        bottomBox.getChildren().addAll(
                new Label("Connected Clients:"),
                clientCountLabel,
                new Separator(),
                clearButton
        );

        root.setBottom(bottomBox);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void initialize() {
        startServer();
    }

    @FXML
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            int maxClients = Integer.parseInt(maxClientsField.getText());

            // server creation
            chatServer = new ChatServer(port, maxClients, this);
            serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.execute(chatServer);

            startServerButton.setDisable(true);
            stopServerButton.setDisable(false);
            portField.setDisable(true);
            maxClientsField.setDisable(true);
            updateStatus("Running", "green");

            logMessage("Server started on port " + port);
            logMessage("Maximum clients: " + maxClients);
            logMessage("------------------------------------------------");

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for port and max clients");
        } catch (Exception e) {
            showAlert("Server Error", "Failed to start server: " + e.getMessage());
        }
    }

    @FXML
    private void stopServer() {
        if (chatServer != null) {
            chatServer.stopServer();
            chatServer = null;
        }

        if (serverExecutor != null) {
            serverExecutor.shutdown();
            serverExecutor = null;
        }

        startServerButton.setDisable(false);
        stopServerButton.setDisable(true);
        portField.setDisable(false);
        maxClientsField.setDisable(false);
        updateStatus("Stopped", "red");

        logMessage("Server stopped");
        connectedClients.set(0);
        updateClientCount(0);
    }

    private void clearLog() {
        logTextArea.clear();
    }

    public void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = dateFormat.format(new Date());
            logTextArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    public void updateClientCount(int count) {
        Platform.runLater(() -> {
            clientCountLabel.setText(String.valueOf(count));
        });
    }

    public void incrementClientCount() {
        int count = connectedClients.incrementAndGet();
        updateClientCount(count);
    }

    public void decrementClientCount() {
        int count = connectedClients.decrementAndGet();
        updateClientCount(count);
    }

    private void updateStatus(String status, String color) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + status);
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
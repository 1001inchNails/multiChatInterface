package org.vroomvroom.multichat.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientTabController {

    private BorderPane root;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Button sendButton;
    @FXML
    private TextField portField;
    @FXML
    private TextField messageField;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private Label statusLabel;
    @FXML
    private CheckBox autoScrollCheck;

    private ChatClient chatClient;
    private int clientId;
    private String clientName;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    // controls the visuals and logic for client tabs
    public ClientTabController() {
        createUI();
        initialize();
    }

    private void createUI() {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // top controls
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        connectButton = new Button("Connect");
        connectButton.setPrefWidth(80);
        connectButton.setOnAction(e -> connectToServer());

        disconnectButton = new Button("Disconnect");
        disconnectButton.setPrefWidth(80);
        disconnectButton.setDisable(true);
        disconnectButton.setOnAction(e -> disconnect());

        portField = new TextField("8080");
        portField.setPrefWidth(80);

        statusLabel = new Label("Disconnected");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        topBox.getChildren().addAll(
                connectButton, disconnectButton,
                new Label("Port:"), portField,
                statusLabel
        );

        root.setTop(topBox);

        // chat area
        VBox centerBox = new VBox(5);
        centerBox.getChildren().add(new Label("Chat Messages:"));

        chatTextArea = new TextArea();
        chatTextArea.setEditable(false);
        chatTextArea.setWrapText(true);
        chatTextArea.setStyle("-fx-font-family: 'Monospaced';");
        VBox.setVgrow(chatTextArea, Priority.ALWAYS);

        centerBox.getChildren().add(chatTextArea);
        root.setCenter(centerBox);

        // bottom controls
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        HBox messageBox = new HBox(5);
        messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        messageField.setOnAction(e -> sendMessage());
        HBox.setHgrow(messageField, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setPrefWidth(80);
        sendButton.setDisable(true);
        sendButton.setOnAction(e -> sendMessage());

        messageBox.getChildren().addAll(messageField, sendButton);

        HBox optionsBox = new HBox(10);
        autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);

        Button clearButton = new Button("Clear Chat");
        clearButton.setPrefWidth(80);
        clearButton.setOnAction(e -> clearChat());

        optionsBox.getChildren().addAll(
                autoScrollCheck,
                new Separator(),
                clearButton
        );

        bottomBox.getChildren().addAll(messageBox, optionsBox);
        root.setBottom(bottomBox);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void initialize() {
        //connectToServer(); // it messes up the clientId and i dont feel like refactoring for a fucking number
    }

    public void setClientId(int id) {
        this.clientId = id;
    }

    public void setClientName(String name) {
        this.clientName = name;
    }


    public int getClientId() {
        return clientId;
    }
    public String getClientName() {
        return clientName;
    }

    public void connectToServer() {
        try {
            int port = Integer.parseInt(portField.getText());

            // pass clientId to ChatClient
            chatClient = new ChatClient(port, this, clientId, clientName);
            chatClient.start();

            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            sendButton.setDisable(false);
            portField.setDisable(true);
            updateStatus("Connected", "green");

            displayMessage("SYSTEM", "Connecting to server at " + ":" + port + "...");

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid port number");
        } catch (Exception e) {
            showAlert("Connection Error", "Failed to connect: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (chatClient != null) {
            chatClient.disconnect();
            chatClient = null;
        }

        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        portField.setDisable(false);
        updateStatus("Disconnected", "red");

        displayMessage("SYSTEM", "Disconnected from server");
    }

    // didnt like the general display so I changed some things but refactoring was a pain in the ass, I mean no offense, but this is a once in a week class, dude...
    public void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatClient != null && chatClient.isConnected()) {
            chatClient.sendMessage(message);
            displayMessage("You", message);
            messageField.clear();
        }
    }

    public void clearChat() {
        chatTextArea.clear();
    }

    public void displayMessage(String sender, String message) {
        Platform.runLater(() -> {
            String timestamp = dateFormat.format(new Date());
            chatTextArea.appendText("[" + timestamp + "] " + message + "\n");
            if (autoScrollCheck.isSelected()) {
                chatTextArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    public void updateStatus(String status, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
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

    public TextField getMessageField() {
        return messageField;
    }

    public void handleServerMessage(String message) {
        displayMessage("SERVER", message);
    }
}
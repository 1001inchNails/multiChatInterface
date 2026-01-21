package org.vroomvroom.multichat.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.vroomvroom.multichat.chat.client.ClientTabController;
import org.vroomvroom.multichat.chat.server.ServerTabController;

import java.util.Optional;

public class ChatApplication extends Application {

    private TabPane tabPane;
    private Tab serverTab;
    private int clientCounter = 1;

    @Override
    public void start(Stage primaryStage) {
        try {

            // main settings

            BorderPane root = new BorderPane();
            tabPane = new TabPane();

            // server tab
            createServerTab();

            // menu bar
            MenuBar menuBar = createMenuBar();

            root.setTop(menuBar);
            root.setCenter(tabPane);

            // scene
            Scene scene = new Scene(root, 900, 600);

            // set stage
            primaryStage.setTitle("Ok, sure, lets throw in an interface too, why not? Everybody loves JavaFX, right?");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to start application: " + e.getMessage());
        }
    }

    // file menu
    private MenuBar createMenuBar() {

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem newClientMenuItem = new MenuItem("New Client");
        newClientMenuItem.setOnAction(e -> createNewClientTab());

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(newClientMenuItem, new SeparatorMenuItem(), exitMenuItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    // the one and only server tab
    private void createServerTab() {
        try {
            ServerTabController controller = new ServerTabController();
            serverTab = new Tab("Server");
            serverTab.setContent(controller.getRoot());
            serverTab.setClosable(false);
            tabPane.getTabs().add(serverTab);
        } catch (Exception e) {
            showError("Server Tab Error", "Failed to create tab: " + e.getMessage());
        }
    }

    // user created client tabs
    private void createNewClientTab() {
        try {

            // name input
            String clientName = showNameInputDialog();

            // cancel? No client for you, bitch
            if (clientName == null || clientName.isEmpty()) {
                return;
            }

            // set the controller
            ClientTabController controller = new ClientTabController();

            // set the name
            controller.setClientName(clientName);
            // set the id
            controller.setClientId(clientCounter);

            // the actual tab
            Tab clientTab = new Tab(clientName + "'s Chat");
            clientTab.setContent(controller.getRoot());
            clientTab.setClosable(true);

            clientTab.setOnCloseRequest(event -> {
                controller.disconnect();
            });

            tabPane.getTabs().add(clientTab);
            tabPane.getSelectionModel().select(clientTab);

            clientCounter++;
        } catch (Exception e) {
            showError("Client Tab Error", "Failed to create client tab: " + e.getMessage());
        }
    }

    // name input modal
    private String showNameInputDialog() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Please enter name:");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        // enable/disable OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // listener to enable/disable OK button
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label nameLabel = new Label("Name:");
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        // convert to string
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });

        // show and wait
        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {

        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to launch application: " + e.getMessage());
        }
    }
}
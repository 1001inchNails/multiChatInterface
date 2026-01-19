package org.vroomvroom.multichat.chat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.vroomvroom.multichat.chat.client.ClientTabController;
import org.vroomvroom.multichat.chat.server.ServerTabController;

public class ChatApplication extends Application {

    private TabPane tabPane;
    private Tab serverTab;
    private int clientCounter = 1;

    @Override
    public void start(Stage primaryStage) {
        try {
            // main container
            BorderPane root = new BorderPane();

            // tab pane
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

    private void createNewClientTab() {
        try {
            ClientTabController controller = new ClientTabController();
            controller.setClientId(clientCounter);

            Tab clientTab = new Tab("Client " + clientCounter);
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
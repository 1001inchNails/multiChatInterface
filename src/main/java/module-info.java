module org.vroomvroom.multichat {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.vroomvroom.multichat.chat;
    exports org.vroomvroom.multichat.chat;
    exports org.vroomvroom.multichat.chat.client;
    opens org.vroomvroom.multichat.chat.client;
    exports org.vroomvroom.multichat.chat.server;
    opens org.vroomvroom.multichat.chat.server;
}
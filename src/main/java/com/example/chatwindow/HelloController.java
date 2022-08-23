package com.example.chatwindow;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class HelloController {


    private Socket socket = null;
    DataInputStream in, inClient;
    DataOutputStream out;

    private String userName;
    @FXML
    private TextArea welcomeText, listClients;
    @FXML
    private TextField textUser, userNameField;
    @FXML
    private HBox loginPanel, msgPanel;

    @FXML
    public void initialize() {
        connect();
    }

    @FXML
    public void sendMsg() {
        if (!textUser.getText().trim().isEmpty()) {
            try {
                out.writeUTF(textUser.getText());
                textUser.setText("");
                textUser.requestFocus();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить пустое сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (!msg.startsWith("/clients")) {
                            welcomeText.appendText(msg + '\n');
                        } else {
                            listClients.clear();
                            listClients.appendText("On-line:" + '\n');
                            listClients.appendText(msg.substring(9) + '\n');
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.start();

        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server[localhost:8189]");

        }
    }


}
package example.server;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    //класс, отвечающий за обмен сообщениями между клиентами и сервером
    private MyServer myServer;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблема при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        try {
            while (true) {
                socket.setSoTimeout(120_000);
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s");
                    String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                    if (nick != null) {
                        if (!myServer.isNickBusy(nick)) {
                            sendMsg("/auth " + nick);
                            name = nick;
                            myServer.broadcastMsg(name + " зашел в чат");
                            myServer.subscribe(this);
                            return;
                        } else {
                            sendMsg("Учетная запись уже используется");
                        }
                    } else {
                        sendMsg("Неверные логин/пароль");
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            //не разобралась почему не закрывается окно чата
           Platform.exit();
            closeConnection();

        }
    }

    public void readMessages() throws IOException {
        while (true) {

            String strFromClient = in.readUTF();

            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals("/end")) {
                return;
            }
            if (!strFromClient.startsWith("/w")) {
                myServer.broadcastMsg(name + ": " + strFromClient);
            } else {
                String[] parts = strFromClient.split("\\s");

                String msgPrivet = strFromClient.substring(9);
                sendMsg(name + ":" + strFromClient);
                myServer.privateMsg(this, parts[1], name + " privet massage:" + msgPrivet);
            }

        }
    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

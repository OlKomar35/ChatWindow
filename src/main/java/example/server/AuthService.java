package example.server;

public interface AuthService {
    //интерфейс, описывающий сервис аутентификации на стороне сервера
    void start();
    String getNickByLoginPass(String login, String pass);
    void stop();
}

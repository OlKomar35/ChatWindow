module com.example.chatwindow {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.chatwindow to javafx.fxml;
    exports com.example.chatwindow;
}
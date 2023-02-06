module com.example.imageeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires opencv;


    opens com.example.imageeditor to javafx.fxml;
    exports com.example.imageeditor;
}
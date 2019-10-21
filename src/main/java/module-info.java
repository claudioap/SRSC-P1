module org.openjfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens view;
    exports view;
}
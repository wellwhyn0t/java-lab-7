module tgquiz.javalab7 {
    requires javafx.controls;
    requires javafx.fxml;


    opens tgquiz.javalab7 to javafx.fxml;
    exports tgquiz.javalab7;
}
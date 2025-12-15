module aplicacion.practica3_jaimecasanova {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires vosk;


    opens aplicacion.practica3_jaimecasanova to javafx.fxml;
    exports aplicacion.practica3_jaimecasanova;
    exports aplicacion.practica3_jaimecasanova.controllers;
    opens aplicacion.practica3_jaimecasanova.controllers to javafx.fxml;
}
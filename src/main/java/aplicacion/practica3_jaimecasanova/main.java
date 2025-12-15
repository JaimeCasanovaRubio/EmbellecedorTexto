package aplicacion.practica3_jaimecasanova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class main extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("practica3.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(main.class.getResource("styles.css").toExternalForm());
        stage.setTitle("Practica3_JaimeCasanova");
        stage.setScene(scene);
        stage.show();

    }
}

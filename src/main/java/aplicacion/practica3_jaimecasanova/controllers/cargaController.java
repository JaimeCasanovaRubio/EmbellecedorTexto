package aplicacion.practica3_jaimecasanova.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.TextFlow;

public class cargaController {
    private practica3Controller pc;


    public TextFlow txtFlow;

    // metodo para pasar las variables desde la clase practica3
    public void setPractica3Controller(practica3Controller controller) {
        this.pc = controller;
    }

}

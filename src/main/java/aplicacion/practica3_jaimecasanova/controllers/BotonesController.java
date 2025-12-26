package aplicacion.practica3_jaimecasanova.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BotonesController {

    @FXML
    private Button btnCursiva;

    @FXML
    private Button btnInvertir;

    @FXML
    private Button btnNegrita;

    @FXML
    private Button btnTamanno;

    private practica3Controller pc;
    public void setPractica3Controller(practica3Controller controller) {
        this.pc = controller;
    }

    @FXML
    void clickCursiva(ActionEvent event) {
        pc.clickCursiva();
    }

    @FXML
    void clickInvertir(ActionEvent event) {
        pc.clickInvertir();
    }

    @FXML
    void clickNegrita(ActionEvent event) {
        pc.clickNegrita();
    }

    @FXML
    void clickTamanno(ActionEvent event) {
        pc.clickMayus();
    }

}

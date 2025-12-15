package aplicacion.practica3_jaimecasanova.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;

public class buscarController {


    @FXML
    private TextField txtText;


    // Variable para guardar la referencia del controlador principal
    private practica3Controller pc;

    // metodo para pasar las variables desde la clase practica3
    public void setPractica3Controller(practica3Controller controller) {
        this.pc = controller;
    }

    public void realizarBusquedaYSeleccion(String busqueda) {
        pc.buscarYSeleccionarSiguiente(busqueda);
    }

    @FXML
    void clickBuscar() {
        String busqueda = txtText.getText();
        realizarBusquedaYSeleccion(busqueda);
    }



}



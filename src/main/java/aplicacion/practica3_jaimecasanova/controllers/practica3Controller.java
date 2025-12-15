package aplicacion.practica3_jaimecasanova.controllers;

import aplicacion.practica3_jaimecasanova.ProgressLabel;
import aplicacion.practica3_jaimecasanova.main;
import aplicacion.practica3_jaimecasanova.nui.NuiCommand;
import aplicacion.practica3_jaimecasanova.nui.NuiController;
import aplicacion.practica3_jaimecasanova.nui.NuiListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class practica3Controller implements NuiListener {

    // Campos de la UI principal
    @FXML
    public TextFlow txtFlow;
    public ProgressLabel progressLabel;
    @FXML
    private Label lblContador;
    @FXML
    public TextArea txtEditable;

    // Campos para la NUI de Voz (integrados)
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea recognizedTextArea;

    // Lógica NUI
    private final NuiController nuiController = new NuiController();
    private AudioFormat audioFormat;
    private TargetDataLine microphone;
    private Thread recognizerThread;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private Model voskModel; // Moved Model to a field to manage its lifecycle

    // Campos de estado del editor
    private boolean primerCambio = true;
    public boolean cursiva = false;
    public boolean negrita = false;
    public boolean mayus = false;

    public boolean dictar = false;
    private static Font defaultFont = Font.font("System", 12);
    private ArrayList<List<Node>> textos = new ArrayList<>();
    private int indice = -1;
    private String ultimoTerminoBuscado;
    private int ultimaPosicionBusqueda;

    // Botón de búsqueda que faltaba
    @FXML
    private Button btnBusqueda;


    @FXML
    public void initialize() {
        // Registro del listener NUI
        nuiController.addNuiListener(this);
        actualizarArray();

        // Listener para el contador de palabras/caracteres
        txtEditable.textProperty().addListener((observable, oldValue, stringListener) -> {
            int caracteres = stringListener.length();
            String[] palabras = stringListener.trim().split("\\s+");
            int numPalabras = stringListener.trim().isEmpty() ? 0 : palabras.length;
            lblContador.setText("Contador -> Palabras: " + numPalabras + ", Caracteres: " + caracteres);
        });

        // Inicializar y arrancar la escucha constante
        initializeVosk();
    }

    private void initializeVosk() {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        try {
            // Initialize the model and store it in the class field
            voskModel = new Model("src/main/resources/aplicacion/practica3_jaimecasanova/model/vosk-model-small-es-0.42");
        } catch (IOException e) {
            if (statusLabel != null) Platform.runLater(() -> statusLabel.setText("Error: Modelo Vosk no cargado."));
            e.printStackTrace();
            return;
        }

        audioFormat = new AudioFormat(16000, 16, 1, true, false);
        listening.set(true);
        if (statusLabel != null) Platform.runLater(() -> statusLabel.setText("Modelo cargado. Escuchando..."));

        recognizerThread = new Thread(() -> {
            // The recognizer is created for the thread and closed automatically by try-with-resources
            try (Recognizer recognizer = new Recognizer(voskModel, 16000)) {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> { if (statusLabel != null) statusLabel.setText("Error: Línea de audio no disponible."); });
                    return;
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(audioFormat);
                microphone.start();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (listening.get()) {

                    try {
                        bytesRead = microphone.read(buffer, 0, buffer.length);
                    } catch (Exception e) {

                        break;
                    }

                    if (bytesRead < 0) {
                        break;
                    }

                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String resultJson = recognizer.getResult();
                        String recognizedText = parseRecognizedText(resultJson, "text");
                        Platform.runLater(() -> {
                            if (recognizedTextArea != null) recognizedTextArea.appendText("Final: " + recognizedText + "\n");
                            processVoiceCommand(recognizedText);
                        });
                    } else {
                        String partialResultJson = recognizer.getPartialResult();
                        String partialText = parseRecognizedText(partialResultJson, "partial");
                        Platform.runLater(() -> {
                            if (recognizedTextArea != null && !partialText.isEmpty()) {
                                recognizedTextArea.setText("Parcial: " + partialText + " ");
                            }
                        });
                    }
                }
            } catch (LineUnavailableException | IOException e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) statusLabel.setText("Error de audio: " + e.getMessage());
                });
            } finally {
                // Cleanup microphone when the thread exits
                if (microphone != null && microphone.isOpen()) {
                    microphone.stop();
                    microphone.close();
                }
                 Platform.runLater(() -> { if (statusLabel != null) statusLabel.setText("Reconocimiento detenido."); });
            }
        }, "Vosk-Recognizer-Thread");

        recognizerThread.setDaemon(true);
        recognizerThread.start();
    }

    private String parseRecognizedText(String json, String key) {
        String keyPattern = "\"" + key + "\" : \"";
        int textIndex = json.indexOf(keyPattern);
        if (textIndex != -1) {
            String sub = json.substring(textIndex + keyPattern.length());
            int endIndex = sub.indexOf("\"");
            if (endIndex != -1) {
                return sub.substring(0, endIndex).trim();
            }
        }
        return "";
    }

    private void processVoiceCommand(String commandText) {
        String lowerCaseCommand = commandText.toLowerCase();
        NuiCommand command = NuiCommand.UNKNOWN;
        String texto = null;

        if (dictar) {
            command = NuiCommand.DICTAR_TEXTO;
            texto = commandText.substring("dictar".length()).trim();
        } else if (lowerCaseCommand.contains("negrita")) {
            command = NuiCommand.APLICAR_NEGRITA;
        } else if (lowerCaseCommand.contains("cursiva")) {
            command = NuiCommand.APLICAR_CURSIVA;
        } else if (lowerCaseCommand.contains("nuevo") || lowerCaseCommand.contains("limpiar")) {
            command = NuiCommand.NUEVO_DOCUMENTO;
        } else if (lowerCaseCommand.contains("abrir") || lowerCaseCommand.contains("cargar")) {
            command = NuiCommand.ABRIR_DOCUMENTO;
        } else if (lowerCaseCommand.contains("guardar")) {
            command = NuiCommand.GUARDAR_DOCUMENTO;
        } else if (lowerCaseCommand.contains("mayúscula")) {
            mayus = false;
            command= NuiCommand.APLICAR_TAMAÑO;
        }else if(lowerCaseCommand.contains("minúscula")) {
            mayus = true;
            command= NuiCommand.APLICAR_TAMAÑO;
        }else if(lowerCaseCommand.contains("invierte")||lowerCaseCommand.contains("invertir")) {
            command=NuiCommand.APLICAR_INVERTIR;
        }

        nuiController.processCommand(command, texto);
    }

    @Override
    public void onCommand(NuiCommand cmd, String texto) {
        switch (cmd) {
            case APLICAR_TAMAÑO:
                clickMayus(null);
                break;
            case APLICAR_NEGRITA:
                clickNegrita(null);
                break;
            case APLICAR_CURSIVA:
                clickCursiva(null);
                break;
            case GUARDAR_DOCUMENTO:
                clickGuardar(null);
                break;
            case NUEVO_DOCUMENTO:
                clickReiniciar(null);
                break;
            case ABRIR_DOCUMENTO:
                clickCargar(null);
                break;
            case DICTAR_TEXTO:
                if (texto != null && !texto.isEmpty()) {
                    txtEditable.appendText(texto + " ");
                }
                break;
            case APLICAR_INVERTIR:
                clickInvertir(null);
                break;
            case UNKNOWN:
                if (statusLabel != null) statusLabel.setText("Comando no reconocido.");
                break;
        }
    }

    // --- MÉTODOS ORIGINALES DEL EDITOR RESTAURADOS ---

    @FXML
    void clickBuscar(ActionEvent event) {
        try {
            // Cargar el nuevo archivo FXML
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("buscarView.fxml"));
            Parent root = fxmlLoader.load();

            buscarController controller = fxmlLoader.getController();
            controller.setPractica3Controller(this);

            // Crear un Stage
            Stage nuevoStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(main.class.getResource("styles.css").toExternalForm());

            nuevoStage.setTitle("Buscar - Practica3_JaimeCasanova");
            nuevoStage.setScene(scene);
            nuevoStage.show(); // Abre la nueva ventana sin cerrar la anterior

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar buscarView.fxml: " + e.getMessage());
        }
    }

    @FXML
    void clickMayus(ActionEvent event) {
        IndexRange selection = txtEditable.getSelection();

       //SIN SELECCIÓN
        if (selection.getLength() == 0) {
            String textoBase;
            if (primerCambio) {
                textoBase = txtEditable.getText();
            } else {
                textoBase = txtFlowAString(txtFlow);
            }
            mayus = !mayus;
            String nuevoTexto = mayus ? textoBase.toUpperCase() : textoBase.toLowerCase();
            txtFlow.getChildren().clear();
            txtFlow.getChildren().add(aplicarEstilos(nuevoTexto));

            primerCambio = false;
            return;
        }

        //CON SELECCIÓN
        String textoCompleto = txtEditable.getText();
        txtFlow.getChildren().clear();

        //Añade al textflow lo anterior a los seleccionado
        if (selection.getStart() > 0) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(0, selection.getStart())));
        }


        //añade lo seleccionado con e cambio
        Text selectedNode = new Text(textoCompleto.substring(selection.getStart(), selection.getEnd()).toUpperCase());
        txtFlow.getChildren().add(selectedNode);

        //añade el resto
        if (selection.getEnd() < textoCompleto.length()) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(selection.getEnd())));
        }

        // Reset global style flags as they are now meaningless.
        negrita = false;
        cursiva = false;
        mayus = false;
        primerCambio = false;
        actualizarArray();
    }

    @FXML
    void clickNegrita(ActionEvent event) {
        IndexRange selection = txtEditable.getSelection();


        if (selection.getLength() == 0) {
            String textoBase;
            if (primerCambio) {
                textoBase = txtEditable.getText();
            } else {
                textoBase = txtFlowAString(txtFlow);
            }
            negrita = !negrita;
            txtFlow.getChildren().clear();
            txtFlow.getChildren().add(aplicarEstilos(textoBase));
            primerCambio = false;
            return;
        }

        String textoCompleto = txtEditable.getText();
        txtFlow.getChildren().clear();

        if (selection.getStart() > 0) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(0, selection.getStart())));
        }

        Text selectedNode = new Text(textoCompleto.substring(selection.getStart(), selection.getEnd()));
        selectedNode.setFont(Font.font(defaultFont.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, defaultFont.getSize()));
        txtFlow.getChildren().add(selectedNode);

        if (selection.getEnd() < textoCompleto.length()) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(selection.getEnd())));
        }

        negrita = false;
        cursiva = false;
        mayus = false;
        primerCambio = false;
        actualizarArray();
    }

    @FXML
    void clickCursiva(ActionEvent event) {
        IndexRange selection = txtEditable.getSelection();

        // If no selection, use existing logic.
        if (selection.getLength() == 0) {
            String textoBase;
            if (primerCambio) {
                textoBase = txtEditable.getText();
            } else {
                textoBase = txtFlowAString(txtFlow);
            }
            cursiva = !cursiva;
            txtFlow.getChildren().clear();
            txtFlow.getChildren().add(aplicarEstilos(textoBase));
            primerCambio = false;
            return;
        }

        String textoCompleto = txtEditable.getText();
        txtFlow.getChildren().clear();


        if (selection.getStart() > 0) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(0, selection.getStart())));
        }

        Text selectedNode = new Text(textoCompleto.substring(selection.getStart(), selection.getEnd()));
        selectedNode.setFont(Font.font(defaultFont.getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, defaultFont.getSize()));
        txtFlow.getChildren().add(selectedNode);


        if (selection.getEnd() < textoCompleto.length()) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(selection.getEnd())));
        }


        negrita = false;
        cursiva = false;
        mayus = false;
        primerCambio = false;
        actualizarArray();

    }

    @FXML
    void clickInvertir(ActionEvent event) {
        IndexRange selection = txtEditable.getSelection();


        if (selection.getLength() == 0) {
            String textoAInvertir;
            if (txtFlow.getChildren().isEmpty() || primerCambio) {
                textoAInvertir = txtEditable.getText();
            } else {
                textoAInvertir = txtFlowAString(txtFlow);
            }
            invertir(textoAInvertir);
            primerCambio = false;
            return;
        }


        String textoCompleto = txtEditable.getText();
        txtFlow.getChildren().clear();


        if (selection.getStart() > 0) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(0, selection.getStart())));
        }

        // Part 2: Selected (and inverted)
        String textoSeleccionado = textoCompleto.substring(selection.getStart(), selection.getEnd());
        String textoInvertido = new StringBuilder(textoSeleccionado).reverse().toString();
        txtFlow.getChildren().add(new Text(textoInvertido));

        // Part 3: After
        if (selection.getEnd() < textoCompleto.length()) {
            txtFlow.getChildren().add(new Text(textoCompleto.substring(selection.getEnd())));
        }

        // Reset global style flags as they are now meaningless.
        negrita = false;
        cursiva = false;
        mayus = false;
        primerCambio = false;
        actualizarArray();

    }

    @FXML
    void clickReiniciar(ActionEvent event) {

        primerCambio=true;
        txtEditable.clear();
        txtFlow.getChildren().clear();

    }

    @FXML
    void clickGuardar(ActionEvent event) {
        String contenidoMarkdown = generarMarkdownDesdeTextFlow();
        progressLabel.setProgress(0); // Reiniciar la barra de progreso
        progressLabel.setText("Iniciando guardado...");

        Thread saveThread = new Thread(() -> {
            try {
                // 1. Simular el progreso en el hilo secundario
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000); // Pausa en el hilo secundario

                    // Prepara el código para actualizar la UI
                    final int step = i;
                    javafx.application.Platform.runLater(() -> {
                        // Este bloque se ejecuta en el hilo de JavaFX de forma segura
                        progressLabel.setProgress(step / 5.0); // Progreso de 0.2 a 1.0
                        progressLabel.setText("Guardando... " + (step * 20) + "%");
                    });
                }

                // 2. Realizar la operación de guardado de archivo
                File tempFile = File.createTempFile("texto_formateado", ".md");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    writer.write(contenidoMarkdown);
                }

                // 3. Actualizar la UI cuando el guardado es exitoso
                javafx.application.Platform.runLater(() -> {
                    Text texto = new Text("Archivo Markdown creado en: " + tempFile.getAbsolutePath());
                    txtFlow.getChildren().clear();
                    txtFlow.getChildren().add(texto);
                    progressLabel.setText("Guardado completado.");
                });

            } catch (IOException | InterruptedException e) {
                // 4. Actualizar la UI si ocurre un error
                javafx.application.Platform.runLater(() -> {
                    Text texto = new Text("Error durante el guardado: " + e.getMessage());
                    txtFlow.getChildren().clear();
                    txtFlow.getChildren().add(texto);
                    progressLabel.setText("Error.");
                });
                e.printStackTrace();
            }
        });

        saveThread.start(); // Inicia el hilo secundario
        primerCambio = true;
    }

    @FXML
    void clickCargar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de texto","*.txt")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File seleccionado=fileChooser.showOpenDialog(null);

        try {
            // Cargar el nuevo archivo FXML
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("cargaView.fxml"));
            Parent root = fxmlLoader.load();

            cargaController controller = fxmlLoader.getController();
            controller.setPractica3Controller(this);

            // Crear un Stage
            Stage nuevoStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(main.class.getResource("styles.css").toExternalForm());

            nuevoStage.setTitle("Carga - Practica3_JaimeCasanova");
            nuevoStage.setScene(scene);
            nuevoStage.show(); // Abre la nueva ventana sin cerrar la anterior

            try (BufferedReader br=new BufferedReader(new FileReader(seleccionado.getAbsolutePath()))){
                StringBuilder stringBuilder=new StringBuilder();

                while(br.readLine()!=null){
                    stringBuilder.append(br.readLine());
                }
                if(controller.txtFlow!=null){
                    controller.txtFlow.getChildren().clear();
                    controller.txtFlow.getChildren().add(new Text(stringBuilder.toString()));
                }


            }catch (IOException e){
                System.out.println("Error al leer el archivo: " + e.getMessage());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar cargaView.fxml: " + e.getMessage());
        }
    }

    @FXML
    void clickUndo(ActionEvent event) {
        if(indice>0){
            indice--;
            List<Node> estadoAnterior=clonarNodos(textos.get(indice));
            txtFlow.getChildren().clear();
            txtFlow.getChildren().addAll(estadoAnterior);
        }
    }

    @FXML
    void clickRedo(ActionEvent event) {
        if(indice<textos.size()-1){
            indice++;
            List<Node> siguienteEstado =clonarNodos(textos.get(indice));
            txtFlow.getChildren().clear();
            txtFlow.getChildren().addAll(siguienteEstado);
        }

    }

    public void invertir(String texto) {
        txtFlow.getChildren().clear();
        String textoInvertido = new StringBuilder(texto).reverse().toString();
        txtFlow.getChildren().add(aplicarEstilos(textoInvertido));
    }

    public Text aplicarEstilos(String texto) {
        Text textoNode = new Text(texto);

        FontWeight weight = negrita ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = cursiva ? FontPosture.ITALIC : FontPosture.REGULAR;

        textoNode.setFont(Font.font(defaultFont.getFamily(), weight, posture, defaultFont.getSize()));

        return textoNode;
    }

    private String generarMarkdownDesdeTextFlow() {
        StringBuilder markdown = new StringBuilder();
        for (Node node : txtFlow.getChildren()) {
            if (node instanceof Text) {
                Text textNode = (Text) node;
                String texto = textNode.getText();
                Font font = textNode.getFont();

                boolean esNegrita = font.getStyle().contains("Bold");
                boolean esCursiva = font.getStyle().contains("Italic");

                if (esNegrita && esCursiva) {
                    markdown.append("***").append(texto).append("***");
                } else if (esNegrita) {
                    markdown.append("**").append(texto).append("**");
                } else if (esCursiva) {
                    markdown.append("*").append(texto).append("*");
                } else {
                    markdown.append(texto);
                }
            }
        }
        return markdown.toString();
    }

    private String txtFlowAString(TextFlow txtFlow) {
        StringBuilder textoAEscribir = new StringBuilder();
        for (Node node : txtFlow.getChildren()) {
            if (node instanceof Text) {
                textoAEscribir.append(((Text) node).getText());
            }
        }
        return textoAEscribir.toString();
    }

    public void buscarYSeleccionarSiguiente(String busqueda) {
        String textoCompleto = txtEditable.getText();
        if (busqueda == null || busqueda.isEmpty() || textoCompleto == null || textoCompleto.isEmpty()) {
            txtEditable.deselect();
            return;
        }

        if (!busqueda.equalsIgnoreCase(ultimoTerminoBuscado)) {
            ultimaPosicionBusqueda = 0;
            ultimoTerminoBuscado = busqueda;
        }

        String textoMinuscula = textoCompleto.toLowerCase();
        String busquedaMinuscula = busqueda.toLowerCase();

        int startIndex = textoMinuscula.indexOf(busquedaMinuscula, ultimaPosicionBusqueda);

        if (startIndex == -1) {
            ultimaPosicionBusqueda = 0;
            startIndex = textoMinuscula.indexOf(busquedaMinuscula, ultimaPosicionBusqueda);
        }

        if (startIndex != -1) {
            int endIndex = startIndex + busqueda.length();
            txtEditable.selectRange(startIndex, endIndex);
            txtEditable.requestFocus();
            ultimaPosicionBusqueda = endIndex;
        } else {
            txtEditable.deselect();
            ultimaPosicionBusqueda = 0;
            ultimoTerminoBuscado = "";
        }
    }

    private void actualizarArray() {
        while (textos.size()-1>indice) {
            textos.remove(textos.size()-1);
        }
        textos.add(clonarNodos(txtFlow.getChildren()));

        indice=textos.size()-1;
    }

    private List<Node> clonarNodos(List<Node> nodos) {
        List<Node> clonados = new ArrayList<>();
        for (Node node : nodos) {
            if(node instanceof Text) {
                Text original= (Text) node;
                Text clon= new Text(original.getText());
                clon.setFont(original.getFont());
                clon.setFill(original.getFill());
                clonados.add(clon);
            }
        }
        return clonados;
    }

    public void clickDictar(ActionEvent actionEvent) {
        dictar=!dictar;
    }
}
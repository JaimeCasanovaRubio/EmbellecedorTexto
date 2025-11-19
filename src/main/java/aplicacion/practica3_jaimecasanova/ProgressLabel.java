package aplicacion.practica3_jaimecasanova;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressLabel extends VBox {
    private final ProgressBar progressBar;
    private final Label label;

    public ProgressLabel() {
        this (0.0,"listo");
    }

    public ProgressLabel(double progress, String text) {
        this.progressBar = new ProgressBar(progress);
        this.label = new Label(text);
        label.setId("progressLabelText");

        setSpacing(4);

        progressBar.setPrefWidth(200);

        getChildren().addAll(progressBar,label);

    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }
    public double getProgress() {
        return progressBar.getProgress();
    }

    public DoubleProperty getProgressProperty() {
        return progressBar.progressProperty();
    }

    public void setText(String text) {
        label.setText(text);
    }
    public String getText() {
        return label.getText();
    }

    public StringProperty getTextProperty() {
        return label.textProperty();
    }
}

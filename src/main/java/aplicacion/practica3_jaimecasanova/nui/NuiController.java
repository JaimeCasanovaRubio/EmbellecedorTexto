package aplicacion.practica3_jaimecasanova.nui;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador central para la lógica NUI.
 * Registra listeners (como la UI principal) y recibe comandos de los adaptadores de entrada (voz, gestos).
 * Notifica a los listeners cuando llega un comando nuevo.
 */
public class NuiController {

    private final List<NuiListener> listeners = new ArrayList<>();

    /**
     * Registra un nuevo listener para ser notificado de los comandos NUI.
     * @param listener El listener a añadir.
     */
    public void addNuiListener(NuiListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un listener para que deje de recibir notificaciones.
     * @param listener El listener a eliminar.
     */
    public void removeNuiListener(NuiListener listener) {
        listeners.remove(listener);
    }

    /**
     * Método llamado por los adaptadores de entrada para procesar un comando.
     * Notifica a todos los listeners registrados.
     * @param command El comando NUI reconocido.
     * @param texto Datos adicionales opcionales.
     */
    public void processCommand(NuiCommand command, String texto) {
        System.out.println("NuiController: Comando recibido -> " + command); // Feedback en consola como pide la práctica
        for (NuiListener listener : listeners) {
            listener.onCommand(command, texto);
        }
    }
}

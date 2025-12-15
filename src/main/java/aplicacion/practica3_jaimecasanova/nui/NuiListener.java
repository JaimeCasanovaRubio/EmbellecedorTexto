package aplicacion.practica3_jaimecasanova.nui;

/**
 * Interfaz para los componentes que escuchan y reaccionan a comandos NUI.
 * La ventana principal del editor implementará esta interfaz para ejecutar las acciones.
 */
public interface NuiListener {
    /**
     * Se llama cuando se recibe un comando NUI.
     *
     * @param cmd El comando NUI reconocido.
     * @param payload Datos adicionales opcionales asociados al comando (por ejemplo, el texto a dictar).
     */
    void onCommand(NuiCommand cmd, String payload);
}

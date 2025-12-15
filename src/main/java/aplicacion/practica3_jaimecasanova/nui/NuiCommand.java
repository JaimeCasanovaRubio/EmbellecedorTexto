package aplicacion.practica3_jaimecasanova.nui;

/**
 * Define el conjunto de comandos que la Interfaz de Usuario Natural (NUI) puede reconocer y procesar.
 * Esta enumeración es clave para desacoplar la fuente de entrada (voz, gestos) de la lógica de la aplicación.
 */
public enum NuiCommand {
    // Comandos de gestión de documentos
    NUEVO_DOCUMENTO,
    ABRIR_DOCUMENTO,
    GUARDAR_DOCUMENTO,

    // Comandos de formato de texto
    APLICAR_TAMAÑO,
    APLICAR_NEGRITA,
    APLICAR_CURSIVA,

    APLICAR_INVERTIR,

    
    // Comando opcional de dictado
    DICTAR_TEXTO,

    // Comando para representar una acción no reconocida
    UNKNOWN;
}

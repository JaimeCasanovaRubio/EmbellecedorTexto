# Práctica 4 - Editor de Texto Avanzado (Versión Refactorizada)

## 1. Descripción General

Este proyecto es la versión significativamente refactorizada y mejorada de un editor de texto básico desarrollado en JavaFX. La aplicación original, que separaba las funciones de edición y transformación de texto en distintas ventanas, ha sido rediseñada para ofrecer una experiencia de usuario más fluida, unificada y potente en una única interfaz.

El núcleo de la refactorización se ha centrado en solucionar los problemas arquitectónicos de la versión anterior, implementando un sistema de edición "no destructivo" y un robusto historial de Deshacer/Rehacer.

## 2. Características Principales

- **Interfaz Unificada:** Las funcionalidades de transformación de texto (estilos, mayúsculas, etc.) se han integrado directamente en la ventana principal, eliminando la ventana separada de "Transformación".
- **Motor de Estilos "No Destructivo":**
  - Permite aplicar y **combinar múltiples estilos** (ej: negrita y cursiva) sobre el mismo texto.
  - El formato se aplica modificando el texto existente en lugar de destruirlo y reconstruirlo, conservando siempre el estado anterior.
- **Sistema de Deshacer y Rehacer (Undo/Redo):**
  - Implementación de un historial de acciones completo para todas las modificaciones (estilos, escritura, etc.).
  - Permite deshacer y rehacer cambios de forma fiable y predecible.
- **Aplicación de Estilos por Selección:** Los estilos (negrita, cursiva) se pueden aplicar tanto a todo el texto como a fragmentos seleccionados de forma precisa.
- **Funcionalidades Adicionales:**
  - **Búsqueda con Resaltado:** Una ventana secundaria permite buscar texto, que se resalta en tiempo real sobre el editor.
  - **Contador de Palabras y Caracteres:** Un `listener` actualiza constantemente las estadísticas del texto.
  - **Inversión y Mayúsculas:** Funciones para invertir el texto o convertirlo a mayúsculas.
  - **Guardado Asíncrono:** Un botón de guardado que simula una operación de escritura en un fichero `.md` con una barra de progreso.
  - **Tema Oscuro:** Interfaz estilizada con CSS para una apariencia moderna.

## 3. Mejoras Clave de la Refactorización

Esta versión representa un salto cualitativo respecto a la original. Los principales cambios son:

1.  **Unificación de la Interfaz (Eliminación de `transformarView`):** Se ha eliminado la ventana `transformarView.fxml` y su controlador `transformarController`. Toda la lógica de estilos ahora reside en `practica3Controller`, centralizando el código y mejorando la experiencia de usuario.

2.  **Nuevo Motor de Estilos (`TextFlow` como Fuente de la Verdad):**
    - **Antes:** La lógica se basaba en leer texto plano del `TextArea`, aplicar un único estilo con CSS (`-fx-font-weight: bold;`) y destruir el formato anterior. Esto impedía combinar estilos y generaba bugs.
    - **Ahora:** El `TextFlow` es la única fuente de la verdad. Las acciones de estilo modifican los `nodos` de texto (`Text`) existentes dentro del `TextFlow` de forma aditiva, usando `FontWeight` y `FontPosture` de JavaFX. Esto permite que un mismo trozo de texto sea negrita y cursiva a la vez.

3.  **Implementación del Sistema de Deshacer/Rehacer:**
    - **Antes:** No existía esta funcionalidad.
    - **Ahora:** Se ha implementado un robusto sistema de historial usando un `ArrayList<List<Node>>`. Cada vez que se realiza un cambio, se guarda una "foto" del estado de todos los nodos del `TextFlow`. Esto permite restaurar estados anteriores con total fidelidad, incluyendo todo el formato.

4.  **Sincronización de UI:** Se ha establecido una sincronización entre el `TextArea` (donde se puede escribir al principio) y el `TextFlow` (que muestra el formato). Cuando el `TextArea` pierde el foco, su contenido se traslada al `TextFlow` y se crea un punto en el historial, haciendo la experiencia de escritura y edición mucho más sólida.

## 4. Estructura del Proyecto

El proyecto está gestionado con Maven y sigue una estructura MVC.

- **Clases Principales:** `src/main/java/aplicacion/practica3_jaimecasanova/`
  - `main.java`: Punto de entrada de la aplicación.
  - `controllers/practica3Controller.java`: **Controlador principal** que ahora contiene toda la lógica del editor.
  - `controllers/buscarController.java`: Controlador para la ventana de búsqueda.
  - `ProgressLabel.java`: Componente visual personalizado.
- **Vistas y Estilos:** `src/main/resources/aplicacion/practica3_jaimecasanova/`
  - `practica3View.fxml`: Vista principal del editor.
  - `buscarView.fxml`: Vista de la ventana de búsqueda.
  - `styles.css`: Hoja de estilos principal.

## 5. Cómo Ejecutar el Proyecto

**Requisitos:**
- JDK 21 o superior.
- Apache Maven.

Abre un terminal en la raíz del proyecto (`Interfaces/practica3_jaimeCasanova`) y ejecuta:
```sh
mvn clean javafx:run
```
**Nota:** El `pom.xml` podría tener una `mainClass` incorrecta por defecto (`HelloApplication`). Si el comando falla, asegúrate de que en `pom.xml`, dentro de la configuración del `javafx-maven-plugin`, la `mainClass` sea `aplicacion.practica3_jaimecasanova/aplicacion.practica3_jaimecasanova.main`.

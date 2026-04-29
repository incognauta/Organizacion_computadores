/*********
 * AssemblerException.java – Excepción usada por el traductor y el
 * desensamblador para reportar errores de sintaxis o semántica en el
 * archivo de entrada. Lleva opcionalmente el número de línea donde se
 * detectó el problema, para que el mensaje al usuario sea preciso.
 * Autor 1: Juan Andres Salcedo
 * Autor 2:  Juan Manuel Escobar

 *********/

/**
 * Excepción propia del proyecto. Se lanza cuando el archivo de entrada
 * (.asm o .hack) contiene una construcción que el traductor no puede
 * interpretar. Es una excepción "checked" para forzar que los
 * orquestadores la manejen explícitamente y produzcan el mensaje
 * "Error en linea N: ..." que pide el enunciado.
 */
public class AssemblerException extends Exception {

    /** Numero de linea (1-indexado) donde ocurrio el error. -1 si no aplica. */
    private final int linea;

    /**
     * Crea la excepcion sin numero de linea conocido. La usan clases
     * como Code que no saben en que linea estan; el orquestador la
     * recaptura y agrega el numero antes de mostrarla al usuario.
     *
     * @param mensaje descripcion del error
     */
    public AssemblerException(String mensaje) {
        super(mensaje);
        this.linea = -1;
    }

    /**
     * Crea la excepcion con numero de linea ya incorporado al mensaje.
     *
     * @param mensaje descripcion del error
     * @param linea   numero de linea del archivo fuente (1-indexado)
     */
    public AssemblerException(String mensaje, int linea) {
        super(mensaje);
        this.linea = linea;
    }

    /**
     * @return numero de linea del archivo fuente, o -1 si no se conoce
     */
    public int getLinea() {
        return linea;
    }
}

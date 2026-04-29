/*********
 * SymbolTable.java – Tabla de simbolos del assembler Hack. Mantiene
 * un diccionario "simbolo -> direccion" que arranca con los simbolos
 * predefinidos por el lenguaje (R0..R15, SP, LCL, ARG, THIS, THAT,
 * SCREEN, KBD) y se extiende durante la traduccion con etiquetas
 * (pasada 1) y variables de usuario (pasada 2).
 *
 * Esta clase es un diccionario puro: NO sabe sintaxis del assembler,
 * NO abre archivos, NO decide cuando un simbolo es etiqueta o variable
 * (eso lo decide el orquestador). Es agnostica al lenguaje, lo unico
 * que la ata a Hack son los predefinidos cargados en el constructor.
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2:  Juan Manuel Escobar
 *********/

import java.util.HashMap;
import java.util.Map;

/**
 * Tabla de simbolos del traductor Hack.
 *
 * <p>Uso tipico desde el orquestador:</p>
 * <pre>
 *   SymbolTable tabla = new SymbolTable();
 *
 *   // Pasada 1: registrar etiquetas
 *   tabla.addEntry("LOOP", 10);
 *
 *   // Pasada 2: resolver simbolos
 *   if (tabla.contains("x")) {
 *       int dir = tabla.getAddress("x");
 *   } else {
 *       tabla.addEntry("x", proximaRam++);
 *   }
 * </pre>
 */
public class SymbolTable {

    /** Diccionario interno simbolo -> direccion (0..32767). */
    private final Map<String, Integer> tabla;

    /**
     * Crea una tabla nueva con los simbolos predefinidos del lenguaje
     * Hack ya cargados. Cada instancia es independiente: dos
     * traducciones distintas no comparten estado.
     */
    public SymbolTable() {
        this.tabla = new HashMap<>();
        cargarPredefinidos();
    }

    /**
     * Carga los simbolos que define el lenguaje Hack por defecto.
     * R0..R15 son alias de las direcciones 0..15 de la RAM. SP, LCL,
     * ARG, THIS y THAT son alias adicionales para registros virtuales
     * que usa el VM (son los mismos R0..R4). SCREEN y KBD apuntan al
     * mapa de memoria de pantalla y al teclado respectivamente.
     */
    private void cargarPredefinidos() {
        // R0..R15 -> 0..15
        for (int i = 0; i <= 15; i++) {
            tabla.put("R" + i, i);
        }
        // Alias de los primeros registros virtuales
        tabla.put("SP",   0);
        tabla.put("LCL",  1);
        tabla.put("ARG",  2);
        tabla.put("THIS", 3);
        tabla.put("THAT", 4);
        // I/O
        tabla.put("SCREEN", 16384);
        tabla.put("KBD",    24576);
    }

    /**
     * Registra un simbolo nuevo en la tabla.
     *
     * @param nombre    nombre del simbolo (etiqueta o variable)
     * @param direccion direccion asociada
     * @throws AssemblerException si el simbolo ya estaba registrado.
     *         Esto detecta etiquetas duplicadas como dos "(LOOP)" en
     *         el mismo programa fuente.
     */
    public void addEntry(String nombre, int direccion) throws AssemblerException {
        if (tabla.containsKey(nombre)) {
            throw new AssemblerException(
                "simbolo duplicado: '" + nombre + "' ya estaba definido");
        }
        tabla.put(nombre, direccion);
    }

    /**
     * @param nombre simbolo a consultar
     * @return true si el simbolo ya esta en la tabla
     */
    public boolean contains(String nombre) {
        return tabla.containsKey(nombre);
    }

    /**
     * Devuelve la direccion asociada al simbolo.
     *
     * @param nombre simbolo a consultar
     * @return direccion entre 0 y 32767
     * @throws AssemblerException si el simbolo no existe. En un flujo
     *         correcto del orquestador esto no deberia ocurrir, pero
     *         lanzamos error explicito en lugar de devolver -1 o
     *         comportamiento silencioso.
     */
    public int getAddress(String nombre) throws AssemblerException {
        Integer dir = tabla.get(nombre);
        if (dir == null) {
            throw new AssemblerException(
                "simbolo no definido: '" + nombre + "'");
        }
        return dir;
    }
}

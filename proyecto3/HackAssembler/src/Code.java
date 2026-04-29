/*********
 * Code.java – Tabla de traduccion entre mnemonicos del assembler Hack
 * y su representacion binaria (y viceversa). Cubre los tres campos de
 * una instruccion tipo C: comp, dest y jump. Incluye la extension del
 * shift (<<1 y >>1) que agregamos en el Proyecto 2.
 *
 * Esta clase es PURA: no abre archivos, no parsea lineas, no asigna
 * direcciones. Es un diccionario consultable en ambas direcciones.
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2:  Juan Manuel Escobar
 *********/

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsula las tablas de traduccion del lenguaje Hack. Expone metodos
 * directos (mnemonico -> bits) usados por el traductor e inversos
 * (bits -> mnemonico) usados por el desensamblador.
 *
 * Para el campo comp se devuelven 7 bits: el bit "a" concatenado con
 * los 6 bits "cccccc". Esto evita que el llamador tenga que preguntar
 * dos cosas separadas y es como aparece la instruccion en el binario:
 *
 *     1 1 1   a c c c c c c   d d d   j j j
 *               ^^^^^^^^^^^
 *               7 bits que devuelve comp()
 */
public final class Code {

    // ----------------------------------------------------------------
    // Tablas hacia adelante (mnemonico -> bits)
    // ----------------------------------------------------------------

    /** Tabla comp: mnemonico -> 7 bits (bit a + 6 bits cccccc). */
    private static final Map<String, String> COMP = new HashMap<>();

    /** Tabla dest: mnemonico -> 3 bits ddd. */
    private static final Map<String, String> DEST = new HashMap<>();

    /** Tabla jump: mnemonico -> 3 bits jjj. */
    private static final Map<String, String> JUMP = new HashMap<>();

    // ----------------------------------------------------------------
    // Tablas inversas (bits -> mnemonico). Se construyen automaticamente
    // a partir de las tablas directas, asi una sola fuente de verdad.
    // ----------------------------------------------------------------

    private static final Map<String, String> COMP_INV = new HashMap<>();
    private static final Map<String, String> DEST_INV = new HashMap<>();
    private static final Map<String, String> JUMP_INV = new HashMap<>();

    static {
        // ============================================================
        // COMP: instrucciones aritmetico-logicas estandar de la ALU.
        // Bit "a" decide si el segundo operando es A (a=0) o M (a=1).
        // Las funciones que solo usan D no dependen de "a", pero por
        // convencion del libro Nand2Tetris se codifican con a=0.
        // ============================================================

        // a = 0 (operando es A o constante)
        COMP.put("0",   "0101010");
        COMP.put("1",   "0111111");
        COMP.put("-1",  "0111010");
        COMP.put("D",   "0001100");
        COMP.put("A",   "0110000");
        COMP.put("!D",  "0001101");
        COMP.put("!A",  "0110001");
        COMP.put("-D",  "0001111");
        COMP.put("-A",  "0110011");
        COMP.put("D+1", "0011111");
        COMP.put("A+1", "0110111");
        COMP.put("D-1", "0001110");
        COMP.put("A-1", "0110010");
        COMP.put("D+A", "0000010");
        COMP.put("D-A", "0010011");
        COMP.put("A-D", "0000111");
        COMP.put("D&A", "0000000");
        COMP.put("D|A", "0010101");

        // a = 1 (operando es M = RAM[A])
        COMP.put("M",   "1110000");
        COMP.put("!M",  "1110001");
        COMP.put("-M",  "1110011");
        COMP.put("M+1", "1110111");
        COMP.put("M-1", "1110010");
        COMP.put("D+M", "1000010");
        COMP.put("D-M", "1010011");
        COMP.put("M-D", "1000111");
        COMP.put("D&M", "1000000");
        COMP.put("D|M", "1010101");

        // ============================================================
        // EXTENSION SHIFT (Proyecto 2).
        // Activacion en la ALU: zx=0, nx=0, zy=0, ny=0, no=1.
        //   f = 0 -> shift LEFT  -> cccccc = 000001
        //   f = 1 -> shift RIGHT -> cccccc = 000011
        // El bit "a" sigue distinguiendo A/D (a=0) vs M (a=1).
        // OJO: A<<1 y D<<1 producen el mismo binario porque la ALU
        // extendida no tiene un bit adicional para distinguirlos
        // cuando a=0. Esto es intencional segun el design.txt.
        // ============================================================

        COMP.put("A<<1", "0000001");
        COMP.put("D<<1", "0000001");  // mismo binario que A<<1
        COMP.put("M<<1", "1000001");

        COMP.put("A>>1", "0000011");
        COMP.put("D>>1", "0000011");  // mismo binario que A>>1
        COMP.put("M>>1", "1000011");

        // ============================================================
        // DEST: que registro(s) reciben el resultado de comp.
        // Bits ddd = (d1=A, d2=D, d3=M). Se prenden los que aplican.
        // ============================================================

        DEST.put("",    "000");  // sin destino
        DEST.put("M",   "001");
        DEST.put("D",   "010");
        DEST.put("MD",  "011");
        DEST.put("A",   "100");
        DEST.put("AM",  "101");
        DEST.put("AD",  "110");
        DEST.put("AMD", "111");

        // ============================================================
        // JUMP: condicion de salto basada en el resultado de comp.
        // ============================================================

        JUMP.put("",    "000");  // sin salto
        JUMP.put("JGT", "001");
        JUMP.put("JEQ", "010");
        JUMP.put("JGE", "011");
        JUMP.put("JLT", "100");
        JUMP.put("JNE", "101");
        JUMP.put("JLE", "110");
        JUMP.put("JMP", "111");

        // ============================================================
        // Construir tablas inversas. Para COMP, la primera entrada
        // que se inserte para un binario dado se queda como la
        // "canonica" en la traduccion inversa. Por eso insertamos
        // las versiones con A antes que las con D al hacer putIfAbsent:
        // asi A<<1 (a=0) queda como traduccion canonica, no D<<1.
        // ============================================================

        for (Map.Entry<String, String> e : COMP.entrySet()) {
            COMP_INV.putIfAbsent(e.getValue(), e.getKey());
        }
        // Forzar canonicas para shift (A en lugar de D) por si el
        // orden de iteracion del HashMap puso D primero.
        COMP_INV.put("0000001", "A<<1");
        COMP_INV.put("0000011", "A>>1");

        for (Map.Entry<String, String> e : DEST.entrySet()) {
            DEST_INV.put(e.getValue(), e.getKey());
        }
        for (Map.Entry<String, String> e : JUMP.entrySet()) {
            JUMP_INV.put(e.getValue(), e.getKey());
        }
    }

    /** Constructor privado: clase utilitaria, no se instancia. */
    private Code() {}

    // ----------------------------------------------------------------
    // API: mnemonico -> bits (modo assembler)
    // ----------------------------------------------------------------

    /**
     * Traduce el mnemonico de comp a sus 7 bits (a + cccccc).
     *
     * @param mnemonico texto exacto, ej. "D+A", "M<<1", "0"
     * @return cadena de 7 caracteres '0'/'1'
     * @throws AssemblerException si el mnemonico no existe en la tabla
     */
    public static String comp(String mnemonico) throws AssemblerException {
        String bits = COMP.get(mnemonico);
        if (bits == null) {
            throw new AssemblerException("comp invalido: '" + mnemonico + "'");
        }
        return bits;
    }

    /**
     * Traduce el mnemonico de dest a sus 3 bits ddd.
     * null o cadena vacia equivalen a "sin destino" (000).
     *
     * @param mnemonico texto exacto, ej. "AM", "D", null
     * @return cadena de 3 caracteres '0'/'1'
     * @throws AssemblerException si el mnemonico no es valido
     */
    public static String dest(String mnemonico) throws AssemblerException {
        if (mnemonico == null) {
            return "000";
        }
        String bits = DEST.get(mnemonico);
        if (bits == null) {
            throw new AssemblerException("dest invalido: '" + mnemonico + "'");
        }
        return bits;
    }

    /**
     * Traduce el mnemonico de jump a sus 3 bits jjj.
     * null o cadena vacia equivalen a "sin salto" (000).
     *
     * @param mnemonico texto exacto, ej. "JMP", "JEQ", null
     * @return cadena de 3 caracteres '0'/'1'
     * @throws AssemblerException si el mnemonico no es valido
     */
    public static String jump(String mnemonico) throws AssemblerException {
        if (mnemonico == null) {
            return "000";
        }
        String bits = JUMP.get(mnemonico);
        if (bits == null) {
            throw new AssemblerException("jump invalido: '" + mnemonico + "'");
        }
        return bits;
    }

    // ----------------------------------------------------------------
    // API: bits -> mnemonico (modo desensamblador)
    // ----------------------------------------------------------------

    /**
     * Traduce 7 bits (a + cccccc) al mnemonico canonico de comp.
     * Para los binarios del shift con a=0 devuelve la forma con A
     * (no con D), por convencion documentada en DESIGN.md.
     *
     * @param bits cadena de exactamente 7 caracteres '0'/'1'
     * @return mnemonico, ej. "D+A", "A<<1"
     * @throws AssemblerException si los bits no corresponden a una
     *                            operacion conocida
     */
    public static String compToMnemonic(String bits) throws AssemblerException {
        String mnem = COMP_INV.get(bits);
        if (mnem == null) {
            throw new AssemblerException("comp binario desconocido: '" + bits + "'");
        }
        return mnem;
    }

    /**
     * Traduce 3 bits ddd al mnemonico de dest. Devuelve cadena
     * vacia si los bits son "000" (sin destino).
     *
     * @param bits cadena de exactamente 3 caracteres '0'/'1'
     * @return mnemonico, ej. "AM", "" para sin destino
     * @throws AssemblerException si los bits no son validos
     */
    public static String destToMnemonic(String bits) throws AssemblerException {
        String mnem = DEST_INV.get(bits);
        if (mnem == null) {
            throw new AssemblerException("dest binario desconocido: '" + bits + "'");
        }
        return mnem;
    }

    /**
     * Traduce 3 bits jjj al mnemonico de jump. Devuelve cadena
     * vacia si los bits son "000" (sin salto).
     *
     * @param bits cadena de exactamente 3 caracteres '0'/'1'
     * @return mnemonico, ej. "JMP", "" para sin salto
     * @throws AssemblerException si los bits no son validos
     */
    public static String jumpToMnemonic(String bits) throws AssemblerException {
        String mnem = JUMP_INV.get(bits);
        if (mnem == null) {
            throw new AssemblerException("jump binario desconocido: '" + bits + "'");
        }
        return mnem;
    }
}

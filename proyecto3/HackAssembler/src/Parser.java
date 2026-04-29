/*********
 * Parser.java – Lector sintactico del assembler Hack. Carga un archivo
 * .asm en memoria, descarta comentarios y lineas en blanco, y expone
 * un iterador que para cada "instruccion real" permite consultar:
 *   - su tipo (A, C o L-etiqueta),
 *   - los campos correspondientes (symbol, dest, comp, jump).
 *
 * El parser NO traduce a binario y NO conoce simbolos. Solo entiende
 * sintaxis. Reconoce las extensiones de shift (<<1 y >>1) porque toma
 * el campo comp como string completo sin tokenizar internamente.
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2:  Juan Manuel Escobar
 *********/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser de archivos .asm Hack. El uso tipico es de dos pasadas:
 * una para registrar etiquetas y otra para traducir instrucciones,
 * usando reset() para volver al inicio entre pasadas.
 */
public class Parser {

    /** Tipos de instruccion del assembler Hack. */
    public enum InstructionType {
        /** @valor o @simbolo */
        A_INSTRUCTION,
        /** dest=comp;jump y variantes */
        C_INSTRUCTION,
        /** (LABEL) */
        L_INSTRUCTION
    }

    /** Lineas relevantes (sin blancos ni comentarios), ya recortadas. */
    private final List<String> lineas;

    /** Numero de linea original (1-indexado) para mensajes de error. */
    private final List<Integer> numerosOriginales;

    /** Indice actual. -1 antes del primer advance(). */
    private int idx;

    /**
     * Lee el archivo, descarta comentarios y lineas vacias, y conserva
     * la correspondencia con el numero de linea original para reportar
     * errores con precision.
     *
     * @param ruta camino al archivo .asm
     * @throws IOException si no se puede leer el archivo
     */
    public Parser(String ruta) throws IOException {
        this.lineas = new ArrayList<>();
        this.numerosOriginales = new ArrayList<>();
        this.idx = -1;

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int numero = 0;
            while ((linea = br.readLine()) != null) {
                numero++;
                String limpia = limpiar(linea);
                if (!limpia.isEmpty()) {
                    lineas.add(limpia);
                    numerosOriginales.add(numero);
                }
            }
        }
    }

    /**
     * Quita comentarios "// ..." y espacios en blanco. Conserva el
     * resto de la linea ya recortada.
     *
     * @param linea texto original
     * @return texto sin comentarios ni espacios; cadena vacia si la
     *         linea no tenia codigo
     */
    private static String limpiar(String linea) {
        int idxComentario = linea.indexOf("//");
        if (idxComentario != -1) {
            linea = linea.substring(0, idxComentario);
        }
        // Quitar todos los espacios en blanco internos: el assembler
        // Hack no los necesita y simplifica el parsing del campo comp
        // (por ejemplo, "D + A" y "D+A" son la misma operacion).
        return linea.replaceAll("\\s+", "");
    }

    /**
     * @return true si queda al menos una instruccion por procesar
     */
    public boolean hasMoreLines() {
        return idx + 1 < lineas.size();
    }

    /**
     * Avanza a la siguiente instruccion. Debe llamarse antes de la
     * primera consulta.
     */
    public void advance() {
        idx++;
    }

    /**
     * Reinicia el iterador para una segunda pasada.
     */
    public void reset() {
        idx = -1;
    }

    /**
     * @return numero de linea original (1-indexado) de la instruccion actual
     */
    public int currentLineNumber() {
        return numerosOriginales.get(idx);
    }

    /**
     * @return texto crudo de la instruccion actual (sin comentarios ni espacios)
     */
    public String currentLine() {
        return lineas.get(idx);
    }

    /**
     * Determina el tipo de la instruccion actual.
     *
     * @return tipo de instruccion
     * @throws AssemblerException si la linea no es ningun tipo valido
     */
    public InstructionType instructionType() throws AssemblerException {
        String l = currentLine();
        if (l.startsWith("@")) {
            return InstructionType.A_INSTRUCTION;
        }
        if (l.startsWith("(") && l.endsWith(")")) {
            return InstructionType.L_INSTRUCTION;
        }
        // Si tiene '=' o ';', o es una expresion sola, la tomamos como C.
        // No validamos aqui el contenido: eso lo hace Code al traducir.
        return InstructionType.C_INSTRUCTION;
    }

    /**
     * Para A_INSTRUCTION devuelve lo que hay despues de "@".
     * Para L_INSTRUCTION devuelve lo que hay entre parentesis.
     *
     * @return simbolo o numero como string
     * @throws AssemblerException si se llama sobre una C_INSTRUCTION
     */
    public String symbol() throws AssemblerException {
        String l = currentLine();
        if (l.startsWith("@")) {
            String s = l.substring(1);
            if (s.isEmpty()) {
                throw new AssemblerException(
                    "instruccion @ vacia", currentLineNumber());
            }
            return s;
        }
        if (l.startsWith("(") && l.endsWith(")")) {
            String s = l.substring(1, l.length() - 1);
            if (s.isEmpty()) {
                throw new AssemblerException(
                    "etiqueta vacia ()", currentLineNumber());
            }
            return s;
        }
        throw new AssemblerException(
            "symbol() en instruccion no-A/no-L", currentLineNumber());
    }

    /**
     * Devuelve el campo dest de una instruccion C. Cadena vacia si no
     * hay '=' (no hay destino).
     *
     * @return mnemonico de dest o ""
     */
    public String dest() {
        String l = currentLine();
        int eq = l.indexOf('=');
        if (eq == -1) {
            return "";
        }
        return l.substring(0, eq);
    }

    /**
     * Devuelve el campo comp de una instruccion C. Es la parte entre
     * '=' (excluyendo) y ';' (excluyendo). Si no hay '=' empieza
     * desde el inicio. Si no hay ';' va hasta el final.
     *
     * @return mnemonico de comp (incluye notacion shift como "M{@literal <<}1")
     */
    public String comp() {
        String l = currentLine();
        int eq = l.indexOf('=');
        int sc = l.indexOf(';');
        int inicio = (eq == -1) ? 0 : eq + 1;
        int fin = (sc == -1) ? l.length() : sc;
        return l.substring(inicio, fin);
    }

    /**
     * Devuelve el campo jump de una instruccion C. Cadena vacia si
     * no hay ';' (no hay salto).
     *
     * @return mnemonico de jump o ""
     */
    public String jump() {
        String l = currentLine();
        int sc = l.indexOf(';');
        if (sc == -1) {
            return "";
        }
        return l.substring(sc + 1);
    }
}

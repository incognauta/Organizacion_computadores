/*********
 * HackDisassembler.java – Desensamblador. Lee un archivo .hack
 * (texto, una linea de 16 bits por instruccion) y produce el
 * archivo .asm equivalente, traduciendo cada linea a su sintaxis
 * de assembler Hack.
 *
 * Es de UNA sola pasada: el .hack ya tiene direcciones resueltas,
 * no hay etiquetas ni variables simbolicas, asi que cada instruccion
 * se traduce de forma independiente sin necesitar contexto.
 *
 * Decision documentada: para los binarios de shift con a=0 (que son
 * ambiguos entre A<<1 y D<<1) se elige la forma con A por convencion.
 * El comportamiento del programa no cambia porque el binario es el
 * mismo; solo cambia el texto que se imprime.
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2:  Juan Manuel Escobar
 *********/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Desensamblador Hack: traduce .hack a .asm.
 */
public class HackDisassembler {

    /**
     * Desensambla un archivo .hack y escribe el .asm correspondiente.
     * El nombre de salida se deriva del de entrada: "Prog.hack" produce
     * "ProgDis.asm".
     *
     * @param rutaHack camino al archivo de entrada
     * @throws IOException        si la lectura/escritura falla
     * @throws AssemblerException si una linea no tiene 16 bits validos
     */
    public static void disassemble(String rutaHack)
            throws IOException, AssemblerException {

        File entrada = new File(rutaHack);
        if (!entrada.exists() || !entrada.isFile()) {
            throw new AssemblerException(
                "archivo no encontrado: " + rutaHack);
        }

        String rutaAsm = derivarNombreSalida(rutaHack);
        File salida = new File(rutaAsm);

        try (BufferedReader br = new BufferedReader(new FileReader(entrada));
             BufferedWriter bw = new BufferedWriter(
                 new OutputStreamWriter(
                     new FileOutputStream(salida),
                     StandardCharsets.US_ASCII))) {

            String linea;
            int numero = 0;
            while ((linea = br.readLine()) != null) {
                numero++;
                String limpia = linea.trim();
                if (limpia.isEmpty()) {
                    // Lineas vacias se omiten en la salida
                    continue;
                }

                String asm = traducirLinea(limpia, numero);
                bw.write(asm);
                bw.write('\n');
            }
        } catch (AssemblerException | IOException ex) {
            if (salida.exists()) {
                salida.delete();
            }
            throw ex;
        }
    }

    /**
     * Traduce una linea de 16 bits a su sintaxis assembler.
     *
     * @param bits   cadena de 16 caracteres '0'/'1'
     * @param numero numero de linea original (para mensajes de error)
     * @return texto en assembler Hack
     * @throws AssemblerException si la linea es invalida
     */
    private static String traducirLinea(String bits, int numero)
            throws AssemblerException {
        if (bits.length() != 16) {
            throw new AssemblerException(
                "se esperaban 16 bits, llegaron " + bits.length(), numero);
        }
        for (int i = 0; i < 16; i++) {
            char c = bits.charAt(i);
            if (c != '0' && c != '1') {
                throw new AssemblerException(
                    "caracter invalido '" + c + "' en linea binaria",
                    numero);
            }
        }

        if (bits.charAt(0) == '0') {
            return traducirA(bits);
        }
        return traducirC(bits, numero);
    }

    /**
     * Traduce una instruccion tipo A (bits[0] == 0). Los bits[1..15]
     * son el valor en binario, big-endian.
     */
    private static String traducirA(String bits) {
        String valor = bits.substring(1);
        int n = Integer.parseInt(valor, 2);
        return "@" + n;
    }

    /**
     * Traduce una instruccion tipo C: 111 a cccccc ddd jjj
     * Se reconstruye dest=comp;jump segun los campos presentes.
     */
    private static String traducirC(String bits, int numero)
            throws AssemblerException {
        // Validar prefijo 111. Si no es 111, los bits 1 y 2 estan
        // "no especificados" en la ISA Hack original; los toleramos
        // pero seguimos asumiendo formato C porque el bit 0 es 1.
        // (No exigimos que sean 1 estrictamente porque algunos
        // ensambladores reservan esos bits para extensiones futuras.)
        String compBits = bits.substring(3, 10);   // a + cccccc (7 bits)
        String destBits = bits.substring(10, 13);  // ddd
        String jumpBits = bits.substring(13, 16);  // jjj

        try {
            String comp = Code.compToMnemonic(compBits);
            String dest = Code.destToMnemonic(destBits);
            String jump = Code.jumpToMnemonic(jumpBits);

            StringBuilder sb = new StringBuilder();
            if (!dest.isEmpty()) {
                sb.append(dest).append('=');
            }
            sb.append(comp);
            if (!jump.isEmpty()) {
                sb.append(';').append(jump);
            }
            return sb.toString();
        } catch (AssemblerException ex) {
            throw new AssemblerException(ex.getMessage(), numero);
        }
    }

    /**
     * Cambia la extension .hack a Dis.asm. Por ejemplo
     * "Prog.hack" -> "ProgDis.asm".
     */
    private static String derivarNombreSalida(String rutaHack) {
        if (rutaHack.toLowerCase().endsWith(".hack")) {
            return rutaHack.substring(0, rutaHack.length() - 5) + "Dis.asm";
        }
        return rutaHack + "Dis.asm";
    }
}

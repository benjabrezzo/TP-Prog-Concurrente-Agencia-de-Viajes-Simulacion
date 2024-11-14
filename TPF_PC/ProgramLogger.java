import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgramLogger {

    private Integer transicionReg;
    private static final String FILE_INVARIANTES = "invariantesDePlaza.log";
    private static final String FILE_NAME = "transiciones.log"; // Nombre del archivo donde se guardarán las transiciones
    private StringBuilder logContent = new StringBuilder(); // Usamos StringBuilder para acumular las transiciones en memoria
    private StringBuilder logContent2 = new StringBuilder();
    private HashMap<Integer,Integer> cuentaFinal = new HashMap<>();
    private ArrayList<Integer> estadoAct;
    private long runStartedAt; // Tiempo aproximado de la actual ejecución
    private int[] invariantsCount = new int[4]; // Estado persistente para los conteos de invariantes

    private static final Pattern INVARIANTE_PATTERN = Pattern.compile("(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)");

    public ProgramLogger() {
        this.runStartedAt = System.currentTimeMillis();
    }

    /**
     * Este método registra la transición en un archivo .log y en memoria, y verifica los patrones.
     */
    public void logTransicion(Integer transicion) {
        this.transicionReg = transicion;
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print("T" + transicionReg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }

        logContent.append("T").append(transicionReg);
        logContent2.append("T").append(transicionReg);

        if (revisarDisparoFinal()) {
            System.out.printf("\n\nTiempo de ejecución: ~" + ((System.currentTimeMillis() - runStartedAt)) + " ms.\n");
            //System.out.println("\nRESFINAL: "+ logContent);
            imprimirFinal();
            resFinal();
        }
        /*
        try {
            updateInvariants();  // Actualizar los invariantes después de cada registro
        } catch (IOException e) {
            System.err.println("Error al actualizar los invariantes: " + e.getMessage());
        }
        */
    }
    /*
        private void updateInvariants() throws IOException {
            String currentData = logContent.toString();
            StringBuilder podado = new StringBuilder();

            Matcher matcher;
            boolean found;

            do {
                found = false;
                matcher = INVARIANTE_PATTERN.matcher(currentData);
                if (matcher.find()) {
                    // Poda la cadena
                    String result = matcherReplacement(matcher);
                    podado.append(matcher.group(0));
                    //System.out.println("PODADO " + podado);
                    currentData = matcher.replaceFirst(result);
                    found = true;
                }
            } while (found);

            // Verificar y contar los invariantes en la cadena podada
            matcher = INVARIANTE_PATTERN.matcher(podado.toString());
            while (matcher.find()) {
                if (matcher.group(6) != null && matcher.group(14) != null) {
                    invariantsCount[3]++;
                } else if (matcher.group(6) != null) {
                    invariantsCount[2]++;
                } else if (matcher.group(14) != null) {
                    invariantsCount[1]++;
                } else {
                    invariantsCount[0]++;
                }
                podado = new StringBuilder(matcher.replaceFirst(""));
                matcher = INVARIANTE_PATTERN.matcher(podado.toString());
            }

            logContent = new StringBuilder(currentData);

            // Mostrar los resultados finales

            System.out.println("Total coincidencias INVARIANTE 1: " + invariantsCount[0]);
            System.out.println("Total coincidencias INVARIANTE 2: " + invariantsCount[1]);
            System.out.println("Total coincidencias INVARIANTE 3: " + invariantsCount[2]);
            System.out.println("Total coincidencias INVARIANTE 4: " + invariantsCount[3]);
            System.out.println("Remaining string: '" + logContent + "'");

        }

        private String matcherReplacement(Matcher m) {
            return m.group(2) + m.group(4) + (m.group(6) != null ? m.group(7) : m.group(10)) + m.group(12) + (m.group(14) != null ? m.group(15) + m.group(17) : m.group(20)) + m.group(22);
        }
    */
    private static int countT11Matches(String text) {
        int count = 0;
        int index = text.indexOf("T11");
        while (index >= 0) {
            count++;
            index = text.indexOf("T11", index + 1);
        }
        return count;
    }

    public void resFinal(){
        System.exit(0);
    }

    /**
     * Método para registrar los resultados de las invariantes de plaza.
     */
    public void logInvariante(String message) {
        try (FileWriter fw = new FileWriter(FILE_INVARIANTES, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de invariantes: " + e.getMessage());
        }
    }

    /**
     * Método para limpiar el archivo de transiciones al inicio de la aplicación.
     */
    public void clearTransicionesLog() {
        try (FileWriter fw = new FileWriter(FILE_NAME, false); // false para sobrescribir el archivo
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(""); // Sobrescribir con contenido vacío
        } catch (IOException e) {
            System.err.println("Error al limpiar el archivo de transiciones: " + e.getMessage());
        }
    }

    /**
     * Método para limpiar el archivo de invariantes de plaza al inicio de la aplicación.
     */
    public void clearInvarianteLog() {
        try (FileWriter fw = new FileWriter(FILE_INVARIANTES, false); // false para sobrescribir el archivo
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(""); // Sobrescribir con contenido vacío
        } catch (IOException e) {
            System.err.println("Error al limpiar el archivo de invariantes: " + e.getMessage());
        }
    }

    public boolean revisarDisparoFinal(){
        return countT11Matches(logContent2.toString()) == 186;
    }

    public void actulizarFinal(HashMap<Integer,Integer> f, ArrayList<Integer> e){
        estadoAct = e;
        cuentaFinal = f;
    }

    public void imprimirFinal(){
        System.out.println(estadoAct.toString());
        System.out.println(cuentaFinal.toString());
    }

}

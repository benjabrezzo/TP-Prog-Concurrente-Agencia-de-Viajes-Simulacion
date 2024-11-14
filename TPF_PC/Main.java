import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        int[][] matrizIncidencia = {
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {-1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0},
                {-1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0},
                {0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 1, -1, -1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, -1, -1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, -1}
        };

        Integer[] marcadoInicial = {5,1,0,0,5,0,1,1,0,0,1,0,0,0,0};
        ArrayList<Integer> listaMarcadoInicial = new ArrayList<>(Arrays.asList(marcadoInicial));

        //La cantidad de filas son la cantidad de plazas y la cantidad de columnas son la cantidad de transiciones
        int cantPlazas = matrizIncidencia.length, cantTransiciones = matrizIncidencia[0].length;

        //creamos un objeto para imprimir resultados y manejar condiciones apartir de los mismos
        ProgramLogger log = new ProgramLogger();
        log.clearInvarianteLog();
        log.clearTransicionesLog();

        // Creamos una instacia de la Red de Petri:
        RedDePetri rdp = new RedDePetri(cantPlazas, cantTransiciones, matrizIncidencia, listaMarcadoInicial, log);
        Colas colaDeCondicion = new Colas();
        Politica.initialize(Politica.TipoDePolitica.BALANCEADA);



        //Vemos la responsabilidad de cada hilo en esta red segun lo pedido en la consigna
        HashMap<String, ArrayList<Integer>> responsabilidades = new HashMap<>();

        ArrayList<Integer> transicionesHilo0 = new ArrayList<>();
        ArrayList<Integer> transicionesHilo1 = new ArrayList<>();
        ArrayList<Integer> transicionesHilo2 = new ArrayList<>();
        ArrayList<Integer> transicionesHilo3 = new ArrayList<>();
        ArrayList<Integer> transicionesHilo4 = new ArrayList<>();
        ArrayList<Integer> transicionesHilo5 = new ArrayList<>();

        transicionesHilo0.add(0);
        transicionesHilo0.add(1);
        transicionesHilo1.add(2);
        transicionesHilo1.add(5);
        transicionesHilo2.add(3);
        transicionesHilo2.add(4);
        transicionesHilo3.add(6);
        transicionesHilo3.add(9);
        transicionesHilo3.add(10);
        transicionesHilo4.add(7);
        transicionesHilo4.add(8);
        transicionesHilo5.add(11);


        responsabilidades.put("Hilo0", transicionesHilo0);
        responsabilidades.put("Hilo1", transicionesHilo1);
        responsabilidades.put("Hilo2", transicionesHilo2);
        responsabilidades.put("Hilo3", transicionesHilo3);
        responsabilidades.put("Hilo4", transicionesHilo4);
        responsabilidades.put("Hilo5", transicionesHilo5);
        //Inicializamos responsabilidades e iniciamos los hilos

        //Creamos los hilos deacuerdo al analisis del paper
        HilosRed hilosUsados = new HilosRed(rdp, colaDeCondicion, responsabilidades);

        hilosUsados.getInicio();
        while (true) {
            rdp.getInvaraintesPlaza();
            if (log.revisarDisparoFinal()) {
                hilosUsados.detenerTodosLosHilos();
            }
        }
    }
}
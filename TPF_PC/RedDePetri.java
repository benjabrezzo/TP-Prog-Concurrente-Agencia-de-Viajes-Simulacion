import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedDePetri {
    public int[][] matrizIncidencia;
    public int cantPlazas;
    public int cantTransiciones;


    public HashMap<Integer,Integer> transiciones = new HashMap<>();

    /**
     * estadoPosible es una variable donde se guarda el estado que tendrá la red cuando se dispare una transición, este
     * estado se obtendrá del resultado de la ecuación fundamental, pero puede ocurrir que esta transición no se pueda disparar,
     * esto se verá sin en el vector queda un negativo. Para evitar cambiar el estadoActual primero se guarda en esta variable y si
     * está todo bien (no hay negativos) se actualizará estado actual.
     **/
    public ArrayList<Integer> estadoPosible = new ArrayList<>(cantPlazas);

    // En el constructor se inicializa con la marca inicial porque esa va a ser la marca actual al inicio, pero irá
    // cambiando cada vez que se realice un disparo:
    public ArrayList<Integer> estadoActual;
    //Archivo de salida para verificar red
    private ProgramLogger salida;
    private Integer transicion;
    private static final Map<Integer, Long> transicionesConTiempo = new HashMap<>();

    static{
        transicionesConTiempo.put(1,50L);
        transicionesConTiempo.put(4,125L);
        transicionesConTiempo.put(5,125L);
        transicionesConTiempo.put(8,50L); // Con este alpha las politicas junto con la cantidad de transiciones se cumple de una manera esperada, casi que ideal, pero no se asemeja a la realidad ese tiempo.
        transicionesConTiempo.put(9,75L);
        transicionesConTiempo.put(10,95L);
    }

    public enum DisparoStatus{
        DISPARE_TRUE,
        NO_SENSIBILIZADA,
        FUERA_VENTANA_TIEMPO
    }



    // Constructor con dimensiones iniciales
    public RedDePetri(int cantPlazas, int cantTransiciones, int[][] matrizIncidencia, ArrayList<Integer> estadoInicial, ProgramLogger logger) {
        this.matrizIncidencia = matrizIncidencia;
        this.cantPlazas = cantPlazas;
        this.cantTransiciones = cantTransiciones;
        this.estadoActual = estadoInicial;
        this.salida = logger;
        for (int i=0; i<cantTransiciones; i++ ) {
            transiciones.put(i,0);
        }
    }

    public DisparoStatus disparar(int transicion){
        this.transicion = transicion;
        // boolean disparar = false; CREO QUE NO HACE FALTA
        ArrayList<Integer> transicionADisparar = creaVectorTransicion(transicion);
        long alpha = alphaTransicion(transicion); // alpha de la transicion actual

        // Actualiza estadoPosible
        estadoPosible = actualizarEstado(transicionADisparar);

        // Verificamos si se puede disparar
        if(!verificarDisparo(estadoPosible)){
            long tiempoDeSensibilizado = System.currentTimeMillis(); // Tiempo de sensibilizado (time stamp)
            // Verificamos si estamos dentro de la ventana de tiempo de disparo de la transicion
            if(testVentanaTiempo(alpha, tiempoDeSensibilizado)){
                return DisparoStatus.DISPARE_TRUE; // Estamos dentro de la ventana
            } else {
                return DisparoStatus.FUERA_VENTANA_TIEMPO; // Estamos fuera de la ventana
            }
        }
        return DisparoStatus.NO_SENSIBILIZADA;
    }

    // El siguiente metodo solo se llama si es un hilo que se durmio y luego volvio a ingresar al Monitor para verificar
    // que siga sensibilizada la transicion. No hace falta volver a verificar el tiempo.
    public DisparoStatus verificarDisparoSensibilizado(int transicion){
        this.transicion = transicion;
        ArrayList<Integer> transicionADisparar = creaVectorTransicion(transicion);
        estadoPosible = actualizarEstado(transicionADisparar);

        if(!verificarDisparo(estadoPosible)){
            return DisparoStatus.DISPARE_TRUE;
        } else {
            return DisparoStatus.NO_SENSIBILIZADA;
        }
    }




    /* PROBABLEMENTE A BORRAR:
    public boolean disparar(int transicion, Semaphore mutex) {
        // Se crea el vector que se utilizará para realizar la cuenta de la ecuación fundamental.
        // Este vector consta de todos 0, con un 1 en la posición que representa a la transición
        // a disparar. Por ejemplo, un 1 en la posición 2 representa que se intenta disparar la
        // transición 2.
        this.transicion = transicion;
        boolean disparar = false;
        ArrayList<Integer> transicionADisparar = creaVectorTransicion(transicion);
        long alpha = alphaTransicion(transicion); // alpha de la transicion actual

        // Actualiza estadoPosible:
        estadoPosible = actualizarEstado(transicionADisparar);

        // Verificamos si esta sensibilizadad:
        if(!verificarDisparo(estadoPosible)){
            long tiempoDeSensibilizado = System.currentTimeMillis(); // Tiempo de sensibilizado (timeStamp)

            // Verificamos si estamos dentro de la ventana de tiempo de disparo de la transicion
            if(testVentanaTiempo(alpha, tiempoDeSensibilizado)){
                // Si estamos dentro cambiamos el estado de disparar y se devuelve true al final
                disparar = true;

            } else {
                /*
                *  ACA HAY QUE AGREGAR LA LOGICA PARA HACERLE SABER AL PROGRAMA QUE NO SE PUEDE DISPARAR PORQUE NO ESTA EN LA
                *  VENTANA DE TIEMPO.
                *
            }
        }

        return disparar;
    }
    */


    /**
     *  El método senzibilizadas() solo se debería llamar LUEGO de que se actualizó el
     *  estado, es decir, luego de haber llamado setEstadoActual() en el Monitor.
     * */
    public ArrayList<Integer> sensibilizadas() {
        // Se busca que transiciones quedan habilitadas luego de que el hilo en el Monitor
        // lograra trabajar (y por ende cambiar el estado).
        ArrayList<Integer> sensibilizadas = new ArrayList<>();
        sensibilizadas.ensureCapacity(cantTransiciones);

        // Variables auxiliares para la simulación:
        ArrayList<Integer> transicionADispararAux;
        ArrayList<Integer> estadoPosibleAux;

        // Se simulan todas las transiciones para saber cuales quedan sensibilizadas. Cada iteración
        // del for será para cada transición.

        for(int i = 0; i < cantTransiciones; i++) {
            int transicion = i;
            transicionADispararAux = creaVectorTransicion(transicion);
            estadoPosibleAux = actualizarEstado(transicionADispararAux);

            // Agrega por defecto la transicion, supone que todas las transiciones estan sensibilizado. Mapea la
            // transicion con el indice.
            sensibilizadas.add(transicion, transicion);

            // Revisa que no haya negativo. Si lo hay esa transicion no se puede disparar y cambia el valor -1 en la
            // posicion del ArrayList sensibilizadas que coincide con la transicion.
            for(int j = 0; j < cantPlazas; j++) {
                if(estadoPosibleAux.get(j) < 0){
                    sensibilizadas.set(transicion, -1);
                    break;
                }
            }

        }
        return sensibilizadas;
    }

    public void setEstadoActual() {
        this.estadoActual = estadoPosible;
        this.transiciones.put(transicion,transiciones.get(transicion)+1);
        salida.actulizarFinal(transiciones, estadoActual);
        this.salida.logTransicion(this.transicion);
    }

    public HashMap<Integer, Integer> getTransicionesDisparadas(){
        return transiciones;
    }

    public void getInvaraintesPlaza() {
        StringBuilder resultado = new StringBuilder();
        if (estadoActual.size() != 15) {
            resultado.append("Error: tamaño incorrecto de estadoActual.\n");
        } else {
            // Verificación de cada invariante, y registro de resultados
            resultado.append("Plaza[1](" + estadoActual.get(1) + ") + Plaza[2](" + estadoActual.get(2) + ") = ").append((estadoActual.get(1) + estadoActual.get(2) == 1) ? "Correcto\n" : "Incorrecto\n");
            resultado.append("Plaza[10](" + estadoActual.get(10) + ") + Plaza[11](" + estadoActual.get(11) + ") + Plaza[12](" + estadoActual.get(12) + ") + Plaza[13](" + estadoActual.get(13) + ") = ").append((estadoActual.get(10) + estadoActual.get(11) + estadoActual.get(12) + estadoActual.get(13) == 1) ? "Correcto\n" : "Incorrecto\n");
            resultado.append("Gran invariante[0](" + estadoActual.get(0) + ") + [11](" + estadoActual.get(11) + ") + [12](" + estadoActual.get(12) + ") + [13](" + estadoActual.get(13) + ") + [14](" + estadoActual.get(14) + ") + [2](" + estadoActual.get(2) + ") + [3](" + estadoActual.get(3) + ") + [5](" + estadoActual.get(5) + ") + [8](" + estadoActual.get(8) + ") + [9](" + estadoActual.get(9) + ") = ").append((estadoActual.get(0) + estadoActual.get(11) + estadoActual.get(12) + estadoActual.get(13) + estadoActual.get(14) + estadoActual.get(2) + estadoActual.get(3) + estadoActual.get(5) + estadoActual.get(8) + estadoActual.get(9) == 5) ? "Correcto\n" : "Incorrecto\n");
            resultado.append("Plaza[2](" + estadoActual.get(2) + ") + Plaza[3](" + estadoActual.get(3) + ") + Plaza[4](" + estadoActual.get(4) + ") = ").append((estadoActual.get(2) + estadoActual.get(3) + estadoActual.get(4) == 5) ? "Correcto\n" : "Incorrecto\n");
            resultado.append("Plaza[5](" + estadoActual.get(5) + ") + Plaza[6](" + estadoActual.get(6) + ") = ").append((estadoActual.get(5) + estadoActual.get(6) == 1) ? "Correcto\n" : "Incorrecto\n");
            resultado.append("Plaza[7](" + estadoActual.get(7) + ") + Plaza[8](" + estadoActual.get(8) + ") = ").append((estadoActual.get(7) + estadoActual.get(8) == 1) ? "Correcto\n" : "Incorrecto\n");
        }
        salida.logInvariante(resultado.toString()); // Usar el método de log para guardar el resultado
    }

    public long alphaTransicion(int transicion){
        return transicionesConTiempo.getOrDefault(transicion,0L);
    }

    public long cuantoFalta(long tiempoDeSensibilizado, long alphaTransicion){
        long tiempoActual = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoActual - tiempoDeSensibilizado;
        return alphaTransicion - tiempoTranscurrido;
    }

    /*
    public void tiempoTransicion(int transicion, long tiempoDeSensibilizado){
        long alpha = alphaTransicion(transicion); // alpha de la transicion actual
        // si NO esta dentro de la ventana de disparo:
        if(!testVentanaTiempo(alpha, tiempoDeSensibilizado)){
            // Simula el tiempo poniendo a dormir el hilo
            try{
                Thread.sleep(cuantoFalta(tiempoDeSensibilizado,alpha));
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }*/

    // Private methods

    /**
     *  En este método se realiza la cuenta de la Ecuación Fundamental. El resultado de la cuenta
     *  será un vector que representa la nueva marca (estado) de la red una vez dispara la transición.
     * */
    private ArrayList<Integer> actualizarEstado(ArrayList<Integer> transicionesDisparar) {
        ArrayList<Integer> resultados = multiMat(transicionesDisparar);
        ArrayList<Integer> resultadoFInal = new ArrayList<>();

        // Verificar que ambos ArrayList tengan la misma longitud
        if (cantPlazas != resultados.size()) {
            System.out.println("ACT:ESTADO_ No se pueden sumar: los ArrayList tienen diferente longitud.");
        }

        // Sumar los elementos de los ArrayList
        for (int i = 0; i < cantPlazas; i++) {
            int suma = estadoActual.get(i) + resultados.get(i);
            resultadoFInal.add(suma); // Agregar el resultado al ArrayList de resultados
        }

        return resultadoFInal;
    }

    private ArrayList<Integer> multiMat(ArrayList<Integer> transicionADisparar) {
        ArrayList<Integer> resultados = new ArrayList<>();
        int calculo = 0;

        for(int i = 0; i < cantPlazas; i++) {
            for(int j = 0; j < cantTransiciones; j++) {
                calculo += matrizIncidencia[i][j] * transicionADisparar.get(j);
            }
            resultados.add(calculo);
            calculo = 0;
        }

        return resultados;
    }

    // Devuelve true si tiene negativo -> No se podría disparar
    // Devuelve false si no tiene negativo -> Si se podría disparar
    private boolean verificarDisparo(ArrayList<Integer> estadoPosible) {
        // Verificar si algún número es menor que cero usando forEach
        boolean tieneNegativo = false;

        for (Integer numero : estadoPosible) {
            if (numero < 0) {
                tieneNegativo = true;
                break;
            }
        }

        if (tieneNegativo) {
            estadoPosible.clear();
        }

        return tieneNegativo;
    }

    private ArrayList<Integer> creaVectorTransicion(int transicion){
        ArrayList<Integer> transicionADisparar = new ArrayList<>();
        transicionADisparar.ensureCapacity(cantTransiciones);
        for (int i = 0; i < cantTransiciones; i++) {
            if (transicion == i) {
                transicionADisparar.add(1);
            } else {
                transicionADisparar.add(0); // Agregar cero a cada posición
            }
        }
        return transicionADisparar;
    }


    private boolean testVentanaTiempo(long alpha, long tiempoDeSensibilizado){
        long tiempoActual = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoActual - tiempoDeSensibilizado;
        return tiempoTranscurrido >= alpha;
    }




}
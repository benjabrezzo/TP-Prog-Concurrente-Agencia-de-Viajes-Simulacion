import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Politica {
    public enum TipoDePolitica {
        BALANCEADA,
        PROCESAMIENTO_PRIORIZADO
    }
    private static TipoDePolitica tipoDePolitica;

    public static void initialize(TipoDePolitica tipoDePolitica) {
        Politica.tipoDePolitica = tipoDePolitica;
    }

    /*
        Se recibira un ArrayList con solo las transiciones que estan sensibilizadas y tienen hilos esperando.
        La prioridad será:
            1. T6 o T7
            2. T2 o T3
            3. El resto de transiciones tendran prioridad las "más grandes": T11 > T10 > T9 > ... > T1 > T0
            La razón de esta última es para que se cumpla más rápido una vuelta de la red.
     */
    public static int cual(ArrayList<Integer> interseccion, HashMap<Integer, Integer> nTriggers) {

        if(interseccion.size() == 1){
            // solo una transicion, no hay decision que tomar
            return interseccion.get(0);
        }

        // Segunda prioridad:
        if(interseccion.contains(2) && interseccion.contains(3)){
            try{
                return decisionT2T3(nTriggers);
            } catch (Exception e){
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Mayor prioridad:
        if(interseccion.contains(6) && interseccion.contains(7)){
            try{
                return decisionT6T7(nTriggers);
            } catch (Exception e){
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Este ultima se ejecuta solo si no hay 6, ni 7, ni 3, ni 2:
        return Collections.max(interseccion);
    }

    // Private Methods

    // T6: APROBACION : T7: RECHAZO
    private static int decisionT6T7(HashMap<Integer, Integer> nTriggers){
        if(tipoDePolitica == TipoDePolitica.BALANCEADA){
            if(((1.0 / (nTriggers.get(6) + nTriggers.get(7))) * nTriggers.get(6)) >= 0.5){
                return 7;
            } else{
                return 6;
            }
        } else if(tipoDePolitica == TipoDePolitica.PROCESAMIENTO_PRIORIZADO){
            if(((1.0 / (nTriggers.get(6) + nTriggers.get(7))) * nTriggers.get(6)) >= 0.8){
                return 7;
            } else{
                return 6;
            }
        }
        throw new IllegalArgumentException("No hay un tipo de politica.");
    }

    // T2: Agente 1 (superior) : T3: Agente 2 (inferior)
    private static int decisionT2T3(HashMap<Integer, Integer> nTriggers){
        if(tipoDePolitica == TipoDePolitica.BALANCEADA){
            if(((1.0 / (nTriggers.get(2) + nTriggers.get(3))) * nTriggers.get(2)) >= 0.5){
                return 3;
            } else{
                return 2;
            }
        } else if(tipoDePolitica == TipoDePolitica.PROCESAMIENTO_PRIORIZADO){
            if(((1.0 / (nTriggers.get(2) + nTriggers.get(3))) * nTriggers.get(2)) >= 0.75){
                return 3;
            } else{
                return 2;
            }
        }
        throw new IllegalArgumentException("No hay un tipo de politica.");
    }
}

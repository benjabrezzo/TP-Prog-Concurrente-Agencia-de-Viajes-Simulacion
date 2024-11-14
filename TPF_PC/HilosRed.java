import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HilosRed {
    public HashMap<String, ArrayList<Integer>> responsabilidad;
    public RedDePetri rdp;
    private ArrayList<Thread> hilosActivos = new ArrayList<>();
    private Colas colaDeCondicion;
    public AtomicBoolean detenerHilos;

    public HilosRed(RedDePetri rdp, Colas colaDeCondicion, HashMap<String, ArrayList<Integer>> responsabilidad){
        this.rdp = rdp;
        this.colaDeCondicion = colaDeCondicion;
        this.responsabilidad = responsabilidad;
        this.detenerHilos = new AtomicBoolean(false);
    }

    public void getInicio() {
        Monitor control = new Monitor(rdp, colaDeCondicion);
        // Crear los gestores y los hilos
        for (Map.Entry<String, ArrayList<Integer>> entry : responsabilidad.entrySet()) {
            Gestor gestor = new Gestor(rdp, entry.getValue(), control, detenerHilos);
            Thread d = new Thread(gestor, entry.getKey());// creamo el hilo con si name ej: hilo1
            hilosActivos.add(d);
        }

        for (Thread d : hilosActivos){
            d.start();
        }
    }

    public void detenerTodosLosHilos() {
        detenerHilos.set(true); // Detiene todos los hilos
    }

}

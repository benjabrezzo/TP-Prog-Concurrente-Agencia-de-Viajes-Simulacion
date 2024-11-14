import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gestor implements Runnable {

    ArrayList<Integer> transiciones;
    Monitor control;
    int cantidadTransiciones;
    private AtomicBoolean detenerHilos;
    RedDePetri rdp; // Para obtener los tiempos de la transiciones temporales


    public Gestor(RedDePetri rdp, ArrayList<Integer> nro, Monitor ctrl, AtomicBoolean stop) {
        this.transiciones = nro;
        this.control = ctrl;
        this.cantidadTransiciones = nro.size();
        this.detenerHilos = stop;
        this.rdp = rdp;
    }

    @Override
    public void run() {
        while (!detenerHilos.get()) {
            for (int i = 0; i < cantidadTransiciones; i++){
                control.fireTransition(transiciones.get(i));
            }
        }
    }
}

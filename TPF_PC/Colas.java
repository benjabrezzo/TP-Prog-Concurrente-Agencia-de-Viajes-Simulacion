import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Colas {
    private Map<Integer, List<Thread>> colas = new LinkedHashMap<>();

    //Mandamos el numero de transicion para dormir al hilo responsable de esa transicion
    public void delay(Integer transicion, Semaphore mutex) throws InterruptedException {
        Thread currentThread = Thread.currentThread(); //Es el hilo actual del monitor y se va a poner a dormir

        synchronized(currentThread) {
            colas.putIfAbsent(transicion, new ArrayList<>()); // Si no existe una lista de hilos asociados a la transicion la crea.
            colas.get(transicion).add(currentThread); // Agrega a la lista asociada a la transicion el hilo.

            // free the #MonitorDeConcurrencia mutex (make "monitor schedule")
            mutex.release();

            // sleep indefinitely
            currentThread.wait();

            // Cuando se despierte (con notify dentro de resume()) continuará acá, que como no hay más nada
            // regresa al Monitor.
        }
    }


    /**
     *  Este método será llamado luego de que la Política nos diga que hilo (identificado por la transición)
     *  despertar.
     *  El hilo es despertado y continua la ejecución justo después del wait en delay(), que como no hay nada regresa al
     *  monitor que es de donde fue llamada originalmente. Luego en el Monitor se vuelve a disparar fireTransition() haciendo
     *  resursividad.
     * */
    public void resume(Integer transicion){
        List<Thread> threads = colas.get(transicion);
        if(threads != null && !threads.isEmpty()){
            Thread threadToResume = threads.remove(0);
            synchronized(threadToResume){
                threadToResume.notify();
            }
            // Si no quedan más hilos asociados a la transicion elimina la llave del LinkedHashMap
            if(threads.isEmpty()){
                colas.remove(transicion);
            }
        }
    }

    /**
     *  Este método crea un ArrayList con las transiciones que tienen hilos esperando. Para hacerlo recorre las claves del HashMap.
     * */
    public ArrayList<Integer> quienesEstan(){
        ArrayList<Integer> enEspera = new ArrayList<>();
        if(colas.isEmpty()) { // Si no hay nadie en la cola devuelve a enEspera en estado VACIO (vacio != null, null es que no existe)
            return enEspera;
        }
        for(Map.Entry<Integer, List<Thread>> entry : colas.entrySet()){
            enEspera.add(entry.getKey());
        }
        return enEspera;
    }
}

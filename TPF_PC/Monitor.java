import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {

    private final RedDePetri rdp;
    private final Colas colaDeEspera;
    private final Semaphore mutex = new Semaphore(1);
    private final Random decision = new Random();
    public Monitor(RedDePetri rdp, Colas colaDeEspera) {
        this.rdp = rdp;
        this.colaDeEspera = colaDeEspera;
    }

    @Override
    public boolean fireTransition(int transicion){
        return fireTransition(transicion, false);
    }

    private boolean fireTransition(int transicion, boolean afterSleep){
        try{
            mutex.acquire();
            RedDePetri.DisparoStatus status;

            if(afterSleep){
                // Si se desperto del sleep, solo verifica si sigue sensibilizado
                status = rdp.verificarDisparoSensibilizado(transicion);
            } else {
                // Verifica si esta sensibilizado y la ventana de tiempo
                status = rdp.disparar(transicion);
            }

            switch(status){
                case DISPARE_TRUE:
                    // Logica de disparo y notificacion con Politica
                    //System.out.printf("En monitor: "+Thread.currentThread().getName()+" con transición: "+transicion+"\n");
                    rdp.setEstadoActual();
                    ArrayList<Integer> interseccion = rdp.sensibilizadas();
                    ArrayList<Integer> enEspera = colaDeEspera.quienesEstan();

                    if(!enEspera.isEmpty()){
                        interseccion.retainAll(enEspera);
                        if(!interseccion.isEmpty()){
                            int transitionToTrigger = Politica.cual(interseccion, rdp.getTransicionesDisparadas());
                            //int transitionToTrigger = interseccion.get(decision.nextInt(interseccion.size())); // descomentar para no usar politica, que sea random
                            colaDeEspera.resume(transitionToTrigger);
                        }
                    }
                    //System.out.printf("Apunto de liberar el mutex: "+Thread.currentThread().getName()+" con transición: "+transicion+"\n");
                    mutex.release();
                    break;
                case NO_SENSIBILIZADA:
                    // Se manda a dormir el hilo por un tiempo indetermiando:
                    colaDeEspera.delay(transicion, mutex);
                    /**
                     *  Cuando se despierte continuará acá. Por lo que se debe llamar otra vez a fireTransition(), introduciendo una recursividad.
                     *  Esta recursividad termina en algún punto. Esta recursividad es utilizada por los hilos que estaban dormidos, si se despertaron
                     *  es porque las condiciones para que estos trabejen están dadas, por lo que NO volverán a entrar a este else, harán su trabajo en el
                     *  if (no el trabajo real, lo que hace el código) y eventualmente retornaran true para el fireTransition() de la recursividad y luego
                     *  salen del else y devuelven "otra vez" true para ya salir del primer llamado de fireTransition().
                     * */


                    fireTransition(transicion, false); //Recursividad
                    break;
                case FUERA_VENTANA_TIEMPO:
                    // Esta sensibilizado pero no estamos en la ventana de tiempo, se duerme el hilo el tiempo necesario y vuelve a intentar
                    long tiempoDeSensibilizado = System.currentTimeMillis(); // Un poco posterior al time stamp, pero deberia servir igual
                    long alpha = rdp.alphaTransicion(transicion);
                    long sleepTime = rdp.cuantoFalta(tiempoDeSensibilizado, alpha); // Como el tiempo de sensibilizado ahora es un poco posterior este puede dar igual o menor que cero.
                    mutex.release(); // Liberamos el mutex para simular el tiempo

                    if(sleepTime <= 0){
                        // Pasa a ser instantaneo, ya paso el tiempo necesario, no hace falta dormir
                       fireTransition(transicion, true);

                    } else {
                        try{
                            Thread.sleep(sleepTime);
                            fireTransition(transicion, true);
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return true;
        } catch (InterruptedException e){
            e.printStackTrace();
            return false;
        }
    }
}

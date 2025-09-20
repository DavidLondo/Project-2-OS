import kareltherobot.*;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simulación con rutas fijas, control de Pare/Siga por grupos de 4 y exclusión
 * mutua por celda para evitar choques. Coordenadas (calle, avenida) => (street, avenue).
 */

// Gestor de ocupación por celda para evitar que dos robots estén en el mismo (S, A)
class OcupacionCeldas {
    private static final Object lock = new Object();
    private static final Map<String, Integer> ocupadoPor = new HashMap<>(); // key "S,A" -> robotId

    static void entrar(int s, int a, int robotId) throws InterruptedException {
        String key = s + "," + a;
        synchronized (lock) {
            while (ocupadoPor.containsKey(key)) {
                lock.wait();
            }
            ocupadoPor.put(key, robotId);
        }
    }

    static void salir(int s, int a) {
        String key = s + "," + a;
        synchronized (lock) {
            ocupadoPor.remove(key);
            // Notificamos cuando se libera una celda
            lock.notifyAll();
        }
    }

    // Pre-registrar ocupación inicial sin bloquear (para robots que aún no se mueven)
    static void preset(int s, int a, int robotId) {
        String key = s + "," + a;
        synchronized (lock) {
            // Si ya está ocupado, lo sobreescribimos sólo si es el mismo robot
            Integer cur = ocupadoPor.get(key);
            if (cur == null || cur == robotId) {
                ocupadoPor.put(key, robotId);
            }
        }
    }
}

// Control Pare/Siga por tandas de 4 robots exactos
class PareSigaBatch {
    private final int grupo = 4;
    private int esperando = 0;
    private int enTramo = 0;
    private boolean formando = false;

    public synchronized void formarYSalir() throws InterruptedException {
        // No permitir que se formen dos grupos a la vez
        while (formando) {
            wait();
        }
        // Empezar a formar un nuevo grupo
        formando = true;
        esperando++;
        if (esperando < grupo) {
            // Esperar a que lleguen 4
            while (esperando < grupo) {
                wait();
            }
        } else {
            // Ya están los 4 -> liberar a todos los que estaban esperando para esta tanda
            notifyAll();
        }
        // A partir de aquí, los 4 pasan
        enTramo++;
    }

    public synchronized void salirDeTramo() {
        enTramo--;
        if (enTramo == 0) {
            // Termina la tanda; reseteamos para que se forme la siguiente
            esperando = 0;
            formando = false;
            notifyAll();
        }
    }
}

// Pequeña utilidad para constantes de orientación internas
enum Ori {NORTE, ESTE, SUR, OESTE}

class SimRobot extends Robot implements Runnable, Directions {
    private final int id;
    private final String zona; // "azul" o "verde"
    private final String tipoRuta; // "rapida" o "larga"
    // Estado interno de posición/orientación para control de celdas
    private int s; // street (calle)
    private int a; // avenue (avenida)
    private Ori o;

    // Puertas de Pare/Siga para rutas rápidas
    private static final PareSigaBatch pareSigaAzul = new PareSigaBatch();
    private static final PareSigaBatch pareSigaVerde = new PareSigaBatch();

    public SimRobot(int id, int street, int avenue, Direction direction, int beeps,
                    Color color, String zona, String tipoRuta) {
        super(street, avenue, direction, beeps, color);
        this.id = id;
        this.zona = zona;
        this.tipoRuta = tipoRuta;
        this.s = street;
        this.a = avenue;
        // Traducir Direction inicial a nuestra Ori
        if (direction == North) this.o = Ori.NORTE;
        else if (direction == South) this.o = Ori.SUR;
        else if (direction == East) this.o = Ori.ESTE;
        else this.o = Ori.OESTE;
        World.setupThread(this);
    }

    // Utilidad: moverse un paso con exclusión de celda
    private void pasoSeguro() throws InterruptedException {
        int ns = s, na = a;
        if (o == Ori.NORTE) ns = s + 1;
        else if (o == Ori.SUR) ns = s - 1;
        else if (o == Ori.ESTE) na = a + 1;
        else if (o == Ori.OESTE) na = a - 1;

        // Adquirir la celda destino y avanzar
        OcupacionCeldas.entrar(ns, na, id);
        move();
        // Liberar la celda previa
        OcupacionCeldas.salir(s, a);
        // Actualizar estado
        s = ns; a = na;
        Thread.sleep(50); // Suavizar animación
    }

    private void recogerPasajerosMax4() { /* omitido en esta fase */ }

    private void pausaEstacion() throws InterruptedException { Thread.sleep(3000); }

    // Ayudas para caminar N pasos siempre que no haya pared (si hay pared, se detiene antes)
    private void avanzarPasos(int pasos) throws InterruptedException {
        for (int i = 0; i < pasos; i++) {
            if (!frontIsClear()) break;
            pasoSeguro();
        }
    }

    // Helpers de giro que actualizan nuestra orientación y la del robot
    private void girarIzquierda() { super.turnLeft();
        if (o == Ori.NORTE) o = Ori.OESTE; else if (o == Ori.OESTE) o = Ori.SUR; else if (o == Ori.SUR) o = Ori.ESTE; else o = Ori.NORTE; }
    private void girarDerecha() { super.turnLeft(); super.turnLeft(); super.turnLeft();
        if (o == Ori.NORTE) o = Ori.ESTE; else if (o == Ori.ESTE) o = Ori.SUR; else if (o == Ori.SUR) o = Ori.OESTE; else o = Ori.NORTE; }
    private void orientarSur() { while (o != Ori.SUR) girarIzquierda(); }
    private void orientarEste() { while (o != Ori.ESTE) girarIzquierda(); }
    private void orientarOeste() { while (o != Ori.OESTE) girarIzquierda(); }
    private void orientarNorte() { while (o != Ori.NORTE) girarIzquierda(); }
    
    // Alinear con la calle 1 (bajar sin atravesar muros)
    private void alinearACalle1() throws InterruptedException {
        while (s > 1) {
            orientarSur();
            if (!frontIsClear()) break; // si hay pared, no forzamos
            pasoSeguro();
        }
    }

    // Ir exactamente hasta (avenida,av) y (calle,st) en ejes, sin saltar paredes
    private void irHasta(int av, int st) throws InterruptedException {
        if (a != av) {
            if (av > a) orientarEste(); else orientarOeste();
            avanzarPasos(Math.abs(av - a));
        }
        if (s != st) {
            if (st > s) orientarNorte(); else orientarSur();
            avanzarPasos(Math.abs(st - s));
        }
    }

    // Salir del recinto azul hacia la salida común (7,1) utilizando la avenida 7 como columna de descenso
    private void salirRecintoAzulHaciaSalida() throws InterruptedException {
        // Si ya estamos en calle 1, solo ir horizontal a av.7
        if (s == 1) {
            if (a != 7) { if (a < 7) orientarEste(); else orientarOeste(); avanzarPasos(Math.abs(7 - a)); }
            return;
        }
        // 1) Movernos horizontalmente hasta la avenida 7 en la calle actual
        if (a != 7) { if (a < 7) orientarEste(); else orientarOeste(); avanzarPasos(Math.abs(7 - a)); }
        // 2) Bajar por la misma columna hasta la calle 1; si hay otro robot, esperar a que libere
        while (s > 1) {
            orientarSur();
            if (!frontIsClear()) { Thread.sleep(80); continue; }
            pasoSeguro();
        }
    }

    // --- Navegación local para salir del recinto sin saltar paredes ---
    private boolean derechaLibre() {
        girarDerecha();
        boolean libre = frontIsClear();
        girarIzquierda();
        return libre;
    }

    private void navegarHasta(int av, int st, int maxPasos) throws InterruptedException {
        int pasos = 0;
        while (!(a == av && s == st) && pasos < maxPasos) {
            if (derechaLibre()) {
                girarDerecha();
                if (frontIsClear()) pasoSeguro();
                else girarIzquierda();
            } else if (frontIsClear()) {
                pasoSeguro();
            } else {
                int intentos = 0;
                while (!frontIsClear() && intentos < 4) { girarIzquierda(); intentos++; }
                if (frontIsClear()) pasoSeguro(); else break;
            }
            pasos++;
        }
    }

    // Ruta rápida zona azul (imagen: azul claro hasta (30,1) y luego subir a (30,12))
    private void rutaRapidaZonaAzul() throws InterruptedException {
        recogerPasajerosMax4();
    // Salir del recinto hacia la salida común (7,1)
    salirRecintoAzulHaciaSalida();
        // Corredor con Pare/Siga: (7,1) -> (30,1)
        pareSigaAzul.formarYSalir();
        irHasta(30, 1);
        pareSigaAzul.salirDeTramo();
        pausaEstacion();
        // Subir hasta (30,12)
        irHasta(30, 12);
    // entrega omitida en esta fase
    }

    // Ruta larga zona azul (aprox. siguiendo morada). Segmentos fijos y estaciones.
    private void rutaLargaZonaAzul() throws InterruptedException {
        recogerPasajerosMax4();
    // Salir del recinto hacia la salida común (7,1)
    salirRecintoAzulHaciaSalida();
        // Waypoints exactos proporcionados:
        irHasta(11, 1);
        irHasta(11, 11);
        irHasta(8, 11);
        irHasta(8, 14);
        irHasta(16, 14);
        irHasta(16, 10);
        irHasta(13, 10);
        irHasta(13, 5);
        irHasta(20, 5);
        irHasta(20, 10);
        irHasta(30, 10);
        irHasta(30, 12);
    // entrega omitida en esta fase
    }

    // Ruta rápida zona verde: (23,12) -> (30,12) -> (30,10) -> corredor hacia bahías -> bajar a (..,2) -> (9,2)
    private void rutaRapidaZonaVerde() throws InterruptedException {
        pareSigaVerde.formarYSalir();
        try {
            recogerPasajerosMax4();
            // Orientarnos al Este y llegar a av. 30 por la calle 12
            orientarEste();
            avanzarPasos(30 - a);
            // Bajar a calle 10
            orientarSur();
            avanzarPasos(s - 10);
            // Tomar corredor hacia el Oeste (largo) y luego bajar a calle 2
            orientarOeste();
            avanzarPasos(8); // aproximación por bahías
            pausaEstacion();
            orientarSur();
            avanzarPasos(8); // bajar de 10 a 2
            // Ir al destino (9,2) hacia el Oeste
            orientarOeste();
            avanzarPasos(a - 9);
            // Entregar
            // entrega omitida en esta fase
        } finally {
            pareSigaVerde.salirDeTramo();
        }
    }

    // Ruta larga zona verde (negra): salir a la derecha en calle 11 y seguir hacia el oeste por calles internas
    private void rutaLargaZonaVerde() throws InterruptedException {
        recogerPasajerosMax4();
        // Bajar a calle 11 (una al sur)
        orientarSur();
        pasoSeguro();
        // Girar a la derecha (oeste) y avanzar largo hasta cerca de av. 15
        orientarOeste();
        avanzarPasos(a - 15);
        pausaEstacion();
        // Bajar por corredor central hasta calle 5
        orientarSur();
        avanzarPasos(s - 5);
        // Hacia el oeste hasta llegar a av. 9 y luego bajar a calle 2
        orientarOeste();
        avanzarPasos(a - 9);
        orientarSur();
        avanzarPasos(s - 2);
        // Entregar
    // entrega omitida en esta fase
    }

    public void run() {
        try {
            System.out.println("Robot " + id + " -> zona:" + zona + " ruta:" + tipoRuta);

            if ("azul".equals(zona)) {
                // Todos salen del recinto por la calle 1
                alinearACalle1();
                if ("rapida".equals(tipoRuta)) rutaRapidaZonaAzul();
                else rutaLargaZonaAzul();
            } else {
                if ("rapida".equals(tipoRuta)) rutaRapidaZonaVerde();
                else rutaLargaZonaVerde();
            }
        } catch (InterruptedException e) {
            System.err.println("Robot " + id + " interrumpido: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Liberar ocupación de la celda final
            OcupacionCeldas.salir(s, a);
            System.out.println("Robot " + id + " terminó");
        }
    }
}

public class SimulacionPareSiga implements Directions {
    // ========== Definición de rutas (zona azul) ==========
    // Un segmento es una orientación cardinal y una cantidad de pasos.
    // dir: 'N' norte, 'E' este, 'S' sur, 'W' oeste. estacion=true indica pausa de 3s al finalizar ese segmento.
    public static class Segmento {
        public final char dir; public final int pasos; public final boolean estacion;
        public Segmento(char dir, int pasos) { this(dir, pasos, false); }
        public Segmento(char dir, int pasos, boolean estacion) { this.dir = dir; this.pasos = pasos; this.estacion = estacion; }
        @Override public String toString() { return "["+dir+":"+pasos+(estacion?"*":"")+"]"; }
    }

    private static Segmento N(int p) { return new Segmento('N', p); }
    private static Segmento E(int p) { return new Segmento('E', p); }
    private static Segmento S(int p) { return new Segmento('S', p); }
    private static Segmento W(int p) { return new Segmento('W', p); }
    private static Segmento Np(int p) { return new Segmento('N', p, true); }
    private static Segmento Ep(int p) { return new Segmento('E', p, true); }

    // Ruta 1 (rápida, con Pare y Siga): (1,7) -> Este hasta av. 30; luego Norte hasta calle 12.
    // Pasos exactos en este mundo: 23 al Este (de av 7 a 30), 11 al Norte (de calle 1 a 12)
    public static final List<Segmento> RUTA_AZUL_RAPIDA = Collections.unmodifiableList(
        Arrays.asList(
            Ep(23), // incluye una parada al final del corredor horizontal
            N(11)
        )
    );

    // Ruta 2 (larga): (1,7)->E hasta av.11; N hasta calle 11; luego trazado por flechas azules.
    // El trazado se aproxima en segmentos rectos siguiendo la imagen.
    public static final List<Segmento> RUTA_AZUL_LARGA = Collections.unmodifiableList(
        Arrays.asList(
            E(4),      // a av. 11
            Np(10),    // subir a calle 11 (parada de estación)
            E(5),      // hacia el este
            S(6),      // bajada
            Ep(5),     // este (parada)
            N(5),      // subida
            E(9),      // hasta av. 30 aprox.
            N(2)       // ajuste a calle 12
        )
    );

    public static void main(String[] args) throws InterruptedException {
        World.readWorld("Aranjuez.kwld");
        World.setVisible(true);
        World.setDelay(20);

        int[][] posiciones = generarPosicionesZonaAzul();
        SimRobot[] robots = new SimRobot[28];
        for (int i = 0; i < 28; i++) {
            String tipo = (i < 7) ? "rapida" : (i < 14 ? "larga" : "quieto");
            robots[i] = new SimRobot(100 + i, posiciones[i][0], posiciones[i][1], East, 0, Color.BLUE, "azul", tipo);
            OcupacionCeldas.preset(posiciones[i][0], posiciones[i][1], 100 + i);
        }
        System.out.println("Generados 28 robots azules. Esperando para iniciar movimiento...");
        Thread.sleep(1500);

        int[][] posicionesVerde = generarPosicionesZonaVerde();
        SimRobot[] robotsVerde = new SimRobot[28];
        for (int i = 0; i < 28; i++) {
            String tipo = (i < 7) ? "rapida" : (i < 14 ? "larga" : "quieto");
            robotsVerde[i] = new SimRobot(200 + i, posicionesVerde[i][0], posicionesVerde[i][1], East, 0, Color.GREEN, "verde", tipo);
            OcupacionCeldas.preset(posicionesVerde[i][0], posicionesVerde[i][1], 200 + i);
        }
        Thread.sleep(1500);

        ArrayList<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < robots.length; i++) {
            if (posiciones[i][0] == 1) {
                Thread t = new Thread(robots[i]);
                hilos.add(t);
                t.start();
                Thread.sleep(150);
            }
        }
        Thread.sleep(2000);
        for (int i = 0; i < robots.length; i++) {
            if (posiciones[i][0] == 2) {
                Thread t = new Thread(robots[i]);
                hilos.add(t);
                t.start();
                Thread.sleep(150);
            }
        }

        long deadline = System.currentTimeMillis() + 45000;
        for (Thread t : hilos) {
            long left = deadline - System.currentTimeMillis();
            if (left > 0) try { t.join(left); } catch (InterruptedException ignore) {}
        }
    }

    private static int[][] generarPosicionesZonaAzul() {
        ArrayList<int[]> lista = new ArrayList<>();

        for (int av = 7; av >= 1 && lista.size() < 28; av--) {
            lista.add(new int[]{1, av});
        }

        for (int street = 2; street <= 5 && lista.size() < 28; street++) {
            if (street % 2 == 0) {
                for (int av = 1; av <= 7 && lista.size() < 28; av++) {
                    lista.add(new int[]{street, av});
                }
            } else {
                for (int av = 7; av >= 1 && lista.size() < 28; av--) {
                    lista.add(new int[]{street, av});
                }
            }
        }

        int[][] out = new int[28][2];
        for (int i = 0; i < 28; i++) out[i] = lista.get(i);
        return out;
    }

    private static int[][] generarPosicionesZonaVerde() {
        ArrayList<int[]> lista = new ArrayList<>();
        lista.add(new int[]{12, 23});
        if (lista.size() < 28) lista.add(new int[]{13, 23});
        for (int street = 13; street <= 16 && lista.size() < 28; street++) {
            if (street % 2 == 1) {
                for (int av = 23; av <= 30 && lista.size() < 28; av++) lista.add(new int[]{street, av});
            } else {
                for (int av = 30; av >= 23 && lista.size() < 28; av--) lista.add(new int[]{street, av});
            }
        }
        int[][] out = new int[28][2];
        for (int i = 0; i < 28; i++) out[i] = lista.get(i);
        return out;
    }
}

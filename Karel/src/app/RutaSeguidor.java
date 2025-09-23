package app;

import kareltherobot.*;

public final class RutaSeguidor implements Directions {
    private RutaSeguidor() {}

    public static void seguirRuta(Robot karel, Ruta ruta) {
        if (ruta == null || ruta.longitud() == 0) return;
        Celda actual = ruta.inicio();
        for (int i = 1; i < ruta.longitud(); i++) {
            Celda siguiente = ruta.puntos().get(i);
            avanzarUnPaso(karel, actual, siguiente);
            actual = siguiente;
        }
    }

    public static void avanzarUnPaso(Robot karel, Celda actual, Celda siguiente) {
        int dx = siguiente.avenida - actual.avenida;
        int dy = siguiente.calle   - actual.calle;
        if (Math.abs(dx) + Math.abs(dy) != 1) {
            throw new IllegalArgumentException("Paso no adyacente: " + actual + " -> " + siguiente);
        }
        if (dx == 1) orientarEste(karel);
        else if (dx == -1) orientarOeste(karel);
        else if (dy == 1) orientarNorte(karel);
        else orientarSur(karel);

        if (!karel.frontIsClear()) {
            throw new IllegalStateException("Frente bloqueado antes de mover desde " + actual + " hacia " + siguiente);
        }
        karel.move();
    }

    private static void orientarNorte(Robot karel) {
        while (!karel.facingNorth()) karel.turnLeft();
    }
    private static void orientarEste(Robot karel) {
        while (!karel.facingEast()) karel.turnLeft();
    }
    private static void orientarSur(Robot karel) {
        while (!karel.facingSouth()) karel.turnLeft();
    }
    private static void orientarOeste(Robot karel) {
        while (!karel.facingWest()) karel.turnLeft();
    }
}

package app;

import java.util.HashSet;
import java.util.Set;

public class OcupacionMapa {
    public static final int ANCHO = 30;
    public static final int ALTO  = 19;

    private final Set<Celda> ocupadas = new HashSet<>();

    public static boolean dentroDeLimites(int calle, int avenida) {
        return calle >= 1 && calle <= ALTO && avenida >= 1 && avenida <= ANCHO;
    }

    public synchronized boolean ocupar(int calle, int avenida) {
        if (!dentroDeLimites(calle, avenida)) return false;
        return ocupadas.add(new Celda(calle, avenida));
    }

    public synchronized boolean liberar(int calle, int avenida) {
        if (!dentroDeLimites(calle, avenida)) return false;
        return ocupadas.remove(new Celda(calle, avenida));
    }

    public synchronized boolean estaOcupada(int calle, int avenida) {
        if (!dentroDeLimites(calle, avenida)) return false;
        return ocupadas.contains(new Celda(calle, avenida));
    }

    public synchronized int conteo() {
        return ocupadas.size();
    }
}

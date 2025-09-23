package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Ruta {
    private final String nombre;
    private final List<Celda> puntos;

    public Ruta(String nombre, List<Celda> puntos) {
        this.nombre = nombre;
        this.puntos = Collections.unmodifiableList(new ArrayList<>(puntos));
    }

    public String nombre() { return nombre; }
    public List<Celda> puntos() { return puntos; }
    public int longitud() { return puntos.size(); }
    public Celda inicio() { return puntos.isEmpty() ? null : puntos.get(0); }
    public Celda fin() { return puntos.isEmpty() ? null : puntos.get(puntos.size() - 1); }

    public boolean esValida() {
        if (puntos.isEmpty()) return false;
        for (int i = 0; i < puntos.size(); i++) {
            Celda c = puntos.get(i);
            if (!OcupacionMapa.dentroDeLimites(c.calle, c.avenida)) return false;
            if (i > 0) {
                Celda p = puntos.get(i - 1);
                int dx = Math.abs(c.avenida - p.avenida);
                int dy = Math.abs(c.calle - p.calle);
                if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) return false;
            }
        }
        return true;
    }
}

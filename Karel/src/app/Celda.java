package app;

import java.util.Objects;

public final class Celda {
    public final int avenida;
    public final int calle;

    public Celda(int calle, int avenida) {
        this.calle = calle;
        this.avenida = avenida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Celda)) return false;
        Celda celda = (Celda) o;
        return avenida == celda.avenida && calle == celda.calle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(avenida, calle);
    }

    @Override
    public String toString() {
        return "Celda(calle=" + calle + ", avenida=" + avenida + ")";
    }
}

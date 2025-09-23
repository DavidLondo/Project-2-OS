package app;

import java.util.ArrayList;
import java.util.List;
import kareltherobot.*;

public final class RobotsIniciales implements Directions {
    private RobotsIniciales() {}

    public static List<Robot> crearRobotsZonaAzul28() {
        final int xmin = 1, xmax = 7;
        final int ymin = 1, ymax = 4;
        List<Robot> robots = new ArrayList<>(28);

        for (int calle = ymin; calle <= ymax; calle++) {
            boolean filaImpar = (calle % 2 == 1);
            if (filaImpar) {
                for (int avenida = xmax; avenida >= xmin; avenida--) {
                    robots.add(new Robot(calle, avenida, West, 0));
                }
            } else {
                for (int avenida = xmin; avenida <= xmax; avenida++) {
                    robots.add(new Robot(calle, avenida, East, 0));
                }
            }
        }
        return robots;
    }
}

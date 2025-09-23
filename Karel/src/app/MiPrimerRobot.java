package app;

import kareltherobot.*;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        World.readWorld("src/Aranjuez.kwld");
        World.setVisible(true);

        RobotsIniciales.crearRobotsZonaAzul28();
        // World.setDelay(10);
    }
}

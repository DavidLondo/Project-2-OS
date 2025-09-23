package app;

import kareltherobot.*;

public class DemoAzulLarga implements Directions {
    public static void main(String[] args) {
        World.readWorld("src/Aranjuez.kwld");
        World.setVisible(true);
        // World.setDelay(20);

        Robot karel = new Robot(1, 7, East, 0);
        RutaSeguidor.seguirRuta(karel, RutasPredefinidas.RutaDesdeAzulLarga);
    }
}

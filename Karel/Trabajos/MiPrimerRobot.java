import kareltherobot.*;
import java.awt.Color;

// Clase Racer que corre en su propio hilo
class Racer extends Robot implements Runnable {

    public Racer(int Street, int Avenue, Direction direction, int beeps) {
        super(Street, Avenue, direction, beeps);
        World.setupThread(this); // Configura el hilo para este robot
    }

    // Constructor con color
    public Racer(int Street, int Avenue, Direction direction, int beeps, Color c) {
        super(Street, Avenue, direction, beeps, c);
        World.setupThread(this);
    }

    // El recorrido del robot
    public void race() {
        // Mover 4 pasos
        move();
        move();
        move();
        move();

        // Recoger 5 beepers
        pickBeeper();
        pickBeeper();
        pickBeeper();
        pickBeeper();
        pickBeeper();

        // Girar a la izquierda y salir de los muros
        turnLeft();
        move();
        move();

        // Poner los 5 beepers
        putBeeper();
        putBeeper();
        putBeeper();
        putBeeper();
        putBeeper();

        // Mover y apagarse
        move();
        turnOff();
    }

    // Método run que se ejecuta en el hilo
    public void run() {
        race();
    }
}

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        // Cargar el mundo
        World.readWorld("Mundo1.kwld");
        World.setVisible(true);

        // Crear dos robots en hilos separados
        Racer Karel = new Racer(1, 1, East, 0);
        Racer Azul = new Racer(1, 1, East, 0, Color.BLUE);

        // Iniciar el hilo del primer robot inmediatamente
        new Thread(Karel).start();

        // Retrasar el inicio del azul
        Thread.sleep(100);

        // Iniciar el hilo del robot azul
        new Thread(Azul).start();
    }
}

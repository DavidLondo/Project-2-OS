import kareltherobot.*;
import java.awt.Color;

public class Main implements Directions {
    public static void main(String[] args) {
        try {
            // Cargar mundo
            World.readWorld("../worlds/Aranjuez.kwld");
            World.setVisible(true);
            World.setDelay(30); // Velocidad de animación más rápida
            
            // Inicializar sistema de sincronización
            WorldUtils.initializeWorld(33, 20);
            
            // Crear robots en posiciones iniciales diferentes
            RobotThread robot1 = new RobotThread("ROJO", 1, 1, East, 0, Color.RED);
            RobotThread robot2 = new RobotThread("AZUL", 1, 2, East, 0, Color.BLUE);
            RobotThread robot3 = new RobotThread("VERDE", 1, 20, West, 0, Color.GREEN);
            
            System.out.println("=== INFORMACIÓN DE ROBOTS ===");
            System.out.println(robot1.getRobotInfo());
            System.out.println(robot2.getRobotInfo());
            System.out.println(robot3.getRobotInfo());
            
            // Crear y iniciar hilos
            Thread t1 = new Thread(robot1);
            Thread t2 = new Thread(robot2);
            Thread t3 = new Thread(robot3);
            
            System.out.println("Iniciando simulación con 3 robots...");
            
            t1.start();
            Thread.sleep(500);
            t2.start();
            Thread.sleep(500);
            t3.start();
            
            // Esperar un tiempo y mostrar estado
            Thread.sleep(5000);
            WorldUtils.printWorldStatus();
            
            // Esperar a que todos terminen
            t1.join();
            t2.join();
            t3.join();
            
            System.out.println("Simulación completada");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
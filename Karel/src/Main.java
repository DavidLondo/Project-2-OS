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
            RobotThread robot1 = new RobotThread("BLUE", 1, 1, East, 0, Color.BLUE);
            RobotThread robot2 = new RobotThread("BLUE", 1, 2, East, 0, Color.BLUE);
            RobotThread robot3 = new RobotThread("BLUE",1, 3, East, 0, Color.BLUE);
            RobotThread robot4 = new RobotThread("BLUE",1, 4, East, 0, Color.BLUE);
            RobotThread robot5 = new RobotThread("BLUE",1, 5, East, 0, Color.BLUE);
            RobotThread robot6 = new RobotThread("BLUE",1, 6, East, 0, Color.BLUE);
            
            System.out.println("=== INFORMACIÓN DE ROBOTS ===");
            System.out.println(robot1.getRobotInfo());
            System.out.println(robot2.getRobotInfo());
            System.out.println(robot3.getRobotInfo());
            
            // Crear y iniciar hilos
            Thread t1 = new Thread(robot1);
            Thread t2 = new Thread(robot2);
            Thread t3 = new Thread(robot3);
            Thread t4 = new Thread(robot4);
            Thread t5 = new Thread(robot5);
            Thread t6 = new Thread(robot6);
            
            System.out.println("Iniciando simulación con 6 robots...");
            
            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t5.start();
            t6.start();
            
            // Esperar un tiempo y mostrar estado
            Thread.sleep(20000);
            WorldUtils.printWorldStatus();
            
            // Esperar a que todos terminen
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
            t6.join();
            
            System.out.println("Simulación completada");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
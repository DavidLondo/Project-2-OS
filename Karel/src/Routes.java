import kareltherobot.*;
import java.util.concurrent.Semaphore;

public class Routes {
    private static Semaphore blue1 = new Semaphore(4, true);
    private static Semaphore blue2 = new Semaphore(4, true);
    private static Semaphore blue3 = new Semaphore(4, true);

    private static Semaphore green1 = new Semaphore(7, true);
    private static Semaphore green2 = new Semaphore(4, true);
    private static Semaphore green3 = new Semaphore(6, true);

    private static Semaphore dispute1 = new Semaphore(1, true);
    private static Semaphore dispute2 = new Semaphore(1, true);
    private static Semaphore dispute3 = new Semaphore(1, true);

    public static void moveTo(int position, Directions.Direction direction, RobotThread robot) {
        while (robot.getDirection() != direction) {
            robot.safeTurnLeft();
        }
        switch (direction.toString()) {
            case "North":
                while (robot.getStreet() < position) {
                    robot.safeMove();
                }
                break;
            case "South":
                while (robot.getStreet() > position) {
                    robot.safeMove();
                }
                break;
            case "East":
                while (robot.getAvenue() < position) {
                    robot.safeMove();
                }
                break;
            case "West":
                while (robot.getAvenue() > position) {
                    robot.safeMove();
                }
                break;
            default:
                break;
        }
    }

    public static void blueLong(RobotThread robot) {
        moveTo(11, Directions.North, robot);
        moveTo(8, Directions.West, robot);
        moveTo(14, Directions.North, robot);
        moveTo(16, Directions.East, robot);
        moveTo(10, Directions.South, robot);
        moveTo(13, Directions.West, robot);
        moveTo(5, Directions.South, robot);
        moveTo(20, Directions.East, robot);
        moveTo(10, Directions.North, robot);
        moveTo(30, Directions.East, robot);
        moveTo(12, Directions.North, robot);
    }
    
    public static void greenLong(RobotThread robot) {
        moveTo(20, Directions.West, robot);
        moveTo(19, Directions.North, robot);
        moveTo(18, Directions.West, robot);
        moveTo(15, Directions.South, robot);
        moveTo(1, Directions.West, robot);
        moveTo(10, Directions.South, robot);
        moveTo(10, Directions.East, robot);
        moveTo(2, Directions.South, robot);
        moveTo(8, Directions.West, robot);
    }

    public static void blueShort(RobotThread robot) {
        moveTo(15, Directions.East, robot);

        acquireZonePermission(blue2);
        acquireZonePermission(dispute3);

        releaseZonePermission(blue1);

        moveTo(22, Directions.East, robot);
        releaseZonePermission(dispute3);
        moveTo(25, Directions.East, robot);

        acquireZonePermission(blue3);
        acquireZonePermission(dispute2);
        
        releaseZonePermission(blue2);

        moveTo(30, Directions.East, robot);
        releaseZonePermission(dispute2);
        moveTo(4, Directions.North, robot);

        acquireZonePermission(dispute1);
        
        releaseZonePermission(blue3);

        moveTo(11, Directions.North, robot);
        releaseZonePermission(dispute1);
        moveTo(12, Directions.North, robot);

        // Importante: no detener aquí para permitir encadenar con greenZone + ruta verde
    }

    public static void greenShort(RobotThread robot) {
        moveTo(10, Directions.South, robot);
        moveTo(29, Directions.East, robot);
        
        acquireZonePermission(green2);
        acquireZonePermission(dispute1);
        
        releaseZonePermission(green1);

        moveTo(30, Directions.East, robot);
        moveTo(5, Directions.South, robot);
        moveTo(29, Directions.West, robot);
        
        releaseZonePermission(dispute1);
        
        moveTo(2, Directions.South, robot);

        acquireZonePermission(green3);
        acquireZonePermission(dispute2);

    // Liberar green2 ahora que entramos en la siguiente fase (evita retenerlo hasta el final)
    releaseZonePermission(green2);

        moveTo(1, Directions.South, robot);
        moveTo(26, Directions.West, robot);
        moveTo(2, Directions.North, robot);
        
        releaseZonePermission(dispute2);
        
        moveTo(21, Directions.West, robot);

        acquireZonePermission(dispute3);
        
        moveTo(1, Directions.South, robot);
        moveTo(16, Directions.West, robot);
        moveTo(2, Directions.North, robot);

        releaseZonePermission(dispute3);

    // Liberar green3 antes de finalizar para no agotar sus permisos
    releaseZonePermission(green3);

        moveTo(8, Directions.West, robot);

        // Importante: no detener aquí para permitir encadenar con blueZone + ruta azul
    }

    private static boolean acquireZonePermission(Semaphore semaphore) {
        while (true) {
            try {
                semaphore.acquire();
                break;
            } catch (Exception e) {
                System.out.println("Volviendo a intentar acceder al semaforo: " + semaphore);
            }
        }
        return true;
    }

    public static void blueZone(RobotThread robot) {
        moveTo(4, Directions.North, robot);
        moveTo(1, Directions.West, robot);
        moveTo(3, Directions.South, robot);
        moveTo(7, Directions.East, robot);
        moveTo(2, Directions.South, robot);
        moveTo(1, Directions.West, robot);
        moveTo(1, Directions.South, robot);
        moveTo(11, Directions.East, robot);
    }
    
    public static void greenZone(RobotThread robot) {
        moveTo(16, Directions.North, robot);
        moveTo(29, Directions.West, robot);
        moveTo(15, Directions.South, robot);
        moveTo(23, Directions.West, robot);
        moveTo(14, Directions.South, robot);
        moveTo(29, Directions.East, robot);
        moveTo(12, Directions.South, robot);
        moveTo(28, Directions.West, robot);
        moveTo(13, Directions.North, robot);
        moveTo(23, Directions.West, robot);
        moveTo(11, Directions.South, robot);
    }

    private static boolean tryAcquireZonePermission(Semaphore semaphore) {
        return semaphore.tryAcquire();
    }
    
    private static void releaseZonePermission(Semaphore semaphore) {
        semaphore.release();
    }

    private static boolean shouldTakeBlueShort() {
        if (blue1.availablePermits() == 0) return false;

        int conflicts = 0;

        if (dispute1.availablePermits() == 0) conflicts++;
        if (dispute2.availablePermits() == 0) conflicts++;
        if (dispute3.availablePermits() == 0) conflicts++;

        return conflicts <= 2;
    }

    private static boolean shouldTakeGreenShort() {
        if (green1.availablePermits() == 0) return false;

        int conflicts = 0;

        if (green2.availablePermits() == 0) conflicts++;
        if (green3.availablePermits() == 0) conflicts++;

        if (dispute1.availablePermits() == 0) conflicts++;
        if (dispute2.availablePermits() == 0) conflicts++;
        if (dispute3.availablePermits() == 0) conflicts++;

        return conflicts <= 2;
    }

    public static void runBlueSmart(RobotThread robot) {
        if (shouldTakeBlueShort() && selectFastBlueRoute()) {
    
            blueShort(robot);
        } else {
            blueLong(robot);
        }
    }

    public static void runGreenSmart(RobotThread robot) {
        if (shouldTakeGreenShort() && selectFastGreenRoute()) {
            greenShort(robot);
        } else {
            greenLong(robot);
        }
    }

    public static boolean selectFastBlueRoute() {
        return tryAcquireZonePermission(blue1);
    }

    public static boolean selectFastGreenRoute() {
        return tryAcquireZonePermission(green1);
    }
}

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorldUtils {
    private static final Semaphore worldSemaphore = new Semaphore(1, true);
    private static final Lock movementLock = new ReentrantLock();
    private static boolean[][] occupiedPositions;
    private static int worldWidth = 10;
    private static int worldHeight = 10;

    public static void initializeWorld(int width, int height) {
        worldWidth = width;
        worldHeight = height;
        occupiedPositions = new boolean[width + 1][height + 1];
        System.out.println("Mundo inicializado: " + width + "x" + height);
    }

    public static boolean tryAcquireMovementPermission() {
        return worldSemaphore.tryAcquire();
    }

    public static void releaseMovementPermission() {
        worldSemaphore.release();
    }

    public static boolean isPositionSafe(int street, int avenue) {
        movementLock.lock();
        try {
            // Verificar límites del mundo
            if (street < 1 || avenue < 1 || street > worldHeight || avenue > worldWidth) {
                System.out.println("Posición fuera de límites: (" + street + "," + avenue + ")");
                return false;
            }
            // Verificar si la posición está ocupada
            boolean occupied = occupiedPositions[avenue][street];
            if (occupied) {
                System.out.println("Posición ocupada: (" + street + "," + avenue + ")");
            }
            return !occupied;
        } finally {
            movementLock.unlock();
        }
    }

    public static boolean reservePosition(int street, int avenue) {
        movementLock.lock();
        try {
            if (isPositionSafe(street, avenue)) {
                occupiedPositions[avenue][street] = true;
                return true;
            }
            return false;
        } finally {
            movementLock.unlock();
        }
    }

    public static void freePosition(int street, int avenue) {
        movementLock.lock();
        try {
            if (street >= 1 && avenue >= 1 && street <= worldHeight && avenue <= worldWidth) {
                occupiedPositions[avenue][street] = false;
            }
        } finally {
            movementLock.unlock();
        }
    }

    public static void printWorldStatus() {
        movementLock.lock();
        try {
            System.out.println("=== ESTADO DEL MUNDO ===");
            for (int street = worldHeight; street >= 1; street--) {
                StringBuilder line = new StringBuilder();
                for (int avenue = 1; avenue <= worldWidth; avenue++) {
                    line.append(occupiedPositions[avenue][street] ? "[R]" : "[ ]");
                }
                System.out.println("Calle " + street + ": " + line.toString());
            }
            System.out.println("=======================");
        } finally {
            movementLock.unlock();
        }
    }
}

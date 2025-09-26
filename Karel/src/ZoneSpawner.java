import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

public class ZoneSpawner {

    public enum Team { BLUE, GREEN }

    private static final int BLUE_SPAWN_AVENUE = 8;
    private static final int BLUE_SPAWN_STREET = 2;
    private static final int GREEN_SPAWN_AVENUE = 30;
    private static final int GREEN_SPAWN_STREET = 12;

    private static final AtomicInteger BLUE_COUNTER = new AtomicInteger(0);
    private static final AtomicInteger GREEN_COUNTER = new AtomicInteger(0);

    public static void spawnInterleaved(int blueCount, int greenCount, int delayBetweenSpawnsMs) {
        int max = Math.max(blueCount, greenCount);
        for (int i = 0; i < max; i++) {
            if (i < blueCount) new Thread(() -> spawnOneBlocking(Team.BLUE, max)).start();
            if (i < greenCount) new Thread(() -> spawnOneBlocking(Team.GREEN, max)).start();
            try { Thread.sleep(delayBetweenSpawnsMs); } catch (InterruptedException ignored) {}
        }
    }

    public static void spawnOne(Team team) { new Thread(() -> spawnOneBlocking(team, 0)).start(); }

    private static void spawnOneBlocking(Team team, int num) {

        int street = (team == Team.BLUE) ? BLUE_SPAWN_STREET : GREEN_SPAWN_STREET;
        int avenue = (team == Team.BLUE) ? BLUE_SPAWN_AVENUE : GREEN_SPAWN_AVENUE;
        waitUntilFree(street, avenue);

    Color color = (team == Team.BLUE) ? Color.BLUE : Color.GREEN;
        String name = (team == Team.BLUE)
                ? "BLUE_" + BLUE_COUNTER.incrementAndGet()
                : "GREEN_" + GREEN_COUNTER.incrementAndGet();
    RobotThread r = new RobotThread(
        name,
        street,
        avenue,
        (team == Team.BLUE ? Directions.East : Directions.North),
        0,
        color
    );

        Thread t = new Thread(() -> {
            try {
                if (team == Team.BLUE) {
                    Routes.blueZone(r);
                } else {
                    Routes.greenZone(r);
                }
                if (num != 13) {
                    dispatchRoutes(team, r);
                } else {
                    dispatchRoutes13(team, r);
                }
            } finally {
                r.stopRobot();
            }
        });
        t.start();
    }

    private static void dispatchRoutes(Team team, RobotThread r) {
        if (team == Team.BLUE) {
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
        } else {
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
        }
    }

    private static void dispatchRoutes13(Team team, RobotThread r) {
        if (team == Team.BLUE) {
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
        } else {
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
            Routes.blueZone(r);
            Routes.runBlueSmart(r);
            Routes.greenZone(r);
            Routes.runGreenSmart(r);
        }
    }

    private static void waitUntilFree(int street, int avenue) {
        while (!WorldUtils.isPositionSafe(street, avenue)) {
            try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        }
    }
}

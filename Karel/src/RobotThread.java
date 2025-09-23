import kareltherobot.*;
import java.awt.Color;

public class RobotThread extends Robot{
    private final String robotName;
    private int currentStreet;
    private int currentAvenue;
    private Direction currentDirection;
    private boolean running = true;

    public RobotThread(String name, int street, int avenue, Direction direction, int beepers) {
        super(street, avenue, direction, beepers);
        this.robotName = name;
        this.currentStreet = street;
        this.currentAvenue = avenue;
        this.currentDirection = direction;
        World.setupThread(this);
        WorldUtils.reservePosition(street, avenue);
    }

    public RobotThread(String name, int street, int avenue, Direction direction, int beepers, Color color) {
        super(street, avenue, direction, beepers, color);
        this.robotName = name;
        this.currentStreet = street;
        this.currentAvenue = avenue;
        this.currentDirection = direction;
        World.setupThread(this);
        WorldUtils.reservePosition(street, avenue);
    }

    public int getStreet() {
        return currentStreet;
    }
    
    public int getAvenue() {
        return currentAvenue;
    }
    
    public Direction getDirection() {
        return currentDirection;
    }

    private void updatePositionAfterMove() {
        switch (currentDirection.toString()) {
            case "North": currentStreet++; break;
            case "South": currentStreet--; break;
            case "East": currentAvenue++; break;
            case "West": currentAvenue--; break;
        }
        System.out.println(robotName + ": Nueva posición (" + currentStreet + "," + currentAvenue + ")");
    }

    private void updateDirectionAfterTurn() {
        switch (currentDirection.toString()) {
            case "North": currentDirection = Directions.West; break;
            case "West": currentDirection = Directions.South; break;
            case "South": currentDirection = Directions.East; break;
            case "East": currentDirection = Directions.North; break;
        }
        System.out.println(robotName + ": Nueva dirección " + currentDirection);
    }

    @Override 
    public void move() {
        super.move();
        updatePositionAfterMove();
    }

    @Override
    public void turnLeft() {
        super.turnLeft();
        updateDirectionAfterTurn();
    }

    public boolean safeMove() {
        if (!WorldUtils.tryAcquireMovementPermission()) {
            System.out.println(robotName + ": Esperando permiso para mover...");
            WorldUtils.releaseMovementPermission();
            return false;
        }
        
        try {
            // Calcular próxima posición
            int nextStreet = getStreet();
            int nextAvenue = getAvenue();
            
            switch (currentDirection.toString()) {
                case "North": nextStreet++; break;
                case "South": nextStreet--; break;
                case "East": nextAvenue++; break;
                case "West": nextAvenue--; break;
            }
            
            // Verificar si la posición está segura
            if (WorldUtils.isPositionSafe(nextStreet, nextAvenue)) {
                System.out.println(robotName + ": Moviendo a (" + nextStreet + "," + nextAvenue + ")");
                
                // Reservar nueva posición
                if (WorldUtils.reservePosition(nextStreet, nextAvenue)) {
                    // Liberar posición actual
                    WorldUtils.freePosition(getStreet(), getAvenue());
                    // Realizar movimiento
                    super.move();
                    return true;
                }
            } else {
                System.out.println(robotName + ": Posición (" + nextStreet + "," + nextAvenue + ") no segura");
            }
            return false;
        } finally {
            WorldUtils.releaseMovementPermission();
        }
    }

    public void safeTurnLeft() {
        if (WorldUtils.tryAcquireMovementPermission()) {
            try {
                System.out.println(robotName + ": Girando a la izquierda");
                super.turnLeft();
                updatePositionAfterMove();
            } finally {
                WorldUtils.releaseMovementPermission();
            }
        }
    }

    public void stopRobot() {
        running = false;
        WorldUtils.freePosition(getStreet(), getAvenue());
        turnOff();
    }

    private void testMovement() {
        while (running) {
            try {
                safeMove();
            } catch (Exception e) {
                System.out.println(robotName + ": Error - " + e.getMessage());
                break;
            }
        }
    }

    @Override
    public void run() {
        System.out.println(robotName + ": Iniciando en posición (" + getStreet() + "," + getAvenue() + ")");
        testMovement();
        System.out.println(robotName + ": Finalizando");
    }

    public String getRobotInfo() {
        return robotName + " [Pos: (" + currentStreet + "," + currentAvenue + "), Dir: " + currentDirection + "]";
    }
}

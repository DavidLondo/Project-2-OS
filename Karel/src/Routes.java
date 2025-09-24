import kareltherobot.*;

public class Routes {

    private static void moveTo(int position, Directions.Direction direction, RobotThread robot) {
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
        moveTo(8, Directions.East, robot);
    }
}

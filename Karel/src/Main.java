import kareltherobot.*;
import java.awt.Color;

public class Main implements Directions {
    public static void main(String[] args) {
        try {
            
            World.readWorld("../worlds/Aranjuez.kwld");
            World.setVisible(true);
            World.setDelay(30);
            
            
            WorldUtils.initializeWorld(33, 20);
            
            System.out.println("Spawning intercalado de 28 robots AZULES y 28 robots VERDES (API simplificada)...");
            ZoneSpawner.spawnInterleaved(15, 15, 120);
            ZoneSpawner.spawnInterleaved(13, 13, 120);
            Thread.sleep(20000);
            System.out.println("Fin de espera principal.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
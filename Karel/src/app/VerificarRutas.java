package app;

public class VerificarRutas {
    public static void main(String[] args) {
        Ruta[] rutas = new Ruta[]{
                RutasPredefinidas.RutaDesdeAzulCorta,
                RutasPredefinidas.RutaDesdeAzulLarga,
                RutasPredefinidas.RutaDesdeVerdeCorta,
                RutasPredefinidas.RutaDesdeVerdeLarga
        };
        boolean ok = true;
        for (Ruta r : rutas) {
            boolean valida = r.esValida();
            System.out.println(r.nombre() + ": valida=" + valida + ", puntos=" + r.longitud() + ", inicio=" + r.inicio() + ", fin=" + r.fin());
            if (!valida) ok = false;
        }
        if (!ok) {
            System.exit(1);
        }
    }
}

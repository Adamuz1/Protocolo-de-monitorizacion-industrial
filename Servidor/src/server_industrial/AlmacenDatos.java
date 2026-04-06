package server_industrial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmacenDatos {

    public static Map<String, double[]> umbralesGenerales = new HashMap<>();

    static {
        umbralesGenerales.put("TEMP", new double[]{0.0, 50.0});   // Temperatura normal: entre 0 y 50 ºC
        umbralesGenerales.put("PRES", new double[]{0.5, 2.5});    // Presión normal: entre 0.5 y 2.5 BAR
        umbralesGenerales.put("VIBR", new double[]{0.0, 10.0});   // Vibración normal: entre 0 y 10 RMS
        // Añadimos los límites base para los sensores binarios de humedad (0 o 1)
        umbralesGenerales.put("HUM1", new double[]{0.0, 1.0});
        umbralesGenerales.put("HUM2", new double[]{0.0, 1.0});
        umbralesGenerales.put("HUM3", new double[]{0.0, 1.0});
    }

    public static class Medicion {

        String idSensor, variable, unidad;
        double valor;
        long marcaTemporal;

        public Medicion(String idSensor, String variable, double valor, String unidad, long marcaTemporal) {
            this.idSensor = idSensor;
            this.variable = variable;
            this.valor = valor;
            this.unidad = unidad;
            this.marcaTemporal = marcaTemporal;
        }
    }

    public static class Alerta {

        String idSensor, variable, razon;
        long marcaTemporal;

        public Alerta(String idSensor, String variable, String razon, long marcaTemporal) {
            this.idSensor = idSensor;
            this.variable = variable;
            this.razon = razon;
            this.marcaTemporal = marcaTemporal;
        }
    }

    public static List<Medicion> historialMediciones = new ArrayList<>();
    public static List<Alerta> historialAlertas = new ArrayList<>();
    public static Map<String, Map<String, double[]>> umbrales = new HashMap<>();
    public static Map<String, Long> ultimaActividad = new HashMap<>();
}

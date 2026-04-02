/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author anton
 */
package server_industrial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmacenDatos {

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

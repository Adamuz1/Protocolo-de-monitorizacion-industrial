package server_industrial;

/**
 *
 * @author anton
 */
import java.util.HashMap;

public class ProcesadorProtocolo {

    public static String procesar(String peticion) {
        try {
            String[] partes = peticion.trim().split(" ");
            if (partes.length < 4) {
                return "400 FORMATO_INCORRECTO"; // 
            }
            String metodo = partes[0];
            String idSensor = partes[1];
            long marcaTemporal = Long.parseLong(partes[2]);
            String cargaUtil = partes[3];

            AlmacenDatos.ultimaActividad.put(idSensor, System.currentTimeMillis());

            switch (metodo) {
                case "MEAS":
                    return manejarMedicion(idSensor, marcaTemporal, cargaUtil);
                case "CONF":
                    return manejarConfiguracion(idSensor, cargaUtil);
                case "QRY_C":
                    return manejarConsultaUmbrales(idSensor, cargaUtil);
                case "QRY_M":
                    return manejarConsultaMediciones(idSensor, cargaUtil);
                case "QRY_A":
                    return manejarConsultaAlertas(idSensor, cargaUtil);
                default:
                    return "400 METODO_DESCONOCIDO"; // 
            }
        } catch (Exception e) {
            return "500 ERROR_INTERNO"; // 
        }
    }

    private static String manejarMedicion(String idSensor, long marcaTemporal, String cargaUtil) {
        String[] datos = cargaUtil.split(":"); // [cite: 7]
        if (datos.length != 3) {
            return "400 FORMATO_INCORRECTO"; // 
        }
        String variable = datos[0];
        double valor = Double.parseDouble(datos[1]);
        String unidad = datos[2];

        AlmacenDatos.historialMediciones.add(new AlmacenDatos.Medicion(idSensor, variable, valor, unidad, marcaTemporal));

        if (AlmacenDatos.umbrales.containsKey(idSensor) && AlmacenDatos.umbrales.get(idSensor).containsKey(variable)) {
            double[] limites = AlmacenDatos.umbrales.get(idSensor).get(variable);
            if (valor < limites[0] || valor > limites[1]) {
                AlmacenDatos.historialAlertas.add(new AlmacenDatos.Alerta(idSensor, variable, "OUT_OF_RANGE", marcaTemporal)); // 
            }
        }
        return "200 OK"; // 
    }

    private static String manejarConfiguracion(String idSensor, String cargaUtil) {
        String[] datos = cargaUtil.split(":"); // [cite: 9]
        if (datos.length != 3) {
            return "400 FORMATO_INCORRECTO"; // 
        }
        String variable = datos[0];
        double min = Double.parseDouble(datos[1]);
        double max = Double.parseDouble(datos[2]);

        AlmacenDatos.umbrales.putIfAbsent(idSensor, new HashMap<>());
        AlmacenDatos.umbrales.get(idSensor).put(variable, new double[]{min, max});
        return "200 OK"; // 
    }

    private static String manejarConsultaUmbrales(String idSensor, String cargaUtil) {
        String variable = cargaUtil.split(":")[0];
        if (AlmacenDatos.umbrales.containsKey(idSensor) && AlmacenDatos.umbrales.get(idSensor).containsKey(variable)) {
            double[] limites = AlmacenDatos.umbrales.get(idSensor).get(variable);
            return "200 OK [" + limites[0] + "|" + limites[1] + "]"; // [cite: 14, 15]
        }
        return "404 NO_ENCONTRADO"; // 
    }

    private static String manejarConsultaMediciones(String idSensor, String cargaUtil) {
        String[] datos = cargaUtil.split(":"); // [cite: 11]
        String variable = datos[0];
        long inicio = Long.parseLong(datos[1]);
        long fin = datos[2].equals("NOW") ? System.currentTimeMillis() : Long.parseLong(datos[2]); // [cite: 11]

        StringBuilder constructorRespuesta = new StringBuilder("200 OK ");
        for (AlmacenDatos.Medicion m : AlmacenDatos.historialMediciones) {
            if (m.idSensor.equals(idSensor) && m.variable.equals(variable) && m.marcaTemporal >= inicio && m.marcaTemporal <= fin) {
                constructorRespuesta.append("[").append(m.marcaTemporal).append("|").append(m.valor).append("|").append(m.unidad).append("],"); // [cite: 13, 14]
            }
        }
        return constructorRespuesta.toString().replaceAll(",$", "");
    }

    private static String manejarConsultaAlertas(String idSensor, String cargaUtil) {
        String[] datos = cargaUtil.split(":"); // [cite: 11]
        String variable = datos[0];
        long inicio = Long.parseLong(datos[1]);
        long fin = datos[2].equals("NOW") ? System.currentTimeMillis() : Long.parseLong(datos[2]); // [cite: 11]

        StringBuilder constructorRespuesta = new StringBuilder("200 OK ");
        for (AlmacenDatos.Alerta a : AlmacenDatos.historialAlertas) {
            if (a.idSensor.equals(idSensor) && a.variable.equals(variable) && a.marcaTemporal >= inicio && a.marcaTemporal <= fin) {
                constructorRespuesta.append("[").append(a.marcaTemporal).append("|").append(a.variable).append("|").append(a.razon).append("],"); // [cite: 14]
            }
        }
        return constructorRespuesta.toString().replaceAll(",$", "");
    }
}

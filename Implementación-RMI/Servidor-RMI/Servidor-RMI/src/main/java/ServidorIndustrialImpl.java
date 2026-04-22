package rmi_industrial;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import server_industrial.AlmacenDatos; // Reutilizamos el modelo de dominio exacto

public class ServidorIndustrialImpl extends UnicastRemoteObject implements IServidorIndustrial {
    
    private static final SimpleDateFormat formatoVista = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public ServidorIndustrialImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized RespuestaServidor enviarMedicion(String idSensor, long timestamp, String variable, double valor, String unidad) throws RemoteException {
        AlmacenDatos.ultimaActividad.put(idSensor, System.currentTimeMillis());
        
        // 1. Guardar historial
        AlmacenDatos.historialMediciones.add(new AlmacenDatos.Medicion(idSensor, variable, valor, unidad, timestamp));

        // 2. Lógica del tanque (Reutilizada)
        if (variable.startsWith("HUM")) {
            if (variable.equals("HUM2") && valor == 0.0) {
                AlmacenDatos.historialAlertas.add(new AlmacenDatos.Alerta(idSensor, variable, "LOW_WATER", timestamp));
            } else if (variable.equals("HUM1") && valor == 0.0) {
                AlmacenDatos.historialAlertas.add(new AlmacenDatos.Alerta(idSensor, variable, "TANK_EMPTY", timestamp));
            }
            return new RespuestaServidor(200, "OK");
        }

        // 3. Lógica de umbrales numéricos (Reutilizada)
        double[] limites = null;
        if (AlmacenDatos.umbrales.containsKey(idSensor) && AlmacenDatos.umbrales.get(idSensor).containsKey(variable)) {
            limites = AlmacenDatos.umbrales.get(idSensor).get(variable);
        } else if (AlmacenDatos.umbralesGenerales.containsKey(variable)) {
            limites = AlmacenDatos.umbralesGenerales.get(variable);
        }

        if (limites != null && (valor < limites[0] || valor > limites[1])) {
            AlmacenDatos.historialAlertas.add(new AlmacenDatos.Alerta(idSensor, variable, "OUT_OF_RANGE", timestamp));
        }

        return new RespuestaServidor(200, "OK");
    }

    @Override
    public synchronized RespuestaServidor consultarHistoricoMediciones(String idSensor, String variable, long inicio, long fin) throws RemoteException {
        // Actualizamos la actividad del sensor
        AlmacenDatos.ultimaActividad.put(idSensor, System.currentTimeMillis());
        
        RespuestaServidor respuesta = new RespuestaServidor(200, "OK");
        
        // Si el cliente envía 0 como fecha de fin, simulamos el comportamiento de "NOW"
        long tiempoFinReal = (fin == 0) ? System.currentTimeMillis() : fin;
        
        for (AlmacenDatos.Medicion m : AlmacenDatos.historialMediciones) {
            if (m.idSensor.equals(idSensor) && m.variable.equals(variable) && m.marcaTemporal >= inicio && m.marcaTemporal <= tiempoFinReal) {
                String fechaLegible = formatoVista.format(new Date(m.marcaTemporal * 1000L));
                
                // DESCOMPOSICIÓN DE DATOS (Requisito clave RMI): Guardamos un array limpio
                respuesta.agregarRegistro(new String[]{fechaLegible, String.valueOf(m.valor), m.unidad});
            }
        }
        
        if (respuesta.getDatos().isEmpty()) {
            return new RespuestaServidor(404, "NO_ENCONTRADO");
        }
        return respuesta;
    }
    @Override
    public synchronized RespuestaServidor consultarUmbrales(String idSensor, String variable) throws RemoteException {
        // Actualizamos actividad
        AlmacenDatos.ultimaActividad.put(idSensor, System.currentTimeMillis());
        
        // Reutilizamos la búsqueda en el AlmacenDatos
        if (AlmacenDatos.umbrales.containsKey(idSensor) && AlmacenDatos.umbrales.get(idSensor).containsKey(variable)) {
            double[] limites = AlmacenDatos.umbrales.get(idSensor).get(variable);
            
            RespuestaServidor respuesta = new RespuestaServidor(200, "OK");
            
            // Guardamos los límites descompuestos en el array estructurado
            respuesta.agregarRegistro(new String[]{String.valueOf(limites[0]), String.valueOf(limites[1])});
            return respuesta;
        }
        
        // Caso inválido ya definido en el protocolo original
        return new RespuestaServidor(404, "NO_ENCONTRADO");
    }
    @Override
    public synchronized RespuestaServidor consultarHistoricoAlertas(String idSensor, String variable, long inicio, long fin) throws RemoteException {
        // Actualizamos actividad
        AlmacenDatos.ultimaActividad.put(idSensor, System.currentTimeMillis());
        
        RespuestaServidor respuesta = new RespuestaServidor(200, "OK");
        
        // Manejo del tiempo "NOW"
        long tiempoFinReal = (fin == 0) ? System.currentTimeMillis() : fin;
        
        for (AlmacenDatos.Alerta a : AlmacenDatos.historialAlertas) {
            if (a.idSensor.equals(idSensor) && a.variable.equals(variable) && a.marcaTemporal >= inicio && a.marcaTemporal <= tiempoFinReal) {
                String fechaLegible = formatoVista.format(new Date(a.marcaTemporal * 1000L));
                
                // Descomponemos: Fecha, Variable, Razón de la alerta
                respuesta.agregarRegistro(new String[]{fechaLegible, a.variable, a.razon});
            }
        }
        
        // Validación y control de errores si no hay datos
        if (respuesta.getDatos().isEmpty()) {
            return new RespuestaServidor(404, "NO_ENCONTRADO");
        }
        return respuesta;
    }
}
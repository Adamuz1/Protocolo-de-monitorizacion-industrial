package rmi_industrial;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IServidorIndustrial extends Remote {
    // 1. Envío de mediciones (MEAS)
    RespuestaServidor enviarMedicion(String idSensor, long timestamp, String variable, double valor, String unidad) throws RemoteException;
    
    // 2. Configuración (CONF)
    RespuestaServidor configurarUmbrales(String idSensor, String variable, double min, double max) throws RemoteException;
    
    // 3. Consultas (QRY_C, QRY_M, QRY_A)
    RespuestaServidor consultarUmbrales(String idSensor, String variable) throws RemoteException;
    RespuestaServidor consultarHistoricoMediciones(String idSensor, String variable, long inicio, long fin) throws RemoteException;
    RespuestaServidor consultarHistoricoAlertas(String idSensor, String variable, long inicio, long fin) throws RemoteException;
}

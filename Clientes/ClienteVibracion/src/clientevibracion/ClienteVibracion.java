package clientevibracion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class ClienteVibracion {

    public static void main(String[] args) {
        final String IP_SERVIDOR = "localhost";
        final int PUERTO_SERVIDOR = 5000;
        final String SENSOR_ID = "SENS_VIBR_01";
        
        float vibracionActual = 2.5f; // Valor inicial simulado en RMS

        try (DatagramSocket socketUDP = new DatagramSocket()) {
            socketUDP.setSoTimeout(2000); 
            InetAddress direccionServidor = InetAddress.getByName(IP_SERVIDOR);

            System.out.println("--- Iniciado Sensor de Vibración de Batidora (UDP) ---");

            while (true) {
                // Simular cambio físico (Añade "ruido" a la vibración)
                vibracionActual += (float) ((Math.random() * 0.4) - 0.2); 
                if (vibracionActual < 0) vibracionActual = 0; // No hay vibración negativa

                // Construir el mensaje según el ABNF
                long timestamp = System.currentTimeMillis() / 1000L;
                String valorStr = String.format(Locale.US, "%.1f", vibracionActual);
                
                String mensaje = "MEAS " + SENSOR_ID + " " + timestamp + " VIBR:" + valorStr + ":RMS\r\n";
                byte[] bufferEnvio = mensaje.getBytes();

                // Enviar el Datagrama
                DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, direccionServidor, PUERTO_SERVIDOR);
                socketUDP.send(paqueteEnvio);
                System.out.print("[Tx] " + mensaje);

                // Esperar Respuesta (ACK) del Servidor
                try {
                    byte[] bufferRecepcion = new byte[1024];
                    DatagramPacket paqueteRecepcion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
                    socketUDP.receive(paqueteRecepcion);
                    
                    String respuesta = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
                    System.out.println("[Rx] Servidor responde: " + respuesta.trim());
                } catch (SocketTimeoutException e) {
                    System.err.println("[!] No se recibió respuesta del servidor (Timeout).");
                }

                // Esperar 2 segundos antes de la siguiente medición
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            System.err.println("Error en el cliente de vibración: " + e.getMessage());
        }
    }
}
package clientetemperatura;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class ClienteTemperatura {

    public static void main(String[] args) {
        final String IP_SERVIDOR = "localhost";
        final int PUERTO_SERVIDOR = 5000;
        final String SENSOR_ID = "SENS_TEMP_01";
        
        float temperaturaActual = 20.0f;

        try (DatagramSocket socketUDP = new DatagramSocket()) {
            socketUDP.setSoTimeout(2000); 
            InetAddress direccionServidor = InetAddress.getByName(IP_SERVIDOR);

            System.out.println("--- Iniciado Sensor de Temperatura (UDP) ---");

            while (true) {
                // 1. Simular cambio físico (ej. sube o baja un poco aleatoriamente)
                temperaturaActual += (float) (Math.random() - 0.5); 

                // 2. Construir el mensaje según el ABNF
                long timestamp = System.currentTimeMillis() / 1000L;
                String valorStr = String.format(Locale.US, "%.1f", temperaturaActual);
                
                String mensaje = "MEAS " + SENSOR_ID + " " + timestamp + " TEMP:" + valorStr + ":C\r\n";
                byte[] bufferEnvio = mensaje.getBytes();

                // 3. Enviar el Datagrama
                DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, direccionServidor, PUERTO_SERVIDOR);
                socketUDP.send(paqueteEnvio);
                System.out.print("[Tx] " + mensaje);

                // 4. Esperar Respuesta (ACK) del Servidor
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
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}
package clientehumedad;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ClienteHumedad {

    public static void main(String[] args) {
        final String IP_SERVIDOR = "localhost";
        final int PUERTO_SERVIDOR = 5000;
        final String SENSOR_ID = "SENS_HUM_01";
        
        // Umbrales de humedad definidos en el sensor
        final float UMBRAL_1 = 30.0f;
        final float UMBRAL_2 = 50.0f;
        final float UMBRAL_3 = 70.0f;
        
        float humedadRelativaActual = 45.0f; // Humedad inicial simulada en %

        try (DatagramSocket socketUDP = new DatagramSocket()) {
            socketUDP.setSoTimeout(1000);
            InetAddress direccionServidor = InetAddress.getByName(IP_SERVIDOR);
            System.out.println("--- Iniciado Sensor de Humedad de 3 Umbrales (UDP) ---");

            while (true) {
                // Simular cambio de humedad
                humedadRelativaActual += (float) ((Math.random() * 4) - 2); 
                if (humedadRelativaActual < 0) humedadRelativaActual = 0;
                if (humedadRelativaActual > 100) humedadRelativaActual = 100;

                long timestamp = System.currentTimeMillis() / 1000L;
                
                // Evaluar umbrales
                String valHum1 = (humedadRelativaActual >= UMBRAL_1) ? "YES" : "NO";
                String valHum2 = (humedadRelativaActual >= UMBRAL_2) ? "YES" : "NO";
                String valHum3 = (humedadRelativaActual >= UMBRAL_3) ? "YES" : "NO";

                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM1", valHum1, "%");
                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM2", valHum2, "%");
                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM3", valHum3, "%");

                System.out.println("-------------------------------------------------");
                Thread.sleep(3000); // Esperar 3 segundos para el siguiente ciclo
            }

        } catch (Exception e) {
            System.err.println("Error en el cliente de humedad: " + e.getMessage());
        }
    }

    // Método auxiliar para no repetir código al enviar datagramas
    private static void enviarMedicion(DatagramSocket socket, InetAddress ip, int puerto, String id, long ts, String var, String val, String unit) throws IOException {
        String mensaje = "MEAS " + id + " " + ts + " " + var + ":" + val + ":" + unit + "\r\n";
        byte[] bufferEnvio = mensaje.getBytes();
        DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, ip, puerto);
        socket.send(paqueteEnvio);
        System.out.print("[Tx] " + mensaje);
        
        // Recepción rápida
        try {
            byte[] bufRx = new byte[256];
            DatagramPacket paqueteRx = new DatagramPacket(bufRx, bufRx.length);
            socket.receive(paqueteRx);
            System.out.println("  -> [Rx] " + new String(paqueteRx.getData(), 0, paqueteRx.getLength()).trim());
        } catch (SocketTimeoutException e) {
            System.err.println("  -> [!] Sin respuesta.");
        }
    }
}
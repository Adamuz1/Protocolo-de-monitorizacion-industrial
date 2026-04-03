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
        
        // Nivel de agua simulado en el tanque (0 = Vacío, 1 = Bajo, 2 = Medio, 3 = Alto)
        int nivelAguaSimulado = 0; 
        boolean llenando = true; // Variable para simular que el tanque se llena y se vacía

        try (DatagramSocket socketUDP = new DatagramSocket()) {
            socketUDP.setSoTimeout(1000); // 1 segundo de espera máxima para la respuesta
            InetAddress direccionServidor = InetAddress.getByName(IP_SERVIDOR);
            
            System.out.println("--- Iniciado Sensor de Humedad (3 Niveles Discretos) ---");

            while (true) {
                // 1. Simulación física: El tanque se llena hasta 3 y luego se vacía hasta 0
                if (llenando) {
                    nivelAguaSimulado++;
                    if (nivelAguaSimulado >= 3) llenando = false;
                } else {
                    nivelAguaSimulado--;
                    if (nivelAguaSimulado <= 0) llenando = true;
                }

                // 2. Lógica interna de los 3 sensores físicos (Cascada de 1s y 0s)
                int hum1 = (nivelAguaSimulado >= 1) ? 1 : 0;
                int hum2 = (nivelAguaSimulado >= 2) ? 1 : 0;
                int hum3 = (nivelAguaSimulado >= 3) ? 1 : 0;

                long timestamp = System.currentTimeMillis() / 1000L;
                
                System.out.println("Nivel físico del tanque: " + nivelAguaSimulado + "/3");

                // 3. Enviar la información al Servidor usando el ABNF acordado (Enviamos 1 o 0 con unidad %)
                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM1", String.valueOf(hum1), "%");
                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM2", String.valueOf(hum2), "%");
                enviarMedicion(socketUDP, direccionServidor, PUERTO_SERVIDOR, SENSOR_ID, timestamp, "HUM3", String.valueOf(hum3), "%");

                System.out.println("-------------------------------------------------");
                Thread.sleep(3000); // Esperar 3 segundos para la siguiente medición
            }

        } catch (Exception e) {
            System.err.println("Error en el cliente de humedad: " + e.getMessage());
        }
    }

    // Método auxiliar para construir el mensaje ABNF, enviarlo y leer el ACK del servidor
    private static void enviarMedicion(DatagramSocket socket, InetAddress ip, int puerto, String id, long ts, String var, String val, String unit) throws IOException {
        // Construimos el string exacto del ABNF: MEAS ID TIMESTAMP VAR:VAL:UNIT\r\n
        String mensaje = "MEAS " + id + " " + ts + " " + var + ":" + val + ":" + unit + "\r\n";
        byte[] bufferEnvio = mensaje.getBytes();
        
        DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, ip, puerto);
        socket.send(paqueteEnvio);
        System.out.print("[Tx] " + mensaje);
        
        // Recibir confirmación del servidor
        try {
            byte[] bufRx = new byte[256];
            DatagramPacket paqueteRx = new DatagramPacket(bufRx, bufRx.length);
            socket.receive(paqueteRx);
            System.out.println("  -> [Rx] " + new String(paqueteRx.getData(), 0, paqueteRx.getLength()).trim());
        } catch (SocketTimeoutException e) {
            System.err.println("  -> [!] Timeout: El servidor no respondió a " + var);
        }
    }
}
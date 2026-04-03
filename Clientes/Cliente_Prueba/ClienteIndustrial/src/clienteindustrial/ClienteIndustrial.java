package clienteindustrial;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClienteIndustrial {

    public static void main(String[] args) {
        
        try (DatagramSocket conectorUDP = new DatagramSocket();
             Scanner escaner = new Scanner(System.in)) {

            InetAddress ipServidor = InetAddress.getByName("localhost");
            int puertoServidor = 5000;

            System.out.println("=== Terminal de Cliente Industrial ===");
            System.out.println("Escribe los comandos según la gramática ABNF.");
            System.out.println("Escribe '0' para salir.");
            System.out.println("Ejemplo: CONF SENSOR_01 1670000000 TEMP:10.0:30.0\n");

            while (true) {
                System.out.print("Enviar -> ");
                String comando = escaner.nextLine();

                if (comando.equals("0")) {
                    System.out.println("Cerrando cliente...");
                    break;
                }

                if (comando.trim().isEmpty()) {
                    continue;
                }

                byte[] buferEnvio = comando.getBytes();
                DatagramPacket paqueteEnvio = new DatagramPacket(buferEnvio, buferEnvio.length, ipServidor, puertoServidor);
                conectorUDP.send(paqueteEnvio);

                byte[] buferRecepcion = new byte[1024];
                DatagramPacket paqueteRecepcion = new DatagramPacket(buferRecepcion, buferRecepcion.length);
                conectorUDP.receive(paqueteRecepcion);

                String respuesta = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
                System.out.println("Servidor <- " + respuesta.trim() + "\n");
            }

        } catch (Exception e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }
}

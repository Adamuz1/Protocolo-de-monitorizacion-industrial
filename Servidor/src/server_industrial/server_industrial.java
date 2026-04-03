package server_industrial;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class server_industrial {

    public static void main(String[] args) {

        Thread hiloUdp = new Thread(() -> iniciarServidorUDP());
        hiloUdp.start();

        Thread hiloMonitor = new Thread(() -> comprobarDesconexiones());
        hiloMonitor.start();

        Scanner escaner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Panel de Control ---");
            System.out.println("1. Ver métricas del servidor");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            String entrada = escaner.nextLine();

            switch (entrada) {
                case "1" -> {
                    System.out.println("\n[MÉTRICAS]");
                    System.out.println("Sensores activos registrados: " + AlmacenDatos.ultimaActividad.size());
                    System.out.println("Mediciones en histórico: " + AlmacenDatos.historialMediciones.size());
                    System.out.println("Alertas disparadas: " + AlmacenDatos.historialAlertas.size());
                }
                case "0" -> {
                    System.out.println("Apagando el servidor...");
                    salir = true;
                    System.exit(0);
                }
                default ->
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void iniciarServidorUDP() {
        final int PUERTO = 5000;
        byte[] bufer = new byte[1024];

        try {
            DatagramSocket conectorUDP = new DatagramSocket(PUERTO);

            while (true) {
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);
                conectorUDP.receive(peticion);

                String mensaje = new String(peticion.getData(), 0, peticion.getLength());
                String respuestaCadena = ProcesadorProtocolo.procesar(mensaje);

                bufer = (respuestaCadena + "\r\n").getBytes(); // Finaliza con CRLF según la norma

                int puertoCliente = peticion.getPort();
                InetAddress direccion = peticion.getAddress();
                DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length, direccion, puertoCliente);

                conectorUDP.send(respuesta);
                bufer = new byte[1024];
            }

        } catch (SocketException ex) {
            Logger.getLogger(server_industrial.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(server_industrial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void comprobarDesconexiones() {
        while (true) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                break;
            }
            long ahora = System.currentTimeMillis();
            for (String idSensor : AlmacenDatos.ultimaActividad.keySet()) {
                if (ahora - AlmacenDatos.ultimaActividad.get(idSensor) > 60000) {
                    AlmacenDatos.historialAlertas.add(new AlmacenDatos.Alerta(idSensor, "SYS", "DISCONNECTED", ahora)); // 
                }
            }
        }
    }
}

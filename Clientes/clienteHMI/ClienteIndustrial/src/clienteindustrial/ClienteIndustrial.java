package clienteindustrial;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClienteIndustrial {

    public static void main(String[] args) {
        
        try (DatagramSocket conectorUDP = new DatagramSocket();
             Scanner escaner = new Scanner(System.in)) {

            // Configuramos un tiempo de espera de 3 segundos
            conectorUDP.setSoTimeout(3000); 
            InetAddress ipServidor = InetAddress.getByName("localhost");
            int puertoServidor = 5000;

            System.out.println("=== Panel HMI (Cliente Industrial) ===");
            System.out.println("Conectado a: " + ipServidor.getHostAddress() + ":" + puertoServidor);

            while (true) {
                mostrarMenu();
                System.out.print("Seleccione una opción -> ");
                String opcion = escaner.nextLine();

                if (opcion.equals("0")) {
                    System.out.println("Cerrando cliente...");
                    break;
                }

                long marcaTemporal = System.currentTimeMillis() / 1000L;
                String comandoABNF = "";

                switch (opcion) {
                    case "1":
                        System.out.print("ID del Sensor (ej. SENSOR_01): ");
                        String idConf = escaner.nextLine();
                        System.out.print("Variable (ej. TEMP): ");
                        String varConf = escaner.nextLine();
                        System.out.print("Umbral Mínimo: ");
                        String min = escaner.nextLine();
                        System.out.print("Umbral Máximo: ");
                        String max = escaner.nextLine();
                        comandoABNF = "CONF " + idConf + " " + marcaTemporal + " " + varConf + ":" + min + ":" + max;
                        break;

                    case "2":
                        System.out.print("ID del Sensor: ");
                        String idQryC = escaner.nextLine();
                        System.out.print("Variable a consultar (ej. TEMP): ");
                        String varQryC = escaner.nextLine();
                        comandoABNF = "QRY_C " + idQryC + " " + marcaTemporal + " " + varQryC;
                        break;

                    case "3":
                        System.out.print("ID del Sensor: ");
                        String idQryM = escaner.nextLine();
                        System.out.print("Variable a consultar (ej. TEMP): ");
                        String varQryM = escaner.nextLine();
                        comandoABNF = "QRY_M " + idQryM + " " + marcaTemporal + " " + varQryM + ":0:NOW";
                        break;

                    case "4":
                        System.out.print("ID del Sensor: ");
                        String idQryA = escaner.nextLine();
                        System.out.print("Variable a consultar (ej. TEMP): ");
                        String varQryA = escaner.nextLine();
                        comandoABNF = "QRY_A " + idQryA + " " + marcaTemporal + " " + varQryA + ":0:NOW";
                        break;

                    case "5":
                        System.out.print("Escribe el comando ABNF completo: ");
                        comandoABNF = escaner.nextLine();
                        break;

                    default:
                        System.out.println("Opción no válida.\n");
                        continue;
                }

                if (comandoABNF.trim().isEmpty()) {
                    continue;
                }

                // Ejecutamos el envío y recepción
                procesarComando(conectorUDP, ipServidor, puertoServidor, comandoABNF);
            }

        } catch (Exception e) {
            System.out.println("Error de red crítico: " + e.getMessage());
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ DE OPERACIONES HMI ---");
        System.out.println("1. Configurar Umbrales (CONF)");
        System.out.println("2. Consultar Umbrales Actuales (QRY_C)");
        System.out.println("3. Histórico de Mediciones (QRY_M)");
        System.out.println("4. Histórico de Alertas (QRY_A)");
        System.out.println("5. Enviar comando manual (Raw)");
        System.out.println("0. Salir");
    }

    private static void procesarComando(DatagramSocket conectorUDP, InetAddress ipServidor, int puertoServidor, String comandoABNF) {
        try {
            // Añadimos el salto de línea por si el servidor es estricto con el protocolo
            String comandoFinal = comandoABNF + "\r\n";
            byte[] buferEnvio = comandoFinal.getBytes();
            
            DatagramPacket paqueteEnvio = new DatagramPacket(buferEnvio, buferEnvio.length, ipServidor, puertoServidor);
            conectorUDP.send(paqueteEnvio);
            System.out.println("\nCliente -> " + comandoABNF);

            // Aumentamos a 2048 bytes (o más) porque las respuestas QRY_M / QRY_A devuelven mucho texto
            byte[] buferRecepcion = new byte[2048];
            DatagramPacket paqueteRecepcion = new DatagramPacket(buferRecepcion, buferRecepcion.length);
            
            conectorUDP.receive(paqueteRecepcion);
            String respuesta = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
            
            System.out.println("Servidor <- \n" + respuesta.trim());
            System.out.println("-------------------------------------------");

        } catch (SocketTimeoutException e) {
            System.out.println("[!] Tiempo de espera agotado. El servidor no respondió.");
        } catch (IOException e) {
            System.out.println("[!] Error al comunicar con el servidor: " + e.getMessage());
        }
    }
}
package rmi_industrial;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteIndustrialRMI {

    public static void main(String[] args) {
        
        // Descomentar si hay problemas de red con Docker/VirtualBox indicando la IP del servidor
        // System.setProperty("java.rmi.server.hostname", "172.20.10.12"); 

        Scanner escaner = new Scanner(System.in);

        try {
            System.out.println("=== Panel HMI (Cliente Industrial RMI) ===");
            System.out.println("Buscando servidor en el registro RMI...");
            
            // 1. Conexión al registro RMI (localhost puerto 1099 por defecto)
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // 2. Obtener el Stub con el nombre exacto que usó el servidor
            IServidorIndustrial servidor = (IServidorIndustrial) registry.lookup("ServicioIndustrial");
            
            System.out.println("¡Conexión establecida con éxito!\n");

            while (true) {
                mostrarMenu();
                System.out.print("Seleccione una opción -> ");
                String opcion = escaner.nextLine();

                if (opcion.equals("0")) {
                    System.out.println("Cerrando cliente...");
                    break;
                }

                // 3. Gestión de errores y excepciones remotas
                try {
                    procesarOpcion(opcion, escaner, servidor);
                } catch (RemoteException re) {
                    System.err.println("[!] ERROR DE RED: Fallo al comunicar con el servidor.");
                    System.err.println("Detalle: " + re.getMessage());
                } catch (Exception e) {
                    System.err.println("[!] ERROR LOCAL: Entrada inválida. Compruebe los datos numéricos.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error crítico al iniciar el cliente: " + e.getMessage());
        } finally {
            escaner.close();
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ DE OPERACIONES HMI (RMI) ---");
        System.out.println("1. Configurar Umbrales (CONF)");
        System.out.println("2. Consultar Umbrales Actuales (QRY_C)");
        System.out.println("3. Histórico de Mediciones (QRY_M)");
        System.out.println("4. Histórico de Alertas (QRY_A)");
        System.out.println("0. Salir");
    }

    private static void procesarOpcion(String opcion, Scanner escaner, IServidorIndustrial servidor) throws RemoteException {
        RespuestaServidor respuesta = null;
        String idSensor, variable;
        long inicio = 0;
        long fin = System.currentTimeMillis() / 1000L; // Tiempo actual

        switch (opcion) {
            case "1":
                System.out.print("ID del Sensor (ej. MOTOR_01): ");
                idSensor = escaner.nextLine();
                System.out.print("Variable (ej. TEMP): ");
                variable = escaner.nextLine();
                System.out.print("Umbral Mínimo: ");
                double min = Double.parseDouble(escaner.nextLine());
                System.out.print("Umbral Máximo: ");
                double max = Double.parseDouble(escaner.nextLine());
                
                respuesta = servidor.configurarUmbrales(idSensor, variable, min, max);
                break;

            case "2":
                System.out.print("ID del Sensor: ");
                idSensor = escaner.nextLine();
                System.out.print("Variable a consultar: ");
                variable = escaner.nextLine();
                
                respuesta = servidor.consultarUmbrales(idSensor, variable);
                break;

            case "3":
                System.out.print("ID del Sensor: ");
                idSensor = escaner.nextLine();
                System.out.print("Variable a consultar: ");
                variable = escaner.nextLine();
                
                respuesta = servidor.consultarHistoricoMediciones(idSensor, variable, inicio, fin);
                break;

            case "4":
                System.out.print("ID del Sensor: ");
                idSensor = escaner.nextLine();
                System.out.print("Variable a consultar: ");
                variable = escaner.nextLine();
                
                respuesta = servidor.consultarHistoricoAlertas(idSensor, variable, inicio, fin);
                break;

            default:
                System.out.println("Opción no válida.");
                return;
        }

        mostrarRespuestaEstructurada(respuesta);
    }

    private static void mostrarRespuestaEstructurada(RespuestaServidor respuesta) {
        if (respuesta == null) return;

        System.out.println("\n--- RESPUESTA DEL SERVIDOR ---");
        // Usamos los métodos exactos que tiene tu clase RespuestaServidor
        System.out.println("Estado: " + respuesta.getCodigoEstado() + " - " + respuesta.getDescripcion());
        
        if (respuesta.getDatos() != null && !respuesta.getDatos().isEmpty()) {
            System.out.println("Datos recuperados:");
            for (String[] registro : respuesta.getDatos()) {
                System.out.print(" -> [ ");
                for (int i = 0; i < registro.length; i++) {
                    System.out.print(registro[i] + (i < registro.length - 1 ? " | " : ""));
                }
                System.out.println(" ]");
            }
        } else {
            System.out.println("Sin datos adicionales.");
        }
        System.out.println("------------------------------");
    }
}
package rmi_industrial;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteIndustrialRMI {

    public static void main(String[] args) {
        

        Scanner escaner = new Scanner(System.in);

        try {
            System.out.println("=== Panel HMI (Cliente Industrial RMI) ===");
            System.out.println("Buscando servidor en el registro RMI...");
            
            // 1. Conexión al registro RMI
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
        long inicio = 0;
        long fin = System.currentTimeMillis() / 1000L; // Tiempo actual

        switch (opcion) {
            case "1":
                String[] datosConf = seleccionarSensor(escaner);
                System.out.print("Umbral Mínimo: ");
                double min = Double.parseDouble(escaner.nextLine());
                System.out.print("Umbral Máximo: ");
                double max = Double.parseDouble(escaner.nextLine());
                
                respuesta = servidor.configurarUmbrales(datosConf[0], datosConf[1], min, max);
                break;

            case "2":
                String[] datosQryC = seleccionarSensor(escaner);
                respuesta = servidor.consultarUmbrales(datosQryC[0], datosQryC[1]);
                break;

            case "3":
                String[] datosQryM = seleccionarSensor(escaner);
                respuesta = servidor.consultarHistoricoMediciones(datosQryM[0], datosQryM[1], inicio, fin);
                break;

            case "4":
                String[] datosQryA = seleccionarSensor(escaner);
                respuesta = servidor.consultarHistoricoAlertas(datosQryA[0], datosQryA[1], inicio, fin);
                break;

            default:
                System.out.println("Opción no válida.");
                return;
        }

        mostrarRespuestaEstructurada(respuesta);
    }

    private static String[] seleccionarSensor(Scanner escaner) {
        System.out.println("\n-- Lista de Sensores (Favoritos) --");
        System.out.println("1. MOTOR_01 (TEMP)");
        System.out.println("2. TANQUE_A (HUM1)");
        System.out.println("3. TANQUE_A (HUM2)");
        System.out.println("4. TANQUE_A (HUM3)");
        System.out.println("N. Añadir nuevo sensor manualmente");
        System.out.print("Selección: ");
        
        String seleccion = escaner.nextLine().trim().toUpperCase();
        String idSensor = "";
        String variable = "";
        
        switch (seleccion) {
            case "1":
                idSensor = "MOTOR_01"; variable = "TEMP";
                break;
            case "2":
                idSensor = "TANQUE_A"; variable = "HUM1";
                break;
            case "3":
                idSensor = "TANQUE_A"; variable = "HUM2";
                break;
            case "4":
                idSensor = "TANQUE_A"; variable = "HUM3";
                break;
            case "N":
            default:
                if (!seleccion.equals("N")) {
                    System.out.println("Opción no reconocida. Modo manual activado.");
                }
                System.out.print("ID del Sensor (ej. MOTOR_01): ");
                idSensor = escaner.nextLine();
                System.out.print("Variable (ej. TEMP): ");
                variable = escaner.nextLine();
                break;
        }
        
        System.out.println("\n[DATOS] Consultando " + variable + " en " + idSensor + "...");
        return new String[]{idSensor, variable};
    }

    private static void mostrarRespuestaEstructurada(RespuestaServidor respuesta) {
        if (respuesta == null) return;

        System.out.println("\n--- RESPUESTA DEL SERVIDOR ---");
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
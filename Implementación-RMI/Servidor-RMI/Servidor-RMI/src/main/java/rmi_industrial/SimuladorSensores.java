package rmi_industrial;

import java.util.Random;

public class SimuladorSensores implements Runnable {
    
    private final IServidorIndustrial servicioServidor;

    // Pasamos la referencia del servidor para poder usar el método enviarMedicion
    public SimuladorSensores(IServidorIndustrial servicioServidor) {
        this.servicioServidor = servicioServidor;
    }

    @Override
    public void run() {
        Random rand = new Random();
        System.out.println("[SIMULADOR] Iniciando generación automática de datos...");

        while (true) {
            try {
                // Simular el envío de datos cada 5 segundos
                Thread.sleep(5000); 
                long timestamp = System.currentTimeMillis() / 1000L;

                // --- Simulación del Tanque de Agua (HUM1) ---
                // 80% de probabilidad de estar mojado (1.0), 20% de secarse (0.0 -> genera alerta)
                double hum1 = rand.nextDouble() > 0.2 ? 1.0 : 0.0;
                servicioServidor.enviarMedicion("TANQUE_A", timestamp, "HUM1", hum1, "%");

                // --- Simulación de un Motor (TEMP) ---
                // Temperatura base de 40ºC + hasta 20ºC aleatorios
                double tempMotor = 40.0 + (rand.nextDouble() * 20.0);
                tempMotor = Math.round(tempMotor * 100.0) / 100.0; // Redondear a 2 decimales
                servicioServidor.enviarMedicion("MOTOR_01", timestamp, "TEMP", tempMotor, "C");

            } catch (Exception e) {
                System.err.println("[SIMULADOR ERROR] " + e.getMessage());
            }
        }
    }
}
package rmi_industrial;

import java.util.Random;

public class SimuladorSensores implements Runnable {
    
    private final IServidorIndustrial servicioServidor;

    public SimuladorSensores(IServidorIndustrial servicioServidor) {
        this.servicioServidor = servicioServidor;
    }

    @Override
    public void run() {
        Random rand = new Random();
        System.out.println("[SIMULADOR] Iniciando generación automática de datos...");
        
        // El tanque empieza lleno (Nivel 3)
        int nivelTanque = 3; 

        while (true) {
            try {
                // Simular el envío de datos cada 5 segundos
                Thread.sleep(5000); 
                long timestamp = System.currentTimeMillis() / 1000L;

                // --- 1. SIMULACIÓN DEL TANQUE MULTISENSOR ---
                // Hacemos que el nivel del tanque cambie aleatoriamente (-1, 0, o +1)
                int cambio = rand.nextInt(3) - 1; 
                nivelTanque += cambio;
                
                // Ponemos topes para que no baje de 0 ni pase de 3
                if (nivelTanque > 3) nivelTanque = 3;
                if (nivelTanque < 0) nivelTanque = 0;

                // Calculamos qué sensores están mojados (1.0) y cuáles secos (0.0) según el nivel
                double hum3 = (nivelTanque >= 3) ? 1.0 : 0.0;
                double hum2 = (nivelTanque >= 2) ? 1.0 : 0.0;
                double hum1 = (nivelTanque >= 1) ? 1.0 : 0.0;

                // Enviamos las 3 mediciones al servidor RMI al mismo tiempo
                servicioServidor.enviarMedicion("TANQUE_A", timestamp, "HUM3", hum3, "%");
                servicioServidor.enviarMedicion("TANQUE_A", timestamp, "HUM2", hum2, "%");
                servicioServidor.enviarMedicion("TANQUE_A", timestamp, "HUM1", hum1, "%");

                // --- 2. SIMULACIÓN DEL MOTOR (TEMP) ---
                double tempMotor = 40.0 + (rand.nextDouble() * 20.0);
                tempMotor = Math.round(tempMotor * 100.0) / 100.0;
                servicioServidor.enviarMedicion("MOTOR_01", timestamp, "TEMP", tempMotor, "C");

            } catch (Exception e) {
                System.err.println("[SIMULADOR ERROR] " + e.getMessage());
            }
        }
    }
}
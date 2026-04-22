

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Servidor {
    public static void main(String[] args) {
        try {
            // 1. Instanciamos la implementación
            ServidorIndustrialImpl servicio = new ServidorIndustrialImpl();
            
            // 2. Creamos el registro RMI en el puerto por defecto (1099)
            Registry registro = LocateRegistry.createRegistry(1099);
            
            // 3. Publicamos el objeto remoto con un nombre
            registro.rebind("ServicioIndustrial", servicio);
            
            System.out.println("Servidor RMI Industrial listo y esperando peticiones...");
            
            // 4. (Aquí puedes pegar tu hilo monitor y tu bucle while del menú original)
            
        } catch (Exception e) {
            System.err.println("Error grave en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

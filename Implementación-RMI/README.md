# Protocolo de Monitorización Industrial mediante Java RMI (Remote Method Invocation)

Este proyecto implementa un sistema distribuido orientado a un entorno de planta industrial. Simula un ecosistema donde múltiples clientes (que actúan como sensores) envían periódicamente mediciones y datos telemétricos a un servidor central para su procesamiento, monitorización y almacenamiento.

A diferencia de los enfoques tradicionales basados en el paso de mensajes crudos a través de Sockets UDP/TCP, esta evolución del protocolo utiliza **Java RMI**. Esto permite una comunicación fluida basada en objetos distribuidos, donde la red se vuelve transparente y los clientes pueden invocar métodos directamente en el servidor remoto.

## 🏗️ Arquitectura del Sistema

El proyecto se divide en tres componentes fundamentales soportados por la tecnología RMI:

1. **Interfaz Remota (Contrato):** Define los métodos que el servidor expone y que el cliente puede invocar (ej. `enviarMedicion`, `consultarHistorico`).
2. **Servidor Central:** Implementa la lógica de negocio, gestiona el almacenamiento en memoria (o base de datos) y se registra en el *RMI Registry* para ser descubierto.
3. **Cliente / Sensores:** Entidades distribuidas que buscan al servidor en el registro RMI y llaman a sus métodos enviando parámetros tipados y recibiendo objetos de respuesta complejos.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java (JDK 8 o superior)
* **Middleware:** Java RMI (`java.rmi.*`)

## ⚙️ Estructura de Datos (RespuestaServidor)
Para mantener un diseño limpio y robusto, todas las consultas devuelven un objeto serializable llamado `RespuestaServidor`. Este objeto empaqueta:
* Código de estado (ej. 200 OK, 404 NO ENCONTRADO).
* Mensaje descriptivo.
* Listas estructuradas con los datos solicitados listos para ser procesados o mostrados por el cliente.

## 💻 Instrucciones de Ejecución

Para probar el sistema en un entorno local, sigue estos pasos:

1. **Compilar el proyecto:**
   Asegúrate de compilar todas las clases del paquete.
   
2. **Levantar el servidor:**
   Ejecuta la clase principal del servidor (`ServidorMainRMI.java` o similar). Esta clase se encargará de levantar automáticamente el registro RMI en el puerto por defecto (1099) y publicar el servicio.

3. **Ejecutar los clientes:**
   En terminales separadas, ejecuta la clase del cliente. Puedes lanzar tantas instancias como sensores quieras simular.
Protocolo de monitorización industrial de sensores con umbrales y generación de alertas



Se deberá diseñar un protocolo de aplicación orientado a un entorno de planta industrial, donde

múltiples clientes simulen sensores que envían periódicamente mediciones a un servidor central.

Cada sensor deberá estar identificado de forma unívoca según el protocolo y deberá asociarse a

un conjunto de variables industriales (por ejemplo, temperatura, vibración, caudal, presión) con

unidades y rangos operativos definidos.



El servidor será responsable de recibir y almacenar las mediciones, mantener el estado más

reciente de cada sensor y analizar los valores en función de umbrales configurables. Cuando una

medición supere un umbral o viole una regla definida (por ejemplo, fuera de rango, cambio

brusco, ausencia de lecturas), el servidor deberá generar una alerta estructurada, registrarla

internamente y permitir su consulta posterior. El protocolo deberá contemplar tanto la recepción

periódica de datos como operaciones de consulta (últimas mediciones, histórico, alertas activas

o pasadas) y, si se define, la configuración de umbrales por sensor o por tipo de variable.

El protocolo deberá especificar formalmente:



&#x09;• El formato del envío de mediciones, incluyendo identificación de sensor, variable medida,

&#x09;valor y marca temporal.

&#x09;• La confirmación o rechazo estructurado de mediciones inválidas (sensor desconocido,

&#x09;formato incorrecto, unidad no permitida, etc.).

&#x09;• El mecanismo de configuración y consulta de umbrales/reglas de alerta.

&#x09;• El formato de las alertas generadas y su correlación con sensor, variable y evento que las

&#x09;dispara.

&#x09;• La consulta de histórico de mediciones y de alertas, con criterios básicos de filtrado (por

&#x09;sensor, variable, intervalo temporal).



Toda operación deberá validarse respecto al estado interno mantenido por el servidor,

generando respuestas estructuradas. El diseño deberá incluir la definición formal de tipos de

mensaje, su sintaxis, los códigos de respuesta y error, y la máquina de estados del servidor,

contemplando además el manejo de sensores desconectados o inactivos y la consistencia del

registro histórico.



Requisitos:

&#x09;• Envío periódico automatizado de mediciones desde múltiples sensores.

&#x09;• Identificación única de sensor y soporte de múltiples variables por sensor.

&#x09;• Detección de valores fuera de rango según umbrales configurable.

&#x09;• Generación y registro interno de alertas con información completa del evento.

&#x09;• Consulta histórica de mediciones y de alertas con filtros básicos por tiempo y por sensor.

&#x09;• Manejo de sensores inactivos (por ejemplo, alerta por ausencia de lecturas)


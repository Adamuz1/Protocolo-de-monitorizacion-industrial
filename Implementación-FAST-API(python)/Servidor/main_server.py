from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import List, Optional, Dict
import time
import asyncio
import random
import datetime

app = FastAPI(title="API REST Industrial")

# --- 1. ALMACÉN DE DATOS EN MEMORIA (Equivalente a AlmacenDatos.java) ---
umbrales_generales = {
    "TEMP": [0.0, 50.0],
    "PRES": [0.5, 2.5],
    "VIBR": [0.0, 10.0],
    "HUM1": [0.0, 1.0],
    "HUM2": [0.0, 1.0],
    "HUM3": [0.0, 1.0],
}

historial_mediciones = []
historial_alertas = []
umbrales = {}  # Formato: { "idSensor": { "variable": [min, max] } }
ultima_actividad = {}

# --- 2. MODELOS DE DATOS (Equivalente a Medicion, Alerta y los parámetros de los métodos) ---
class Medicion(BaseModel):
    idSensor: str
    timestamp: int
    variable: str
    valor: float
    unidad: str

class UmbralConfig(BaseModel):
    min: float
    max: float

# --- 3. ENDPOINTS (Equivalente a IServidorIndustrial y ServidorIndustrialImpl) ---

@app.post("/mediciones", status_code=200)
def enviar_medicion(medicion: Medicion):
    ultima_actividad[medicion.idSensor] = int(time.time())
    
    # 1. Guardar historial
    historial_mediciones.append({
        "idSensor": medicion.idSensor,
        "variable": medicion.variable,
        "valor": medicion.valor,
        "unidad": medicion.unidad,
        "marcaTemporal": medicion.timestamp
    })

    # 2. Lógica del tanque
    if medicion.variable == "HUM2" and medicion.valor == 0.0:
        historial_alertas.append({"idSensor": medicion.idSensor, "variable": medicion.variable, "valorDisparo": medicion.valor, "razon": "LOW_WATER", "marcaTemporal": medicion.timestamp})
    elif medicion.variable == "HUM1" and medicion.valor == 0.0:
        historial_alertas.append({"idSensor": medicion.idSensor, "variable": medicion.variable, "valorDisparo": medicion.valor, "razon": "TANK_EMPTY", "marcaTemporal": medicion.timestamp})

    # 3. Lógica de umbrales numéricos
    limites = umbrales.get(medicion.idSensor, {}).get(medicion.variable)
    if limites is None:
        limites = umbrales_generales.get(medicion.variable)

    if limites is not None and (medicion.valor < limites[0] or medicion.valor > limites[1]):
        historial_alertas.append({"idSensor": medicion.idSensor, "variable": medicion.variable, "valorDisparo": medicion.valor, "razon": "OUT_OF_RANGE", "marcaTemporal": medicion.timestamp})

    return {"mensaje": "OK"}

@app.get("/mediciones/{id_sensor}/{variable}")
def consultar_historico_mediciones(id_sensor: str, variable: str, inicio: int = 0, fin: int = 0):
    ultima_actividad[id_sensor] = int(time.time())
    tiempo_fin_real = int(time.time()) if fin == 0 else fin
    
    resultados = []
    for m in historial_mediciones:
        if m["idSensor"] == id_sensor and m["variable"] == variable and inicio <= m["marcaTemporal"] <= tiempo_fin_real:
            fecha_legible = datetime.datetime.fromtimestamp(m["marcaTemporal"]).strftime('%d/%m/%Y %H:%M:%S')
            # Devolvemos un array estructurado imitando tu RespuestaServidor
            resultados.append([fecha_legible, str(m["valor"]), m["unidad"]])
            
    if not resultados:
        raise HTTPException(status_code=404, detail="NO_ENCONTRADO")
    return {"datos": resultados}

@app.get("/alertas/{id_sensor}/{variable}")
def consultar_historico_alertas(id_sensor: str, variable: str, inicio: int = 0, fin: int = 0):
    ultima_actividad[id_sensor] = int(time.time())
    tiempo_fin_real = int(time.time()) if fin == 0 else fin
    
    resultados = []
    for a in historial_alertas:
        if a["idSensor"] == id_sensor and a["variable"] == variable and inicio <= a["marcaTemporal"] <= tiempo_fin_real:
            fecha_legible = datetime.datetime.fromtimestamp(a["marcaTemporal"]).strftime('%d/%m/%Y %H:%M:%S')
            info_variable = f"{a['variable']} ({a['valorDisparo']})"
            resultados.append([fecha_legible, info_variable, a["razon"]])
            
    if not resultados:
        raise HTTPException(status_code=404, detail="NO_ENCONTRADO")
    return {"datos": resultados}

@app.get("/umbrales/{id_sensor}/{variable}")
def consultar_umbrales(id_sensor: str, variable: str):
    ultima_actividad[id_sensor] = int(time.time())
    
    limites = umbrales.get(id_sensor, {}).get(variable)
    if limites is None:
        limites = umbrales_generales.get(variable)
        
    if limites is not None:
        return {"datos": [[str(limites[0]), str(limites[1])]]}
        
    raise HTTPException(status_code=404, detail="NO_ENCONTRADO")

@app.put("/umbrales/{id_sensor}/{variable}")
def configurar_umbrales(id_sensor: str, variable: str, config: UmbralConfig):
    ultima_actividad[id_sensor] = int(time.time())
    
    if id_sensor not in umbrales:
        umbrales[id_sensor] = {}
    umbrales[id_sensor][variable] = [config.min, config.max]
    
    return {"mensaje": "OK_CONFIGURADO"}

# --- 4. SIMULADOR EN SEGUNDO PLANO (Equivalente a SimuladorSensores.java) ---
async def simulador_sensores():
    print("[SIMULADOR] Iniciando generación automática de datos...")
    nivel_tanque = 3
    
    while True:
        await asyncio.sleep(5)
        timestamp = int(time.time())
        
        # Simulación del Tanque Multisensor
        cambio = random.randint(-1, 1)
        nivel_tanque = max(0, min(3, nivel_tanque + cambio))
        
        hum3 = 1.0 if nivel_tanque >= 3 else 0.0
        hum2 = 1.0 if nivel_tanque >= 2 else 0.0
        hum1 = 1.0 if nivel_tanque >= 1 else 0.0
        
        # Enviamos datos localmente llamando a la función del endpoint
        enviar_medicion(Medicion(idSensor="TANQUE_A", timestamp=timestamp, variable="HUM3", valor=hum3, unidad="%"))
        enviar_medicion(Medicion(idSensor="TANQUE_A", timestamp=timestamp, variable="HUM2", valor=hum2, unidad="%"))
        enviar_medicion(Medicion(idSensor="TANQUE_A", timestamp=timestamp, variable="HUM1", valor=hum1, unidad="%"))
        
        # Simulación del Motor
        temp_motor = round(40.0 + random.random() * 20.0, 2)
        enviar_medicion(Medicion(idSensor="MOTOR_01", timestamp=timestamp, variable="TEMP", valor=temp_motor, unidad="C"))

@app.on_event("startup")
async def iniciar_tareas_fondo():
    # Arranca el simulador cuando el servidor FastAPI se inicia
    asyncio.create_task(simulador_sensores())
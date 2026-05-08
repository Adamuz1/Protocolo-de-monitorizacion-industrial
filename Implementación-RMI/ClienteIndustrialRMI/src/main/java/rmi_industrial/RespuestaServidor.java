package rmi_industrial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RespuestaServidor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int codigoEstado;
    private String descripcion;
    private List<String[]> datos; 

    public RespuestaServidor(int codigoEstado, String descripcion) {
        this.codigoEstado = codigoEstado;
        this.descripcion = descripcion;
        this.datos = new ArrayList<>();
    }

    public void agregarRegistro(String[] registro) {
        this.datos.add(registro);
    }

    public int getCodigoEstado() { return codigoEstado; }
    public String getDescripcion() { return descripcion; }
    public List<String[]> getDatos() { return datos; }
    
    public boolean isExito() {
        return codigoEstado == 200;
    }
}
package main.java;

import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    private String tipo; // Tipo del nodo (por ejemplo, PROGRAMA, FUNC, etc.)
    private String valor; // Valor asociado al nodo (el texto del token)
    private List<NodoArbol> hijos; // Hijos del nodo
    private String anotacion; // Anotación adicional

    public NodoArbol(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(NodoArbol hijo) {
        this.hijos.add(hijo);
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public List<NodoArbol> getHijos() {
        return hijos;
    }

    public String getAnotacion() {
        return anotacion;
    }

    public void setAnotacion(String anotacion) {
        this.anotacion = anotacion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipo);
        if (valor != null && !valor.isEmpty()) {
            sb.append(" -> ").append(valor);
        }
        if (anotacion != null && !anotacion.isEmpty()) {
            sb.append(" [").append(anotacion).append("]");
        }
        return sb.toString();
    }
}
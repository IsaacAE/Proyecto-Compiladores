package main.java;

import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    private String tipo; // Tipo del nodo (por ejemplo, PROGRAMA, FUNC, etc.)
    private String valor; // Valor asociado al nodo (el texto del token)
    private List<NodoArbol> hijos; // Hijos del nodo

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

   // Método toString para mostrar el árbol de manera más visual
    public String toString(String indentacion) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentacion).append(tipo);
        if (valor != null && !valor.isEmpty()) {
            sb.append(" -> ").append(valor); // Mostrar el valor si existe
        }
        sb.append("\n");
        for (NodoArbol hijo : hijos) {
            sb.append(hijo.toString(indentacion + "  ")); // Recursión para los hijos con mayor indentación
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(""); // Llamar al método recursivo con indentación inicial vacía
    }
}


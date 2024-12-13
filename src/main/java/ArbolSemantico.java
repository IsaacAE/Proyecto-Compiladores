package main.java;

import java.util.List;

public class ArbolSemantico {
    private NodoArbol raiz;

    public ArbolSemantico(NodoArbol raiz) {
        this.raiz = raiz;
    }

    public NodoArbol getRaiz() {
        return raiz;
    }

    public void setRaiz(NodoArbol raiz) {
        this.raiz = raiz;
    }

    public void agregarHijo(NodoArbol padre, NodoArbol hijo) {
        if (padre != null) {
            padre.agregarHijo(hijo);
        }
    }

    public void anotarNodo(NodoArbol nodo, String anotacion) {
        if (nodo != null) {
            nodo.setAnotacion(anotacion);
        }
    }

    public NodoArbol getPadreNodoArbol(NodoArbol nodo) {
        return getPadreNodoArbol(raiz, nodo);
    }

    private NodoArbol getPadreNodoArbol(NodoArbol nodoActual, NodoArbol nodo) {
        if (nodoActual == null) {
            return null;
        }
        for (NodoArbol hijo : nodoActual.getHijos()) {
            if (hijo == nodo) {
                return nodoActual;
            }
            NodoArbol padre = getPadreNodoArbol(hijo, nodo);
            if (padre != null) {
                return padre;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildStringRepresentation(raiz, sb, 0);
        return sb.toString();
    }

    private void buildStringRepresentation(NodoArbol nodo, StringBuilder sb, int depth) {
        if (nodo == null) {
            return;
        }
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(nodo.toString()).append("\n");
        for (NodoArbol hijo : nodo.getHijos()) {
            buildStringRepresentation(hijo, sb, depth + 1);
        }
    }
}
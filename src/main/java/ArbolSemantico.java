package main.java;


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

    @Override
    public String toString() {
        if (raiz == null) {
            return "Árbol vacío";
        }
        return raiz.toString();
    }
}


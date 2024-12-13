package main.java;

public class TestArbolSemantico {
    public static void runTest() {
        // Create the root node for the program
        NodoArbol nodoPrograma = new NodoArbol("PROGRAMA", null);

        // Create the function node
        NodoArbol nodoFuncion = new NodoArbol("FUNC", "main");

        // Create variable nodes
        NodoArbol nodoVarX = new NodoArbol("VAR", "x");
        nodoVarX.setAnotacion("Tipo: int");

        NodoArbol nodoVarY = new NodoArbol("VAR", "y");
        nodoVarY.setAnotacion("Tipo: int");

        // Build the tree
        nodoFuncion.agregarHijo(nodoVarX);
        nodoFuncion.agregarHijo(nodoVarY);
        nodoPrograma.agregarHijo(nodoFuncion);

        // Create the semantic tree
        ArbolSemantico arbolSemantico = new ArbolSemantico(nodoPrograma);

        // Print the tree
        System.out.println(arbolSemantico.toString());
    }
}
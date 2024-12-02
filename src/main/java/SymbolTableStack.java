package main.java;

import java.util.Stack;

public class SymbolTableStack {
    private Stack<SymbolTable> stack = new Stack<>();

    // Añadir una nueva tabla de símbolos a la pila
    public void push(SymbolTable table) {
        stack.push(table);
    }

    // Eliminar la tabla de símbolos de la cima de la pila
    public SymbolTable pop() {
        return stack.pop();
    }

    // Ver el símbolo de la cima de la pila sin eliminarlo
    public SymbolTable peek() {
        return stack.isEmpty() ? null : stack.peek();
    }

    // Ver la base de la pila (la primera tabla agregada)
    public SymbolTable base() {
        return stack.isEmpty() ? null : stack.firstElement();
    }

    // Buscar un símbolo en la pila (primero en la cima, luego en la base)
    public SymbolTable lookup(String id) {
        if (stack.isEmpty()) {
            return null;
        }

        // Buscar en la cima de la pila
        SymbolTable topTable = stack.peek();
        if (topTable.getSymbol(id) != null) {
            return topTable;
        }

        // Buscar en la base de la pila (si es necesario)
        SymbolTable baseTable = stack.firstElement();
        if (baseTable.getSymbol(id) != null) {
            return baseTable;
        }

        // No encontrado
        return null;
    }
}


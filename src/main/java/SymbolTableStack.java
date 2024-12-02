package main.java;
import java.util.Optional;
import java.util.Stack;

public class SymbolTableStack {
    private Stack<SymbolTable> stack = new Stack<>();

    // Añadir una nueva tabla de símbolos
    public void push(SymbolTable table) {
        stack.push(table);
    }

    // Eliminar la tabla de la cima
    public SymbolTable pop() {
        return stack.pop();
    }

    // Ver la tabla de la cima
    public SymbolTable peek() {
        return stack.isEmpty() ? null : stack.peek();
    }

    // Buscar un símbolo en toda la pila
    public Symbol lookup(String id) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            SymbolTable table = stack.get(i);
            Optional<Symbol> symbol = table.getSymbol(id);
            if (symbol.isPresent()) {
                return symbol.get();
            }
        }
        return null; // No encontrado
    }
}


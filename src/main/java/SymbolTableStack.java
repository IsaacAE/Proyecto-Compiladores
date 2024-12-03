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

    public Symbol lookup(String id) {
    // Buscar en el tope de la pila (alcance local actual)
    SymbolTable tablaLocal = stack.peek(); // Obtener la tabla de símbolos en el tope de la pila
    if (tablaLocal != null) {
        Optional<Symbol> simboloLocal = tablaLocal.getSymbol(id);
        if (simboloLocal.isPresent()) {
            return simboloLocal.get(); // Si lo encuentra, lo retorna (variable local)
        }
    }

    // Buscar en el fondo de la pila (tabla de símbolos global)
    if (!stack.isEmpty()) {
        SymbolTable tablaGlobal = stack.firstElement(); // Acceder a la tabla de símbolos global (fondo de la pila)
        Optional<Symbol> simboloGlobal = tablaGlobal.getSymbol(id);
        return simboloGlobal.orElse(null); // Si lo encuentra en el fondo, lo retorna (variable global)
    }

    return null; // Si no se encuentra en ningún ámbito
}


public SymbolTable base(){
	return stack.firstElement();
}
}


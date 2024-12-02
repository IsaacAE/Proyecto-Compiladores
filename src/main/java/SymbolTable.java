package main.java;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class SymbolTable {
    private Map<String, Symbol> symbols = new HashMap<>();

    public void addSymbol(String id, Symbol symbol) {
        symbols.put(id, symbol);
    }

    public Optional<Symbol> getSymbol(String id) {
        return Optional.ofNullable(symbols.get(id));
    }

    public boolean containsSymbol(String id) {
        return symbols.containsKey(id);
    }
    
    // Nuevo método: obtener todos los símbolos
    public ArrayList<Symbol> getAllSymbols() {
        return new ArrayList<>(symbols.values());
    }
}


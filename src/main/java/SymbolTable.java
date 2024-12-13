package main.java;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Optional;

public class SymbolTable {
    private Map<String, Symbol> symbols = new HashMap<>();
    private int nextAdress = 0;
    private int nextTemp = 1000;

    public void addSymbol(String id, Symbol symbol) {
        symbols.put(id, symbol);
    }

    public Optional<Symbol> getSymbol(String id) {
        return Optional.ofNullable(symbols.get(id));
    }
    
     public Symbol getSymbolSecure(String id) {
        return symbols.get(id);
    }

    public boolean containsSymbol(String id) {
        return symbols.containsKey(id);
    }
    
    // Nuevo método: obtener todos los símbolos
    public ArrayList<Symbol> getAllSymbols() {
        return new ArrayList<>(symbols.values());
    }
    
    public Set<String> getAllIds() {
    return symbols.keySet();  // Devuelve el conjunto de claves (ids)
}

public List<Map.Entry<String, Symbol>> getSymbolsByCategory(String category) {
    List<Map.Entry<String, Symbol>> result = new ArrayList<>();

    for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
        if (category.equals(entry.getValue().getCat())) {
            result.add(entry);
        }
    }

    return result;
}

@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tablas de Simbolos:\n");
    for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
        sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
    }
    return sb.toString();
}

}


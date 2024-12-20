package main.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TypeTable {
    private final Map<Integer, Type> types = new HashMap<>();
    private int currentId = 0;

    // Obtener un tipo por su ID
    public Type getType(int id) {
        return types.get(id);
    }

    // Agregar un nuevo tipo
    public int addType(int items, int tam, Integer parent) {
        Type newType = new Type(currentId, items, tam, parent);
        types.put(currentId, newType);
        return currentId++;
    }
    
    
    // Agregar un nuevo tipo
    public int addTypeArray(int id, int items, int tam, Integer parent) {
        Type newType = new Type(id, items, tam, parent);
        types.put(id, newType);
        return id;
    }

    // Obtener el tamaño de un tipo
    public int getTam(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getTam() : 0;
    }

    // Obtener los elementos de un tipo
    public int getItems(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getItems() : 0;
    }

    // Obtener el padre de un tipo
    public Integer getParent(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getParent() : null;
    }
    
    // Agregar un nuevo tipo
    public int addTypeStruct(int id, int items, int tam, Integer parent) {
        Type newType = new Type(id, items, tam, parent);
        types.put(id, newType);
        return id;
    }



private int calculateSize(Map<String, Integer> members) {
    int size = 0;
    for (int memberType : members.values()) {
        size += getTam(memberType);
    }
    return size;
}

public boolean contains(int id) {
    return types.containsKey(id);
}


public Set<Integer> getAllIds() {
    return types.keySet();
}


public int size() {
    return types.size();
}

@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tabla de Tipos:\n");
    for (Map.Entry<Integer, Type> entry : types.entrySet()) {
        sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
    }
    return sb.toString();
}

}


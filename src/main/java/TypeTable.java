package main.java;

import java.util.HashMap;
import java.util.Map;

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
    
    public int addStructType(Map<String, Integer> members, Integer parent) {
    Type newType = new Type(currentId, members.size(), calculateSize(members), parent);
    newType.setMembers(members); // Almacena los miembros del struct
    types.put(currentId, newType);
    return currentId++;
}

private int calculateSize(Map<String, Integer> members) {
    int size = 0;
    for (int memberType : members.values()) {
        size += getTam(memberType);
    }
    return size;
}

public int size() {
    return types.size();
}

}


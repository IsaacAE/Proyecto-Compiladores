package main.java;

import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    private final Map<Integer, Type> types;  // Mapa de tipos, con un ID como clave
    private int currentId;  // ID incremental para cada tipo añadido

    // Constructor
    public TypeTable() {
        this.types = new HashMap<>();
        this.currentId = 0;
    }

    // Método para obtener el tamaño de un tipo a partir de su ID
    public int getTam(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getTam() : 0;  // Si no existe el tipo, retorna 0
    }

    // Método para obtener el número de elementos de un tipo a partir de su ID
    public int getItems(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getItems() : 0;  // Si no existe el tipo, retorna 0
    }

    // Método para obtener el nombre de un tipo a partir de su ID
    public String getName(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getName() : null;  // Si no existe el tipo, retorna null
    }

    // Método para obtener el "padre" de un tipo a partir de su ID
    public Type getParent(int id) {
        Type type = types.get(id);
        return (type != null) ? type.getParent() : null;  // Si no existe el tipo, retorna null
    }

    // Método para obtener el tipo a partir de su ID
    public Type getType(int id) {
        return types.get(id);  // Retorna el tipo, o null si no existe
    }

    // Método para agregar un nuevo tipo a la tabla y retornar su ID único
    public int addType(String name, int items, int parentId, Type parentType) {
        int id = currentId++;  // Asignar un nuevo ID incremental
        
        // Crear un nuevo tipo con los parámetros proporcionados
        Type newType = new Type(name, (short) items, (short) (items * 4), parentType);
        
        // Agregar el nuevo tipo a la tabla
        types.put(id, newType);
        
        return id;  // Retornar el ID del nuevo tipo agregado
    }
}


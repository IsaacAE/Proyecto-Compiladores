package main.java;

public class Type {
    private final int id;       // ID del tipo
    private final int items;  // Número de elementos
    private final int tam;    // Tamaño en bytes
    private final Integer parent; // ID del tipo padre (nullable)

    // Constructor
    public Type(int id, int items, int tam, Integer parent) {
        this.id = id;
        this.items = items;
        this.tam = tam;
        this.parent = parent;
    }

    // Métodos getter
    public int getId() {
        return id;
    }

    public int getItems() {
        return items;
    }

    public int getTam() {
        return tam;
    }

    public Integer getParent() {
        return parent;
    }

    // Promoción de tipos
    public static Type getPromotedType(Type type1, Type type2) {
        if (type1.canBeCastedTo(type2)) {
            return type2;
        } else if (type2.canBeCastedTo(type1)) {
            return type1;
        } else {
            throw new IllegalArgumentException("No se puede promocionar entre " + type1.id + " y " + type2.id);
        }
    }

    // Verificar si un tipo puede convertirse a otro
    public boolean canBeCastedTo(Type other) {
        return this.id == other.id || (this.parent != null && this.parent.equals(other.id));
    }
    
    
}


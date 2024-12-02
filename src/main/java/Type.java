package main.java;

public abstract class Type {
    private final String name;     // Nombre del tipo (por ejemplo, "int", "float")
    private final short items;     // Número de elementos (por ejemplo, en un array o colección)
    private final short tam;       // Tamaño del tipo (en bytes)
    private final TypeParent parent; // Tipo padre (opcional, si lo necesitas para conversiones de tipos)

    // Constructor
    public Type(String name, short items, short tam, TypeParent parent) {
        this.name = name;
        this.items = items;
        this.tam = tam;
        this.parent = parent;
    }

    // Métodos getter
    public String getName() {
        return name;
    }

    public short getItems() {
        return items;
    }

    public short getTam() {
        return tam;
    }

    public TypeParent getParent() {
        return parent;
    }

    // Método para verificar si un tipo puede ser convertido a otro
    public abstract boolean canBeCastedTo(Type other);

    // Representación en cadena del tipo (por ejemplo, "int", "float")
    @Override
    public String toString() {
        return name;
    }
}


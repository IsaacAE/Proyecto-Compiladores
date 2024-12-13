package main.java;

import java.util.Map;

public class Type {
    private final int id;       // ID del tipo
    private int items;  // Número de elementos
    private int tam;    // Tamaño en bytes
    private final Integer parent; // ID del tipo padre (nullable)
    private Map<String, Integer> members; // Miembros del struct
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
    
    public void setTam(int tam) {
    this.tam = tam;
}

public void setItems(int items) {
    this.items = items;
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
    
    // Verifica si un tipo puede ser promovido a otro
    public static boolean canPromote(Type source, Type target) {
        if (source == null || target == null) return false;

        // Ejemplo de reglas de promoción:
        if (source.getId() == 1 && (target.getId() == 2 || target.getId() == 3)) { // int -> float/double
            return true;
        }
        if (source.getId() == 2 && target.getId() == 3) { // float -> double
            return true;
        }
        if (source.getId() == target.getId()) { // Igualdad de tipos
            return true;
        }

        // No se puede promover
        return false;
    }
    
    // Métodos para manejar miembros de struct
    public void setMembers(Map<String, Integer> members) {
        this.members = members;
    }

    public Map<String, Integer> getMembers() {
        return members;
    }
    
 @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Type{");
    sb.append("id=").append(getTipoFromInt(id)); // Usar la descripción del tipo en lugar del número
    sb.append(", items=").append(items);
    sb.append(", tam=").append(tam);
    sb.append(", parent=").append(parent == null ? "null" : getTipoFromInt(parent));
    if (members != null && !members.isEmpty()) {
        sb.append(", members={");
        members.forEach((name, typeId) -> 
            sb.append(name).append(": ").append(getTipoFromInt(typeId)).append(", ")
        );
        // Eliminar la última coma y espacio
        sb.setLength(sb.length() - 2);
        sb.append("}");
    }
    sb.append("}");
    return sb.toString();
}

// Método auxiliar para convertir id a su descripción
private String getTipoFromInt(int tipo) {
    if (tipo < -1) {
        String tipoStr = String.valueOf(tipo);
        if (tipoStr.length() >= 3 && tipoStr.charAt(1) == '8') { // El índice 1 corresponde al segundo carácter
            return "ptr";
        } else {
            return "arreglo";
        }
    }

    if (tipo > 7 ) {
        return "struct";
    }
    switch (tipo) {
        case 1: return "int";
        case 2: return "float";
        case 3: return "double";
        case 4: return "string";
        case 5: return "rune";
        case 6: return "boolean";
        case 7: return "complex";
        case 0: return "void";
       default: return "desconocido";
    }
}



}


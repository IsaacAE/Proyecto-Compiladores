package main.java;

import java.util.List;

public class Symbol {
    private String id;
    private int dir;  // Dirección de la variable/función
    private int type;  // Tipo del símbolo
    private String cat;  // Categoría del símbolo
    private List<String> args;  // Lista de argumentos (como nombres o tipos)

    // Constructor
    public Symbol(int dir, int type, String cat, List<String> args) {
        this.dir = dir;
        this.type = type;
        this.cat = cat;
        this.args = args;
    }

    // Métodos getter
    public int getDir() {
        return dir;
    }

    public int getType() {
        return type;
    }

    public String getCat() {
        return cat;
    }

    public List<String> getArgs() {
        return args;
    }
    
     // Método toString
    @Override
    public String toString() {
        String tipoString = getTipoString(type); // Convertir el tipo a su representación de cadena
        String argsString = args != null ? String.join(", ", args) : "N/A"; // Unir los argumentos si existen
        
        return "Symbol{" +
               "cat='" + cat + '\'' +
               ", tipo=" + tipoString +
               ", direccion=" + dir +
               ", argumentos=" + argsString +
               '}';
    }

    // Método auxiliar para obtener el nombre del tipo basado en su ID
    private String getTipoString(int tipoId) {
        switch (tipoId) {
            case 1: return "int";
            case 2: return "float";
            case 3: return "double";
            case 4: return "string";
            case 5: return "boolean";
            case 0: return "void";
            default: return "desconocido";
        }
    }
}


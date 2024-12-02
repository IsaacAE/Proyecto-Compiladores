package main.java;

import java.util.List;

public class Symbol {
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
}


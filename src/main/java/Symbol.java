package main.java;

import java.util.List;

public class Symbol implements Symbol {
    private int dir;  // Dirección de la variable/función
    private Type type;  // Tipo del símbolo, ahora usando la clase Type
    private String cat;  // Categoría del símbolo (usando enum)
    private List<String> args;  // Argumentos (podrían ser List de tipos o nombres)

    // Constructor
    public Symbol(int dir, Type type, String cat, List<String> args) {
        this.dir = dir;
        this.type = type;
        this.cat = cat;
        this.args = args;
    }

    @Override
    public int getDir() {
        return dir;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getCat() {
        return cat;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}


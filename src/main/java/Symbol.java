package main.java;

import java.util.List;

public class Symbol {
    private int dir;  // Dirección de la variable/función
    private int type;  // Tipo del símbolo
    private String cat;  // Categoría del símbolo
    private List<String> args;  // Lista de argumentos (como nombres o tipos)
    private String value;  // Valor del símbolo (guardado como String)

    // Constructor
    public Symbol(int dir, int type, String cat, List<String> args) {
        this.dir = dir;
        this.type = type;
        this.cat = cat;
        this.args = args;
        this.value = null; // El valor puede estar vacío inicialmente
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

    public String getValue() {
        return value;
    }

    // Métodos setter para el valor
    public void setValue(Object value) {
        // Convertir el valor recibido a String
        if (value instanceof Integer) {
            this.value = String.valueOf(value);
        } else if (value instanceof Float) {
            this.value = String.valueOf(value);
        } else if (value instanceof Double) {
            this.value = String.valueOf(value);
        } else if (value instanceof String) {
            this.value = (String) value;
        } else if (value instanceof Boolean) {
            this.value = String.valueOf(value);
        } else {
            throw new IllegalArgumentException("Tipo de valor no soportado");
        }
    }

    // Método toString
    @Override
    public String toString() {
        String tipoString = getTipoString(type); // Convertir el tipo a su representación de cadena
        String argsString = args != null ? String.join(", ", args) : "N/A"; // Unir los argumentos si existen
        String valueString = (value != null) ? value : "N/A"; // Si el valor es null, mostrar "N/A"

        return "Symbol{" +
               "cat='" + cat + '\'' +
               ", tipo=" + tipoString +
               ", direccion=" + dir +
               ", argumentos=" + argsString +
               ", valor=" + valueString +
               '}';
    }

    // Método auxiliar para obtener el nombre del tipo basado en su ID
    private String getTipoString(int tipoId) {
        if (tipoId < -1) {
            String tipoIdStr = Integer.toString(tipoId); // Convertir a cadena
            char primerDigitoChar;

            // Si el número es negativo, tomar el segundo carácter (después del signo '-')
            if (tipoIdStr.charAt(0) == '-') {
                primerDigitoChar = tipoIdStr.charAt(1);
            } else {
                primerDigitoChar = tipoIdStr.charAt(0);
            }

            // Convertir el carácter de vuelta a entero
            tipoId = Character.getNumericValue(primerDigitoChar);
        }
        if (tipoId > 7) {
            return "struct";
        }
        switch (tipoId) {
            case 1: return "int";
            case 2: return "float";
            case 3: return "double";
            case 4: return "string";
            case 5: return "rune";
            case 6: return "boolean";
            case 7: return "complex";
            case 9: return "struct";
            case 0: return "void";
            default: return "desconocido";
        }
    }
}


package main.java;


// Clase auxiliar para tipo y valor
public class TipoValor {
    private int tipo;
    private String valor; // Valor almacenado como String

    public TipoValor(int tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public int getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }
}


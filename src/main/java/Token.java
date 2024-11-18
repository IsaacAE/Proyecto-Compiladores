package main.java;

public class Token {
    private ClaseLexica clase;
    private String lexema;

    // Definimos la estructura de los tokens para su uso posterior
    public Token(ClaseLexica clase, String lexema) {
        this.clase = clase;
        this.lexema = lexema;
    }

    //Modificamos toString() para que nos de la presentación requerida para los tokens en el formato <clase, lexema>
    @Override
    public String toString() {
        return "<" + this.clase + "," + this.lexema.trim() + ">";
    }
}
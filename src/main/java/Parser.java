package main.java;

import java.util.List;
import java.io.IOException;
import main.jflex.Lexer;


public class Parser {
    private Lexer lexer;
    private Token tokenActual;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        try {
            tokenActual = lexer.yylex(); // Obtener el primer token
        } catch (IOException ioe) {
            System.err.println("Failed to read next token");
        }
    }

    private void eat(ClaseLexica claseEsperada) {
        if (tokenActual.getClase() == claseEsperada) {
            try {
                tokenActual = lexer.yylex(); // Obtener el primer token
            } catch (IOException ioe) {
                System.err.println("Failed to read next token");
            }
        } else {
            error("Se esperaba: " + claseEsperada + ", pero se encontró: " + tokenActual.getClase());
        }
    }

    private void error(String mensaje) {
        throw new RuntimeException("Error de sintaxis: " + mensaje);
    }

    public void parse() {
        try {
            this.tokenActual = lexer.yylex();
        } catch (IOException ioe) {
            System.err.println("Error: No fue posible obtener el primer token de la entrada.");
            System.exit(1);
        }
        programa();
        if (this.tokenActual.getClase() == ClaseLexica.EOF) { // llegamos al EOF sin error
            System.out.println("La cadena es aceptada");
        } else {
            error("Se esperaba el final del archivo");
        }
    }

    

    // Producción principal
    private void programa() {
        decl_proto();
        decl_var();
        decl_func();
    }

    // Producción decl_proto
    private void decl_proto() {
        if (tokenActual.getClase() == ClaseLexica.PROGRAMA) {
            eat(ClaseLexica.PROTO);
            tipo();
            eat(ClaseLexica.ID);
            eat(ClaseLexica.PARENTESIS_ABRE);
            argumentos();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
            decl_proto_prima();
        }
    }

    private void decl_proto_prima() {
        if (tokenActual.getClase() == ClaseLexica.PROTO) {
            decl_proto();
        }
    }

    // Producción decl_var
    private void decl_var() {
        if (esTipo(tokenActual.getClase())) {
            tipo();
            lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);
            decl_var_prima();
        }
    }

    private void decl_var_prima() {
        if (esTipo(tokenActual.getClase())) {
            decl_var();
        }
    }

    // Producción decl_func
    private void decl_func() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            eat(ClaseLexica.FUNC);
            tipo();
            eat(ClaseLexica.ID);
            eat(ClaseLexica.PARENTESIS_ABRE);
            argumentos();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            bloque();
            decl_func_prima();
        }
    }

    private void decl_func_prima() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            decl_func();
        }
    }

    // Producción tipo
    private void tipo() {
        if (esTipoBasico(tokenActual.getClase())) {
            basico();
            tipo_prima();
        } else if (tokenActual.getClase() == ClaseLexica.STRUCT) {
            eat(ClaseLexica.STRUCT);
            eat(ClaseLexica.LLAVE_ABRE);
            decl_var();
            eat(ClaseLexica.LLAVE_CIERRA);
        } else if (tokenActual.getClase() == ClaseLexica.PTR) {
            puntero();
        } else {
            error("Se esperaba un tipo.");
        }
    }

    private void tipo_prima() {
        if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
            compuesto();
        }
    }

    private void puntero() {
        eat(ClaseLexica.PTR);
        basico();
    }

    private void basico() {
        if (tokenActual.getClase() == ClaseLexica.INT || 
            tokenActual.getClase() == ClaseLexica.FLOAT || 
            tokenActual.getClase() == ClaseLexica.DOUBLE || 
            tokenActual.getClase() == ClaseLexica.COMPLEX || 
            tokenActual.getClase() == ClaseLexica.RUNE || 
            tokenActual.getClase() == ClaseLexica.VOID || 
            tokenActual.getClase() == ClaseLexica.STRING) {
            eat(tokenActual.getClase());
        } else {
            error("Se esperaba un tipo básico.");
        }
    }


    private void compuesto() {
        eat(ClaseLexica.CORCHETE_ABRE);
        eat(ClaseLexica.LITERAL_ENTERA);
        eat(ClaseLexica.CORCHETE_CIERRA);
        compuesto_prima();
    }

    private void compuesto_prima() {
        while (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
            eat(ClaseLexica.CORCHETE_ABRE);
            eat(ClaseLexica.LITERAL_ENTERA);
            eat(ClaseLexica.CORCHETE_CIERRA);
        }
    }

    // Producción lista_var
    private void lista_var() {
        eat(ClaseLexica.ID);
        lista_var_prima();
    }

    private void lista_var_prima() {
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            eat(ClaseLexica.ID);
        }
    }

    // Producción argumentos
    private void argumentos() {
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            lista_args();
        }
    }

    private void lista_args() {
        tipo();
        eat(ClaseLexica.ID);
        lista_args_prima();
    }

    private void lista_args_prima() {
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            tipo();
            eat(ClaseLexica.ID);
        }
    }

    // Producción bloque
    private void bloque() {
        eat(ClaseLexica.LLAVE_ABRE);
        declaraciones();
        instrucciones();
        eat(ClaseLexica.LLAVE_CIERRA);
    }

    private void declaraciones() {
        while (esTipo(tokenActual.getClase())) {
            decl_var();
        }
    }

    private void instrucciones() {
        while (esInicioSentencia()) {
            sentencia();
        }
    }

    // Producción sentencia
    private void sentencia() {
        if (tokenActual.getClase() == ClaseLexica.ID) {
            eat(ClaseLexica.ID);
            if (tokenActual.getClase() == ClaseLexica.IGUAL) {
                eat(ClaseLexica.IGUAL);
                exp();
                eat(ClaseLexica.PUNTO_Y_COMA);
            }
        } else if (tokenActual.getClase() == ClaseLexica.IF) {
            eat(ClaseLexica.IF);
            eat(ClaseLexica.PARENTESIS_ABRE);
            exp();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            sentencia();
            elseif();
        } else if (tokenActual.getClase() == ClaseLexica.WHILE) {
            eat(ClaseLexica.WHILE);
            eat(ClaseLexica.PARENTESIS_ABRE);
            exp();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            sentencia();
        } else if (tokenActual.getClase() == ClaseLexica.DO) {
            eat(ClaseLexica.DO);
            sentencia();
            eat(ClaseLexica.WHILE);
            eat(ClaseLexica.PARENTESIS_ABRE);
            exp();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
        } else if (tokenActual.getClase() == ClaseLexica.BREAK) {
            eat(ClaseLexica.BREAK);
            eat(ClaseLexica.PUNTO_Y_COMA);
        } else if (tokenActual.getClase() == ClaseLexica.RETURN) {
            eat(ClaseLexica.RETURN);
            if (esInicioExpresion()) {
                exp();
            }
            eat(ClaseLexica.PUNTO_Y_COMA);
        } else if (tokenActual.getClase() == ClaseLexica.LLAVE_ABRE) {
            bloque();
        } else {
            error("Inicio no válido de una sentencia.");
        }
    }

    private void elseif() {
        if (tokenActual.getClase() == ClaseLexica.ELSE) {
            eat(ClaseLexica.ELSE);
            sentencia();
        }
    }

    // Producción exp
    private void exp() {
        exp_or();
    }

    private void exp_or() {
        exp_and();
        while (tokenActual.getClase() == ClaseLexica.OR) {
            eat(ClaseLexica.OR);
            exp_and();
        }
    }

    private void exp_and() {
        exp_eq();
        while (tokenActual.getClase() == ClaseLexica.AND) {
            eat(ClaseLexica.AND);
            exp_eq();
        }
    }

    private void exp_eq() {
        exp_rel();
        while (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
            eat(tokenActual.getClase());
            exp_rel();
        }
    }

    private void exp_rel() {
        exp_add();
        while (esOperadorRelacional(tokenActual.getClase())) {
            eat(tokenActual.getClase());
            exp_add();
        }
    }

    private void exp_add() {
        exp_mul();
        while (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
            eat(tokenActual.getClase());
            exp_mul();
        }
    }

    private void exp_mul() {
        exp_unary();
        while (esOperadorMultiplicativo(tokenActual.getClase())) {
            eat(tokenActual.getClase());
            exp_unary();
        }
    }

    private void exp_unary() {
        if (tokenActual.getClase() == ClaseLexica.NOT || tokenActual.getClase() == ClaseLexica.MENOS) {
            eat(tokenActual.getClase());
            exp_unary();
        } else {
            primary();
        }
    }

    private void primary() {
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            eat(ClaseLexica.PARENTESIS_ABRE);
            exp();
            eat(ClaseLexica.PARENTESIS_CIERRA);
        } else if (tokenActual.getClase() == ClaseLexica.ID) {
            eat(ClaseLexica.ID);
            if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
                llamada();
            }
        } else if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA || 
                   tokenActual.getClase() == ClaseLexica.LITERAL_FLOTANTE || 
                   tokenActual.getClase() == ClaseLexica.LITERAL_CADENA || 
                   tokenActual.getClase() == ClaseLexica.TRUE || 
                   tokenActual.getClase() == ClaseLexica.FALSE) {
            eat(tokenActual.getClase());
        } else {
            error("Expresión no válida.");
        }
    }


    private void llamada() {
        eat(ClaseLexica.PARENTESIS_ABRE);
        if (esInicioExpresion()) {
            lista_exp();
        }
        eat(ClaseLexica.PARENTESIS_CIERRA);
    }

    private void lista_exp() {
        exp();
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            exp();
        }
    }

    // Métodos auxiliares
    private boolean esTipo(ClaseLexica clase) {
        return esTipoBasico(clase) || clase == ClaseLexica.STRUCT || clase == ClaseLexica.PTR;
    }

    private boolean esTipoBasico(ClaseLexica clase) {
        return clase == ClaseLexica.INT ||
               clase == ClaseLexica.FLOAT ||
               clase == ClaseLexica.DOUBLE ||
               clase == ClaseLexica.COMPLEX ||
               clase == ClaseLexica.RUNE ||
               clase == ClaseLexica.VOID ||
               clase == ClaseLexica.STRING;
    }

    private boolean esInicioSentencia() {
        return tokenActual.getClase() == ClaseLexica.ID || 
               tokenActual.getClase() == ClaseLexica.IF || 
               tokenActual.getClase() == ClaseLexica.WHILE || 
               tokenActual.getClase() == ClaseLexica.DO || 
               tokenActual.getClase() == ClaseLexica.BREAK || 
               tokenActual.getClase() == ClaseLexica.RETURN || 
               tokenActual.getClase() == ClaseLexica.LLAVE_ABRE;
    }

    private boolean esInicioExpresion() {
        return tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE || 
               tokenActual.getClase() == ClaseLexica.ID || 
               tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA || 
               tokenActual.getClase() == ClaseLexica.LITERAL_FLOTANTE || 
               tokenActual.getClase() == ClaseLexica.LITERAL_CADENA || 
               tokenActual.getClase() == ClaseLexica.TRUE || 
               tokenActual.getClase() == ClaseLexica.FALSE;
    }

    private boolean esOperadorRelacional(ClaseLexica clase) {
        if (clase == ClaseLexica.MENOR || 
            clase == ClaseLexica.MENOR_IGUAL || 
            clase == ClaseLexica.MAYOR || 
            clase == ClaseLexica.MAYOR_IGUAL) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean esOperadorMultiplicativo(ClaseLexica clase) {
        if (clase == ClaseLexica.MULTIPLICACION || 
            clase == ClaseLexica.DIVISION || 
            clase == ClaseLexica.MODULO) {
            return true;
        } else {
            return false;
        }
    }
}






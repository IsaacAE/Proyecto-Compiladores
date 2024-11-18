package main.java;

import java.util.List;

public class Parser {
    private Lexer lexer;
    private Token tokenActual;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        tokenActual = lexer.yylex(); // Obtener el primer token
    }

    private void eat(ClaseLexica claseEsperada) {
        if (tokenActual.clase == claseEsperada) {
            tokenActual = lexer.yylex(); // Consumir token
        } else {
            error("Se esperaba: " + claseEsperada + ", pero se encontró: " + tokenActual.clase);
        }
    }

    private void error(String mensaje) {
        throw new RuntimeException("Error de sintaxis: " + mensaje);
    }

    public void parse() {
        programa();
        if (tokenActual.clase != null) { // null representa EOF
            error("Tokens extra después del programa.");
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
        if (tokenActual.clase == ClaseLexica.PROTO) {
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
        if (tokenActual.clase == ClaseLexica.PROTO) {
            decl_proto();
        }
    }

    // Producción decl_var
    private void decl_var() {
        if (esTipo(tokenActual.clase)) {
            tipo();
            lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);
            decl_var_prima();
        }
    }

    private void decl_var_prima() {
        if (esTipo(tokenActual.clase)) {
            decl_var();
        }
    }

    // Producción decl_func
    private void decl_func() {
        if (tokenActual.clase == ClaseLexica.FUNC) {
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
        if (tokenActual.clase == ClaseLexica.FUNC) {
            decl_func();
        }
    }

    // Producción tipo
    private void tipo() {
        if (esTipoBasico(tokenActual.clase)) {
            basico();
            tipo_prima();
        } else if (tokenActual.clase == ClaseLexica.STRUCT) {
            eat(ClaseLexica.STRUCT);
            eat(ClaseLexica.LLAVE_ABRE);
            decl_var();
            eat(ClaseLexica.LLAVE_CIERRA);
        } else if (tokenActual.clase == ClaseLexica.PTR) {
            puntero();
        } else {
            error("Se esperaba un tipo.");
        }
    }

    private void tipo_prima() {
        if (tokenActual.clase == ClaseLexica.CORCHETE_ABRE) {
            compuesto();
        }
    }

    private void puntero() {
        eat(ClaseLexica.PTR);
        basico();
    }

    private void basico() {
        switch (tokenActual.clase) {
            case INT, FLOAT, DOUBLE, COMPLEX, RUNE, VOID, STRING -> eat(tokenActual.clase);
            default -> error("Se esperaba un tipo básico.");
        }
    }

    private void compuesto() {
        eat(ClaseLexica.CORCHETE_ABRE);
        eat(ClaseLexica.LITERAL_ENTERA);
        eat(ClaseLexica.CORCHETE_CIERRA);
        compuesto_prima();
    }

    private void compuesto_prima() {
        while (tokenActual.clase == ClaseLexica.CORCHETE_ABRE) {
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
        while (tokenActual.clase == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            eat(ClaseLexica.ID);
        }
    }

    // Producción argumentos
    private void argumentos() {
        if (esTipo(tokenActual.clase)) {
            lista_args();
        }
    }

    private void lista_args() {
        tipo();
        eat(ClaseLexica.ID);
        lista_args_prima();
    }

    private void lista_args_prima() {
        while (tokenActual.clase == ClaseLexica.COMA) {
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
        while (esTipo(tokenActual.clase)) {
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
        switch (tokenActual.clase) {
            case ID -> {
                eat(ClaseLexica.ID);
                if (tokenActual.clase == ClaseLexica.IGUAL) {
                    eat(ClaseLexica.IGUAL);
                    exp();
                    eat(ClaseLexica.PUNTO_Y_COMA);
                }
            }
            case IF -> {
                eat(ClaseLexica.IF);
                eat(ClaseLexica.PARENTESIS_ABRE);
                exp();
                eat(ClaseLexica.PARENTESIS_CIERRA);
                sentencia();
                elseif();
            }
            case WHILE -> {
                eat(ClaseLexica.WHILE);
                eat(ClaseLexica.PARENTESIS_ABRE);
                exp();
                eat(ClaseLexica.PARENTESIS_CIERRA);
                sentencia();
            }
            case DO -> {
                eat(ClaseLexica.DO);
                sentencia();
                eat(ClaseLexica.WHILE);
                eat(ClaseLexica.PARENTESIS_ABRE);
                exp();
                eat(ClaseLexica.PARENTESIS_CIERRA);
                eat(ClaseLexica.PUNTO_Y_COMA);
            }
            case BREAK -> {
                eat(ClaseLexica.BREAK);
                eat(ClaseLexica.PUNTO_Y_COMA);
            }
            case RETURN -> {
                eat(ClaseLexica.RETURN);
                if (esInicioExpresion()) {
                    exp();
                }
                eat(ClaseLexica.PUNTO_Y_COMA);
            }
            case LLAVE_ABRE -> bloque();
            default -> error("Inicio no válido de una sentencia.");
        }
    }

    private void elseif() {
        if (tokenActual.clase == ClaseLexica.ELSE) {
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
        while (tokenActual.clase == ClaseLexica.OR) {
            eat(ClaseLexica.OR);
            exp_and();
        }
    }

    private void exp_and() {
        exp_eq();
        while (tokenActual.clase == ClaseLexica.AND) {
            eat(ClaseLexica.AND);
            exp_eq();
        }
    }

    private void exp_eq() {
        exp_rel();
        while (tokenActual.clase == ClaseLexica.IGUAL_IGUAL || tokenActual.clase == ClaseLexica.DIFERENTE) {
            eat(tokenActual.clase);
            exp_rel();
        }
    }

    private void exp_rel() {
        exp_add();
        while (esOperadorRelacional(tokenActual.clase)) {
            eat(tokenActual.clase);
            exp_add();
        }
    }

    private void exp_add() {
        exp_mul();
        while (tokenActual.clase == ClaseLexica.MAS || tokenActual.clase == ClaseLexica.MENOS) {
            eat(tokenActual.clase);
            exp_mul();
        }
    }

    private void exp_mul() {
        exp_unary();
        while (esOperadorMultiplicativo(tokenActual.clase)) {
            eat(tokenActual.clase);
            exp_unary();
        }
    }

    private void exp_unary() {
        if (tokenActual.clase == ClaseLexica.NOT || tokenActual.clase == ClaseLexica.MENOS) {
            eat(tokenActual.clase);
            exp_unary();
        } else {
            primary();
        }
    }

    private void primary() {
        switch (tokenActual.clase) {
            case PARENTESIS_ABRE -> {
                eat(ClaseLexica.PARENTESIS_ABRE);
                exp();
                eat(ClaseLexica.PARENTESIS_CIERRA);
            }
            case ID -> {
                eat(ClaseLexica.ID);
                if (tokenActual.clase == ClaseLexica.PARENTESIS_ABRE) {
                    llamada();
                }
            }
            case LITERAL_ENTERA, LITERAL_FLOTANTE, LITERAL_STRING, TRUE, FALSE -> eat(tokenActual.clase);
            default -> error("Expresión no válida.");
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
        while (tokenActual.clase == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            exp();
        }
    }

    // Métodos auxiliares
    private boolean esTipo(ClaseLexica clase) {
        return esTipoBasico(clase) || clase == ClaseLexica.STRUCT || clase == ClaseLexica.PTR;
    }

    private boolean esTipoBasico(ClaseLexica clase) {
        return switch (clase) {
            case INT, FLOAT, DOUBLE, COMPLEX, RUNE, VOID, STRING -> true;
            default -> false;
        };
    }

    private boolean esInicioSentencia() {
        return switch (tokenActual.clase) {
            case ID, IF, WHILE, DO, BREAK, RETURN, LLAVE_ABRE -> true;
            default -> false;
        };
    }

    private boolean esInicioExpresion() {
        return switch (tokenActual.clase) {
            case PARENTESIS_ABRE, ID, LITERAL_ENTERA, LITERAL_FLOTANTE, LITERAL_STRING, TRUE, FALSE -> true;
            default -> false;
        };
    }

    private boolean esOperadorRelacional(ClaseLexica clase) {
        return switch (clase) {
            case MENOR, MENOR_IGUAL, MAYOR, MAYOR_IGUAL -> true;
            default -> false;
        };
    }

    private boolean esOperadorMultiplicativo(ClaseLexica clase) {
        return switch (clase) {
            case MULTIPLICACION, DIVISION, MODULO -> true;
            default -> false;
        };
    }
}


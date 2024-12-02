package main.java;

import main.jflex.Lexer;
import java.io.IOException;

public class Parser {
    private Lexer lexer;
    private Token tokenActual;
    private ArbolSemantico arbol; // Árbol de análisis semántico

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.arbol = new ArbolSemantico(new NodoArbol("PROGRAMA", "")); // Inicializar árbol
    }

    private void eat(ClaseLexica claseEsperada) {
        if (tokenActual.getClase() == claseEsperada) {
            try {
                tokenActual = lexer.yylex(); // Obtener el siguiente token
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

    public ArbolSemantico parse() {
        try {
            this.tokenActual = lexer.yylex();
        } catch (IOException ioe) {
            System.err.println("Error: No fue posible obtener el primer token de la entrada.");
            System.exit(1);
        }
        NodoArbol nodoPrograma = programa();
        arbol.setRaiz(nodoPrograma); // Asignar la raíz construida al árbol
        if (this.tokenActual.getClase() == ClaseLexica.EOF) { // Llegamos al EOF sin error
            System.out.println("La cadena es aceptada");
            
        } else {
            error("Se esperaba el final del archivo");
        }

        return arbol;
    }

    // Producción principal
    private NodoArbol programa() {
        NodoArbol nodoPrograma = new NodoArbol("PROGRAMA", "");

        NodoArbol nodoDeclProto = decl_proto();
        if (nodoDeclProto != null) nodoPrograma.agregarHijo(nodoDeclProto);

        NodoArbol nodoDeclVar = decl_var();
        if (nodoDeclVar != null) nodoPrograma.agregarHijo(nodoDeclVar);

        NodoArbol nodoDeclFunc = decl_func();
        if (nodoDeclFunc != null) nodoPrograma.agregarHijo(nodoDeclFunc);

        return nodoPrograma;
    }

    // Producción decl_proto
    private NodoArbol decl_proto() {
        if (tokenActual.getClase() == ClaseLexica.PROTO) {
            NodoArbol nodoProto = new NodoArbol("PROTO", tokenActual.getLexema());
            eat(ClaseLexica.PROTO);

            NodoArbol nodoTipo = tipo();
            nodoProto.agregarHijo(nodoTipo);

            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoProto.agregarHijo(nodoID);

            eat(ClaseLexica.PARENTESIS_ABRE);
            NodoArbol nodoArgs = argumentos();
            if (nodoArgs != null) nodoProto.agregarHijo(nodoArgs);
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);

            NodoArbol nodoRecursivo = decl_proto_prima();
            if (nodoRecursivo != null) nodoProto.agregarHijo(nodoRecursivo);

            return nodoProto;
        }
        return null;
    }

    private NodoArbol decl_proto_prima() {
        if (tokenActual.getClase() == ClaseLexica.PROTO) {
            return decl_proto();
        }
        return null;
    }

    // Producción decl_var
    private NodoArbol decl_var() {
        if (esTipo(tokenActual.getClase())) {
            NodoArbol nodoVar = new NodoArbol("DECL_VAR", "");

            NodoArbol nodoTipo = tipo();
            nodoVar.agregarHijo(nodoTipo);

            NodoArbol nodoListaVar = lista_var();
            nodoVar.agregarHijo(nodoListaVar);

            eat(ClaseLexica.PUNTO_Y_COMA);

            NodoArbol nodoRecursivo = decl_var_prima();
            if (nodoRecursivo != null) nodoVar.agregarHijo(nodoRecursivo);

            return nodoVar;
        }
        return null;
    }

    private NodoArbol decl_var_prima() {
        if (esTipo(tokenActual.getClase())) {
            return decl_var();
        }
        return null;
    }

    // Producción decl_func
    private NodoArbol decl_func() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            NodoArbol nodoFunc = new NodoArbol("FUNC", tokenActual.getLexema());
            eat(ClaseLexica.FUNC);

            NodoArbol nodoTipo = tipo();
            nodoFunc.agregarHijo(nodoTipo);

            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoFunc.agregarHijo(nodoID);

            eat(ClaseLexica.PARENTESIS_ABRE);
            NodoArbol nodoArgs = argumentos();
            if (nodoArgs != null) nodoFunc.agregarHijo(nodoArgs);
            eat(ClaseLexica.PARENTESIS_CIERRA);

            NodoArbol nodoBloque = bloque();
            nodoFunc.agregarHijo(nodoBloque);

            NodoArbol nodoRecursivo = decl_func_prima();
            if (nodoRecursivo != null) nodoFunc.agregarHijo(nodoRecursivo);

            return nodoFunc;
        }
        return null;
    }

    private NodoArbol decl_func_prima() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            return decl_func();
        }
        return null;
    }

    // Producción tipo
    private NodoArbol tipo() {
        if (esTipoBasico(tokenActual.getClase())) {
            NodoArbol nodoTipo = new NodoArbol("TIPO_BASICO", tokenActual.getLexema());
            eat(tokenActual.getClase());
            return nodoTipo;
        } else if (tokenActual.getClase() == ClaseLexica.STRUCT) {
            NodoArbol nodoStruct = new NodoArbol("STRUCT", "");
            eat(ClaseLexica.STRUCT);
            eat(ClaseLexica.LLAVE_ABRE);
            NodoArbol nodoDeclVar = decl_var();
            if (nodoDeclVar != null) nodoStruct.agregarHijo(nodoDeclVar);
            eat(ClaseLexica.LLAVE_CIERRA);
            return nodoStruct;
        } else if (tokenActual.getClase() == ClaseLexica.PTR) {
            NodoArbol nodoPtr = puntero();
            return nodoPtr;
        } else {
            error("Se esperaba un tipo.");
            return null; // Unreachable
        }
    }

    private NodoArbol puntero() {
        NodoArbol nodoPtr = new NodoArbol("PTR", tokenActual.getLexema());
        eat(ClaseLexica.PTR);
        NodoArbol nodoBasico = tipo();
        nodoPtr.agregarHijo(nodoBasico);
        return nodoPtr;
    }

    private NodoArbol lista_var() {
        NodoArbol nodoListaVar = new NodoArbol("LISTA_VAR", "");
        NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
        eat(ClaseLexica.ID);
        nodoListaVar.agregarHijo(nodoID);
        NodoArbol nodoListaVarPrima = lista_var_prima();
        if (nodoListaVarPrima != null) nodoListaVar.agregarHijo(nodoListaVarPrima);
        return nodoListaVar;
    }

    private NodoArbol lista_var_prima() {
        NodoArbol nodoListaVarPrima = new NodoArbol("LISTA_VAR_PRIMA", "");
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoListaVarPrima.agregarHijo(nodoID);
        }
        return nodoListaVarPrima.getHijos().isEmpty() ? null : nodoListaVarPrima;
    }

    private NodoArbol argumentos() {
        NodoArbol nodoArgumentos = new NodoArbol("ARGUMENTOS", "");
        if (esTipo(tokenActual.getClase())) {
            NodoArbol nodoListaArgs = lista_args();
            nodoArgumentos.agregarHijo(nodoListaArgs);
        }
        return nodoArgumentos.getHijos().isEmpty() ? null : nodoArgumentos;
    }

    private NodoArbol lista_args() {
        NodoArbol nodoListaArgs = new NodoArbol("LISTA_ARGS", "");
        NodoArbol nodoTipo = tipo();
        nodoListaArgs.agregarHijo(nodoTipo);

        NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
        eat(ClaseLexica.ID);
        nodoListaArgs.agregarHijo(nodoID);

        NodoArbol nodoListaArgsPrima = lista_args_prima();
        if (nodoListaArgsPrima != null) nodoListaArgs.agregarHijo(nodoListaArgsPrima);

        return nodoListaArgs;
    }

    private NodoArbol lista_args_prima() {
        NodoArbol nodoListaArgsPrima = new NodoArbol("LISTA_ARGS_PRIMA", "");
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);

            NodoArbol nodoTipo = tipo();
            nodoListaArgsPrima.agregarHijo(nodoTipo);

            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoListaArgsPrima.agregarHijo(nodoID);
        }
        return nodoListaArgsPrima.getHijos().isEmpty() ? null : nodoListaArgsPrima;
    }

    private NodoArbol bloque() {
        NodoArbol nodoBloque = new NodoArbol("BLOQUE", "");
        eat(ClaseLexica.LLAVE_ABRE);

        NodoArbol nodoDeclaraciones = declaraciones();
        if (nodoDeclaraciones != null) nodoBloque.agregarHijo(nodoDeclaraciones);

        NodoArbol nodoInstrucciones = instrucciones();
        if (nodoInstrucciones != null) nodoBloque.agregarHijo(nodoInstrucciones);

        eat(ClaseLexica.LLAVE_CIERRA);
        return nodoBloque;
    }

    private NodoArbol declaraciones() {
        NodoArbol nodoDeclaraciones = new NodoArbol("DECLARACIONES", "");
        while (esTipo(tokenActual.getClase())) {
            NodoArbol nodoDeclVar = decl_var();
            if (nodoDeclVar != null) nodoDeclaraciones.agregarHijo(nodoDeclVar);
        }
        return nodoDeclaraciones.getHijos().isEmpty() ? null : nodoDeclaraciones;
    }

    private NodoArbol instrucciones() {
        NodoArbol nodoInstrucciones = new NodoArbol("INSTRUCCIONES", "");
        while (esInicioSentencia()) {
            NodoArbol nodoSentencia = sentencia();
            if (nodoSentencia != null) nodoInstrucciones.agregarHijo(nodoSentencia);
        }
        return nodoInstrucciones.getHijos().isEmpty() ? null : nodoInstrucciones;
    }

    private NodoArbol sentencia() {
    NodoArbol nodoSentencia = new NodoArbol("SENTENCIA", "");
    System.out.println("Entrando a sentencia");
    
    if (tokenActual.getClase() == ClaseLexica.ID) {
        NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
        eat(ClaseLexica.ID);
        nodoSentencia.agregarHijo(nodoID);

        if (tokenActual.getClase() == ClaseLexica.ASIGNACION) {
            NodoArbol nodoAsignacion = new NodoArbol("ASIGNACION", "=");
            eat(ClaseLexica.ASIGNACION);
            NodoArbol nodoExp = exp();
            nodoAsignacion.agregarHijo(nodoID);
            nodoAsignacion.agregarHijo(nodoExp);
            nodoSentencia.agregarHijo(nodoAsignacion);
            eat(ClaseLexica.PUNTO_Y_COMA);
        } else {
            error("Se esperaba '=' después del identificador.");
        }
    } else if (tokenActual.getClase() == ClaseLexica.IF) {
        eat(ClaseLexica.IF);
        NodoArbol nodoIf = new NodoArbol("IF", "");
        eat(ClaseLexica.PARENTESIS_ABRE);
        NodoArbol nodoCondicion = exp();
        nodoIf.agregarHijo(nodoCondicion);
        eat(ClaseLexica.PARENTESIS_CIERRA);

        NodoArbol nodoCuerpoIf = sentencia();
        nodoIf.agregarHijo(new NodoArbol("CUERPO_IF", ""));
        nodoIf.agregarHijo(nodoCuerpoIf);

        if (tokenActual.getClase() == ClaseLexica.ELSE) {
            eat(ClaseLexica.ELSE);
            NodoArbol nodoCuerpoElse = sentencia();
            nodoIf.agregarHijo(new NodoArbol("CUERPO_ELSE", ""));
            nodoIf.agregarHijo(nodoCuerpoElse);
        }
        nodoSentencia.agregarHijo(nodoIf);
    } else if (tokenActual.getClase() == ClaseLexica.WHILE) {
        eat(ClaseLexica.WHILE);
        NodoArbol nodoWhile = new NodoArbol("WHILE", "");
        eat(ClaseLexica.PARENTESIS_ABRE);
        NodoArbol nodoCondicion = exp();
        nodoWhile.agregarHijo(nodoCondicion);
        eat(ClaseLexica.PARENTESIS_CIERRA);

        NodoArbol nodoCuerpoWhile = sentencia();
        nodoWhile.agregarHijo(nodoCuerpoWhile);
        nodoSentencia.agregarHijo(nodoWhile);
    } else if (tokenActual.getClase() == ClaseLexica.DO) {
        eat(ClaseLexica.DO);
        NodoArbol nodoDoWhile = new NodoArbol("DO_WHILE", "");
        NodoArbol nodoCuerpoDo = sentencia();
        nodoDoWhile.agregarHijo(nodoCuerpoDo);
        eat(ClaseLexica.WHILE);
        eat(ClaseLexica.PARENTESIS_ABRE);
        NodoArbol nodoCondicion = exp();
        nodoDoWhile.agregarHijo(nodoCondicion);
        eat(ClaseLexica.PARENTESIS_CIERRA);
        eat(ClaseLexica.PUNTO_Y_COMA);
        nodoSentencia.agregarHijo(nodoDoWhile);
    } else if (tokenActual.getClase() == ClaseLexica.BREAK) {
        eat(ClaseLexica.BREAK);
        nodoSentencia.agregarHijo(new NodoArbol("BREAK", "break"));
        eat(ClaseLexica.PUNTO_Y_COMA);
    } else if (tokenActual.getClase() == ClaseLexica.RETURN) {
        eat(ClaseLexica.RETURN);
        NodoArbol nodoReturn = new NodoArbol("RETURN", "return");
        if (esInicioExpresion()) {
            NodoArbol nodoExp = exp();
            nodoReturn.agregarHijo(nodoExp);
        }
        eat(ClaseLexica.PUNTO_Y_COMA);
        nodoSentencia.agregarHijo(nodoReturn);
    } else if (tokenActual.getClase() == ClaseLexica.SWITCH) {
        eat(ClaseLexica.SWITCH);
        NodoArbol nodoSwitch = new NodoArbol("SWITCH", "");
        eat(ClaseLexica.PARENTESIS_ABRE);
        NodoArbol nodoExp = exp();
        nodoSwitch.agregarHijo(nodoExp);
        eat(ClaseLexica.PARENTESIS_CIERRA);
        eat(ClaseLexica.LLAVE_ABRE);
        NodoArbol nodoCasos = casos();
        nodoSwitch.agregarHijo(nodoCasos);
        eat(ClaseLexica.LLAVE_CIERRA);
        nodoSentencia.agregarHijo(nodoSwitch);
    } else if (tokenActual.getClase() == ClaseLexica.PRINT) {
        eat(ClaseLexica.PRINT);
        NodoArbol nodoPrint = new NodoArbol("PRINT", "print");
        NodoArbol nodoExp = exp();
        nodoPrint.agregarHijo(nodoExp);
        eat(ClaseLexica.PUNTO_Y_COMA);
        nodoSentencia.agregarHijo(nodoPrint);
    } else if (tokenActual.getClase() == ClaseLexica.SCAN) {
        eat(ClaseLexica.SCAN);
        NodoArbol nodoScan = new NodoArbol("SCAN", "scan");
        NodoArbol nodoParteIzquierda = parteIzquierda();
        nodoScan.agregarHijo(nodoParteIzquierda);
        nodoSentencia.agregarHijo(nodoScan);
    } else if (tokenActual.getClase() == ClaseLexica.LLAVE_ABRE) {
        NodoArbol nodoBloque = bloque();
        nodoSentencia.agregarHijo(nodoBloque);
    } else {
        error("Inicio no válido de una sentencia.");
    }
    System.out.println("Saliendo de sentencia");
    return nodoSentencia;
}


    private NodoArbol exp() {
        NodoArbol nodoExp = new NodoArbol("EXP", "");
        NodoArbol nodoTermino = exp_or();
        nodoExp.agregarHijo(nodoTermino);
        return nodoExp;
    }

    private NodoArbol exp_or() {
        NodoArbol nodoExpOr = new NodoArbol("EXP_OR", "");
        NodoArbol nodoExpAnd = exp_and();
        nodoExpOr.agregarHijo(nodoExpAnd);

        while (tokenActual.getClase() == ClaseLexica.OR) {
            eat(ClaseLexica.OR);
            NodoArbol nodoExpAnd2 = exp_and();
            nodoExpOr.agregarHijo(new NodoArbol("OR", "||"));
            nodoExpOr.agregarHijo(nodoExpAnd2);
        }
        return nodoExpOr;
    }

    private NodoArbol exp_and() {
        NodoArbol nodoExpAnd = new NodoArbol("EXP_AND", "");
        NodoArbol nodoExpEq = exp_eq();
        nodoExpAnd.agregarHijo(nodoExpEq);

        while (tokenActual.getClase() == ClaseLexica.AND) {
            eat(ClaseLexica.AND);
            NodoArbol nodoExpEq2 = exp_eq();
            nodoExpAnd.agregarHijo(new NodoArbol("AND", "&&"));
            nodoExpAnd.agregarHijo(nodoExpEq2);
        }
        return nodoExpAnd;
    }

    private NodoArbol exp_eq() {
        NodoArbol nodoExpEq = new NodoArbol("EXP_EQ", "");
        NodoArbol nodoExpRel = exp_rel();
        nodoExpEq.agregarHijo(nodoExpRel);

        while (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
            String operador = tokenActual.getClase() == ClaseLexica.IGUAL ? "==" : "!=";
            eat(tokenActual.getClase());
            NodoArbol nodoExpRel2 = exp_rel();
            nodoExpEq.agregarHijo(new NodoArbol("OPERADOR_EQ", operador));
            nodoExpEq.agregarHijo(nodoExpRel2);
        }
        return nodoExpEq;
    }

    private NodoArbol exp_rel() {
        NodoArbol nodoExpRel = new NodoArbol("EXP_REL", "");
        NodoArbol nodoExpAdd = exp_add();
        nodoExpRel.agregarHijo(nodoExpAdd);

        while (esOperadorRelacional(tokenActual.getClase())) {
            String operador = tokenActual.getLexema();
            eat(tokenActual.getClase());
            NodoArbol nodoExpAdd2 = exp_add();
            nodoExpRel.agregarHijo(new NodoArbol("OPERADOR_REL", operador));
            nodoExpRel.agregarHijo(nodoExpAdd2);
        }
        return nodoExpRel;
    }

    private NodoArbol exp_add() {
        NodoArbol nodoExpAdd = new NodoArbol("EXP_ADD", "");
        NodoArbol nodoExpMul = exp_mul();
        nodoExpAdd.agregarHijo(nodoExpMul);

        while (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
            String operador = tokenActual.getClase() == ClaseLexica.MAS ? "+" : "-";
            eat(tokenActual.getClase());
            NodoArbol nodoExpMul2 = exp_mul();
            nodoExpAdd.agregarHijo(new NodoArbol("OPERADOR_ADD", operador));
            nodoExpAdd.agregarHijo(nodoExpMul2);
        }
        return nodoExpAdd;
    }

    private NodoArbol exp_mul() {
        NodoArbol nodoExpMul = new NodoArbol("EXP_MUL", "");
        NodoArbol nodoExpUnary = exp_unary();
        nodoExpMul.agregarHijo(nodoExpUnary);

        while (esOperadorMultiplicativo(tokenActual.getClase())) {
            String operador = tokenActual.getLexema();
            eat(tokenActual.getClase());
            NodoArbol nodoExpUnary2 = exp_unary();
            nodoExpMul.agregarHijo(new NodoArbol("OPERADOR_MUL", operador));
            nodoExpMul.agregarHijo(nodoExpUnary2);
        }
        return nodoExpMul;
    }

    private NodoArbol exp_unary() {
        NodoArbol nodoExpUnary = new NodoArbol("EXP_UNARY", "");
        if (tokenActual.getClase() == ClaseLexica.NOT || tokenActual.getClase() == ClaseLexica.MENOS) {
            String operador = tokenActual.getClase() == ClaseLexica.NOT ? "!" : "-";
            eat(tokenActual.getClase());
            NodoArbol nodoExpUnary2 = exp_unary();
            nodoExpUnary.agregarHijo(new NodoArbol("OPERADOR_UNARY", operador));
            nodoExpUnary.agregarHijo(nodoExpUnary2);
        } else {
            NodoArbol nodoPrimary = primary();
            nodoExpUnary.agregarHijo(nodoPrimary);
        }
        return nodoExpUnary;
    }

    private NodoArbol primary() {
        NodoArbol nodoPrimary = new NodoArbol("PRIMARY", "");
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            eat(ClaseLexica.PARENTESIS_ABRE);
            NodoArbol nodoExp = exp();
            nodoPrimary.agregarHijo(nodoExp);
            eat(ClaseLexica.PARENTESIS_CIERRA);
        } else if (tokenActual.getClase() == ClaseLexica.ID) {
            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoPrimary.agregarHijo(nodoID);

            if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
                NodoArbol nodoLlamada = llamada();
                nodoPrimary.agregarHijo(nodoLlamada);
            }
        } else if (esLiteral(tokenActual.getClase())) {
            NodoArbol nodoLiteral = new NodoArbol("LITERAL", tokenActual.getLexema());
            eat(tokenActual.getClase());
            nodoPrimary.agregarHijo(nodoLiteral);
        } else {
            error("Expresión no válida.");
        }
        return nodoPrimary;
    }

    private NodoArbol llamada() {
        NodoArbol nodoLlamada = new NodoArbol("LLAMADA", "");
        eat(ClaseLexica.PARENTESIS_ABRE);
        if (esInicioExpresion()) {
            NodoArbol nodoListaExp = lista_exp();
            nodoLlamada.agregarHijo(nodoListaExp);
        }
        eat(ClaseLexica.PARENTESIS_CIERRA);
        return nodoLlamada;
    }

    private NodoArbol lista_exp() {
        NodoArbol nodoListaExp = new NodoArbol("LISTA_EXP", "");
        NodoArbol nodoExp = exp();
        nodoListaExp.agregarHijo(nodoExp);

        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            NodoArbol nodoExp2 = exp();
            nodoListaExp.agregarHijo(nodoExp2);
        }
        return nodoListaExp;
    }

    private NodoArbol casos() {
        NodoArbol nodoCasos = new NodoArbol("CASOS", "");
        while (tokenActual.getClase() == ClaseLexica.CASE || tokenActual.getClase() == ClaseLexica.DEFAULT) {
            if (tokenActual.getClase() == ClaseLexica.CASE) {
                NodoArbol nodoCaso = caso();
                nodoCasos.agregarHijo(nodoCaso);
            } else if (tokenActual.getClase() == ClaseLexica.DEFAULT) {
                NodoArbol nodoDefault = predeterminado();
                nodoCasos.agregarHijo(nodoDefault);
            }
        }
        return nodoCasos;
    }
    
    private NodoArbol caso() {
        NodoArbol nodoCaso = new NodoArbol("CASE", "");
        eat(ClaseLexica.CASE);
    
        NodoArbol nodoOpcion = opcion();
        nodoCaso.agregarHijo(nodoOpcion);
    
        eat(ClaseLexica.DOS_PUNTOS); // ':'
    
        NodoArbol nodoInstrucciones = instrucciones();
        if (nodoInstrucciones != null) nodoCaso.agregarHijo(nodoInstrucciones);
    
        return nodoCaso;
    }
    
    private NodoArbol predeterminado() {
        NodoArbol nodoDefault = new NodoArbol("DEFAULT", "");
        eat(ClaseLexica.DEFAULT);
        eat(ClaseLexica.DOS_PUNTOS); // ':'
    
        NodoArbol nodoInstrucciones = instrucciones();
        if (nodoInstrucciones != null) nodoDefault.agregarHijo(nodoInstrucciones);
    
        return nodoDefault;
    }
    
    private NodoArbol opcion() {
        NodoArbol nodoOpcion = new NodoArbol("OPCION", "");
        if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA || tokenActual.getClase() == ClaseLexica.LITERAL_RUNA) {
            nodoOpcion.agregarHijo(new NodoArbol("LITERAL", tokenActual.getLexema()));
            eat(tokenActual.getClase());
        } else {
            error("Se esperaba un literal entero o runa como opción de `case`.");
        }
        return nodoOpcion;
    }
    
    private NodoArbol parteIzquierda() {
        NodoArbol nodoParteIzquierda = new NodoArbol("PARTE_IZQUIERDA", "");
        if (tokenActual.getClase() == ClaseLexica.ID) {
            NodoArbol nodoID = new NodoArbol("ID", tokenActual.getLexema());
            eat(ClaseLexica.ID);
            nodoParteIzquierda.agregarHijo(nodoID);
    
            // Manejar posibles accesos a estructuras (arrays, structs)
            while (tokenActual.getClase() == ClaseLexica.PUNTO || tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
                if (tokenActual.getClase() == ClaseLexica.PUNTO) {
                    eat(ClaseLexica.PUNTO);
                    NodoArbol nodoAccesoStruct = new NodoArbol("ACCESO_STRUCT", tokenActual.getLexema());
                    eat(ClaseLexica.ID);
                    nodoParteIzquierda.agregarHijo(nodoAccesoStruct);
                } else if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
                    eat(ClaseLexica.CORCHETE_ABRE);
                    NodoArbol nodoIndice = exp();
                    NodoArbol nodoArrayAccess = new NodoArbol("ACCESO_ARRAY", "");
                    nodoArrayAccess.agregarHijo(nodoIndice);
                    nodoParteIzquierda.agregarHijo(nodoArrayAccess);
                    eat(ClaseLexica.CORCHETE_CIERRA);
                }
            }
        } else {
            error("Se esperaba una parte izquierda válida.");
        }
        return nodoParteIzquierda;
    }
    
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

    private boolean esLiteral(ClaseLexica clase) {
        return clase == ClaseLexica.LITERAL_ENTERA || 
               clase == ClaseLexica.LITERAL_FLOTANTE || 
               clase == ClaseLexica.LITERAL_DOUBLE || 
               clase == ClaseLexica.LITERAL_CADENA || 
               clase == ClaseLexica.TRUE || 
               clase == ClaseLexica.FALSE;
    }

    private boolean esInicioSentencia() {
        return tokenActual.getClase() == ClaseLexica.ID || 
               tokenActual.getClase() == ClaseLexica.IF || 
               tokenActual.getClase() == ClaseLexica.WHILE || 
               tokenActual.getClase() == ClaseLexica.SWITCH || 
               tokenActual.getClase() == ClaseLexica.DO || 
               tokenActual.getClase() == ClaseLexica.BREAK || 
               tokenActual.getClase() == ClaseLexica.RETURN || 
               tokenActual.getClase() == ClaseLexica.PRINT || 
               tokenActual.getClase() == ClaseLexica.LLAVE_ABRE;
    }

    private boolean esInicioExpresion() {
        return tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE || 
               tokenActual.getClase() == ClaseLexica.ID || 
               tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA || 
               tokenActual.getClase() == ClaseLexica.LITERAL_FLOTANTE || 
               tokenActual.getClase() == ClaseLexica.LITERAL_DOUBLE || 
               tokenActual.getClase() == ClaseLexica.LITERAL_CADENA || 
               tokenActual.getClase() == ClaseLexica.TRUE || 
               tokenActual.getClase() == ClaseLexica.FALSE;
    }

    private boolean esOperadorRelacional(ClaseLexica clase) {
        return clase == ClaseLexica.MENOR || 
               clase == ClaseLexica.MENOR_IGUAL || 
               clase == ClaseLexica.MAYOR || 
               clase == ClaseLexica.MAYOR_IGUAL;
    }

    private boolean esOperadorMultiplicativo(ClaseLexica clase) {
        return clase == ClaseLexica.MULTIPLICACION || 
               clase == ClaseLexica.DIVISION || 
               clase == ClaseLexica.MODULO || 
               clase == ClaseLexica.DIVISION_ENTERA;
    }
}

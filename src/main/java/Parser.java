package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import main.jflex.Lexer;

public class Parser {
    private Lexer lexer;
    private Token tokenActual;

    private SymbolTableStack stackSymbolTable = new SymbolTableStack();
    private TypeTable typeTable = new TypeTable();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private void eat(ClaseLexica claseEsperada) {
        if (tokenActual.getClase() == claseEsperada) {
            try {
                tokenActual = lexer.yylex();
            } catch (IOException ioe) {
                System.err.println("Error al leer el siguiente token");
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

        // Inicializar la tabla global de símbolos
        stackSymbolTable.push(new SymbolTable());
        programa();

        if (this.tokenActual.getClase() == ClaseLexica.EOF) {
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
        while (tokenActual.getClase() == ClaseLexica.PROTO) {
            eat(ClaseLexica.PROTO);
            int tipo = tipo();
            String id = tokenActual.getLexema();
            eat(ClaseLexica.ID);
            eat(ClaseLexica.PARENTESIS_ABRE);
            List<Symbol> args = argumentos();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);

            // Registrar el prototipo como símbolo
            Symbol protoSymbol = new Symbol(-1, tipo, "prototipo", null);
            agregarSimbolo(id, protoSymbol);
        }
    }

    // Producción decl_var
    private void decl_var() {
        while (esTipo(tokenActual.getClase())) {
            int tipo = tipo();
            List<String> variables = lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);

            // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "variable", null);
                agregarSimbolo(var, varSymbol);
            }
        }
    }

    // Producción decl_func
    private void decl_func() {
        while (tokenActual.getClase() == ClaseLexica.FUNC) {
            eat(ClaseLexica.FUNC);
            int tipo = tipo();
            String id = tokenActual.getLexema();
            eat(ClaseLexica.ID);
            eat(ClaseLexica.PARENTESIS_ABRE);
            List<Symbol> args = argumentos();
            eat(ClaseLexica.PARENTESIS_CIERRA);

            // Registrar la función como símbolo
            Symbol funcSymbol = new Symbol(-1, tipo, "funcion", null);
            agregarSimbolo(id, funcSymbol);

            // Crear un nuevo scope
            stackSymbolTable.push(new SymbolTable());
            for (Symbol arg : args) {
                agregarSimbolo(arg.getCat(), arg);
            }

            bloque();
            stackSymbolTable.pop(); // Cerrar scope
        }
    }

    private int tipo() {
        int tipoBase;
    
        if (esTipoBasico(tokenActual.getClase())) {
            tipoBase = basico(); // Identificar el tipo básico
            tipoBase = tipo_prima(tipoBase); // Manejar tipos compuestos, si los hay
        } else if (tokenActual.getClase() == ClaseLexica.STRUCT) {
            // Procesar estructuras (structs)
            eat(ClaseLexica.STRUCT);
            eat(ClaseLexica.LLAVE_ABRE);
            decl_var(); // Manejar declaraciones dentro del struct
            eat(ClaseLexica.LLAVE_CIERRA);
            tipoBase = typeTable.addType((short) 0, (short) 0, null); // Registrar un nuevo tipo struct
        } else if (tokenActual.getClase() == ClaseLexica.PTR) {
            // Manejar punteros
            eat(ClaseLexica.PTR);
            tipoBase = -1; // Representar un puntero
        } else {
            error("Se esperaba un tipo válido.");
            tipoBase = -1; // Código inaccesible en caso de error
        }
    
        return tipoBase;
    }
    
    private int tipo_prima(int tipoBase) {
        if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
            tipoBase = compuesto(tipoBase); // Procesar la primera dimensión y las adicionales
        }
        return tipoBase; // Retorna el tipo final, ya sea básico o compuesto
    }
    

private int compuesto(int tipoBase) {
    eat(ClaseLexica.CORCHETE_ABRE);

    if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
        int dimension = Integer.parseInt(tokenActual.getLexema());
        eat(ClaseLexica.LITERAL_ENTERA);

        // Registrar la primera dimensión en la tabla de tipos
        tipoBase = typeTable.addType((short) dimension, (short) 0, tipoBase);
    } else {
        error("Se esperaba un literal entero como tamaño del arreglo.");
    }

    eat(ClaseLexica.CORCHETE_CIERRA);
    return compuesto_prima(tipoBase); // Manejar dimensiones adicionales
}

private int compuesto_prima(int tipoBase) {
    while (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        eat(ClaseLexica.CORCHETE_ABRE);

        if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
            int dimension = Integer.parseInt(tokenActual.getLexema());
            eat(ClaseLexica.LITERAL_ENTERA);

            // Registrar dimensiones adicionales en la tabla de tipos
            tipoBase = typeTable.addType((short) dimension, (short) 0, tipoBase);
        } else {
            error("Se esperaba un literal entero como tamaño del arreglo.");
        }

        eat(ClaseLexica.CORCHETE_CIERRA);
    }
    return tipoBase; // Retorna el tipo compuesto final
}


    private int basico() {
        int tipo;
        eat(tokenActual.getClase());
        switch (tokenActual.getClase()) {
            case INT: tipo = 1;
            case FLOAT: tipo = 2;
            case DOUBLE: tipo = 3;
            case STRING: tipo = 4;
            case TRUE: tipo = 5;
            case FALSE: tipo = 5;
            case VOID: tipo = 0;
            default: tipo = -1;
        }
        
        return tipo;
    }

    private void puntero() {
        eat(ClaseLexica.PTR);
        basico();
    }

    // Producción lista_var
private List<String> lista_var() {
    List<String> variables = new ArrayList<>();
    variables.add(tokenActual.getLexema()); // Registrar la primera variable
    eat(ClaseLexica.ID);
    lista_var_prima(variables);
    return variables;
}

// Producción lista_var_prima
private void lista_var_prima(List<String> variables) {
    while (tokenActual.getClase() == ClaseLexica.COMA) {
        eat(ClaseLexica.COMA);
        variables.add(tokenActual.getLexema()); // Registrar las siguientes variables
        eat(ClaseLexica.ID);
    }
}

    private List<Symbol> argumentos() {
        List<Symbol> args = new ArrayList<>();
        if (esTipo(tokenActual.getClase())) {
            do {
                int tipo = tipo();
                String id = tokenActual.getLexema();
                eat(ClaseLexica.ID);
                args.add(new Symbol(-1, tipo, id, null));
                if (tokenActual.getClase() == ClaseLexica.COMA) {
                    eat(ClaseLexica.COMA);
                } else {
                    break;
                }
            } while (true);
        }
        return args;
    }

    private void bloque() {
        eat(ClaseLexica.LLAVE_ABRE);
        decl_var();
        instrucciones();
        eat(ClaseLexica.LLAVE_CIERRA);
    }

    private void instrucciones() {
        while (esInicioSentencia()) {
            sentencia();
        }
    }

    private void sentencia() {
        System.out.println("Entrando a sentencia");
    
        if (tokenActual.getClase() == ClaseLexica.ID) {
            // Asignación
            String id = tokenActual.getLexema();
            eat(ClaseLexica.ID);
    
            Symbol simbolo = lookupSimbolo(id);
            if (simbolo == null) {
                error("Identificador no declarado: " + id);
            }
    
            if (tokenActual.getClase() == ClaseLexica.ASIGNACION) {
                System.out.println("Entrando a sentencia-igual");
                eat(ClaseLexica.ASIGNACION);
                int tipoExpresion = exp();
                if (simbolo.getType() != tipoExpresion) {
                    error("Incompatibilidad de tipos en asignación: esperado "
                            + simbolo.getType() + ", encontrado " + tipoExpresion);
                }
                eat(ClaseLexica.PUNTO_Y_COMA);
            } else {
                error("Se esperaba '=' después del identificador.");
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.IF) {
            // Sentencia if
            eat(ClaseLexica.IF);
            eat(ClaseLexica.PARENTESIS_ABRE);
            int tipoCondicion = exp();
            if (tipoCondicion != 5) { // 5 representa boolean
                error("La condición del 'if' debe ser de tipo boolean.");
            }
            eat(ClaseLexica.PARENTESIS_CIERRA);
            sentencia();
            if (tokenActual.getClase() == ClaseLexica.ELSE) {
                eat(ClaseLexica.ELSE);
                sentencia();
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.WHILE) {
            // Sentencia while
            eat(ClaseLexica.WHILE);
            eat(ClaseLexica.PARENTESIS_ABRE);
            int tipoCondicion = exp();
            if (tipoCondicion != 5) {
                error("La condición del 'while' debe ser de tipo boolean.");
            }
            eat(ClaseLexica.PARENTESIS_CIERRA);
            sentencia();
    
        } else if (tokenActual.getClase() == ClaseLexica.DO) {
            // Sentencia do-while
            eat(ClaseLexica.DO);
            sentencia();
            eat(ClaseLexica.WHILE);
            eat(ClaseLexica.PARENTESIS_ABRE);
            int tipoCondicion = exp();
            if (tipoCondicion != 5) {
                error("La condición del 'do-while' debe ser de tipo boolean.");
            }
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
    
        } else if (tokenActual.getClase() == ClaseLexica.BREAK) {
            // Sentencia break
            System.out.println("Entrando al caso del break");
            eat(ClaseLexica.BREAK);
            eat(ClaseLexica.PUNTO_Y_COMA);
    
        } else if (tokenActual.getClase() == ClaseLexica.RETURN) {
            // Sentencia return
            eat(ClaseLexica.RETURN);
            int tipoReturn = -1; // Asumimos que void es por defecto
            if (esInicioExpresion()) {
                tipoReturn = exp();
            }
            eat(ClaseLexica.PUNTO_Y_COMA);
    
            // Validar el tipo de retorno con el tipo de la función actual
            SymbolTable tablaActual = stackSymbolTable.peek();
            Symbol funcionActual = obtenerFuncionActual(tablaActual);
            if (funcionActual == null) {
                error("No se encontró la función actual para validar el retorno.");
            }
            if (tipoReturn != funcionActual.getType() && funcionActual.getType() != 0) { // 0 es void
                error("El tipo de retorno no coincide con el tipo de la función: esperado "
                        + funcionActual.getType() + ", encontrado " + tipoReturn);
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.SWITCH) {
            // Sentencia switch
            eat(ClaseLexica.SWITCH);
            eat(ClaseLexica.PARENTESIS_ABRE);
            int tipoExpresion = exp();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.LLAVE_ABRE);
            casos(tipoExpresion); // Validar casos del switch con la expresión
            eat(ClaseLexica.LLAVE_CIERRA);
    
        } else if (tokenActual.getClase() == ClaseLexica.PRINT) {
            // Sentencia print
            eat(ClaseLexica.PRINT);
            int tipo = exp();
            // Aquí puedes validar que `tipo` sea imprimible si tienes reglas adicionales
            eat(ClaseLexica.PUNTO_Y_COMA);
    
        } else if (tokenActual.getClase() == ClaseLexica.SCAN) {
            // Sentencia scan
            eat(ClaseLexica.SCAN);
            parteIzquierda();
            eat(ClaseLexica.PUNTO_Y_COMA);
    
        } else if (tokenActual.getClase() == ClaseLexica.LLAVE_ABRE) {
            // Bloque
            bloque();
    
        } else {
            error("Inicio no válido de una sentencia.");
        }
        System.out.println("Saliendo de sentencia");
    }
    
    
    private void casos(int tipoSwitch) {
        while (tokenActual.getClase() == ClaseLexica.CASE) {
            eat(ClaseLexica.CASE);
            int tipoCaso = exp();
            if (tipoCaso != tipoSwitch) {
                error("El tipo del caso no coincide con el tipo del switch.");
            }
            eat(ClaseLexica.DOS_PUNTOS);
            instrucciones(); // Procesar las instrucciones del caso
        }
        if (tokenActual.getClase() == ClaseLexica.DEFAULT) {
            eat(ClaseLexica.DEFAULT);
            eat(ClaseLexica.DOS_PUNTOS);
            instrucciones(); // Procesar las instrucciones del caso default
        }
    }

    private void parteIzquierda() {
        if (tokenActual.getClase() == ClaseLexica.ID) {
            String id = tokenActual.getLexema();
            eat(ClaseLexica.ID);
            Symbol simbolo = lookupSimbolo(id);
            if (simbolo == null) {
                error("Identificador no declarado: " + id);
            }
            // Manejo adicional si es necesario (arrays, structs, etc.)
        } else {
            error("Se esperaba una parte izquierda válida.");
        }
    }
    
    
    // Evaluación de expresiones
private int exp() {
    return exp_or();
}

// Producción exp_or → exp_and { '||' exp_and }
private int exp_or() {
    int tipoIzquierdo = exp_and();
    while (tokenActual.getClase() == ClaseLexica.OR) {
        eat(ClaseLexica.OR);
        int tipoDerecho = exp_and();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.OR);
    }
    return tipoIzquierdo;
}

// Producción exp_and → exp_eq { '&&' exp_eq }
private int exp_and() {
    int tipoIzquierdo = exp_eq();
    while (tokenActual.getClase() == ClaseLexica.AND) {
        eat(ClaseLexica.AND);
        int tipoDerecho = exp_eq();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.AND);
    }
    return tipoIzquierdo;
}

// Producción exp_eq → exp_rel { ('==' | '!=') exp_rel }
private int exp_eq() {
    int tipoIzquierdo = exp_rel();
    while (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
        eat(tokenActual.getClase());
        int tipoDerecho = exp_rel();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.IGUAL);
    }
    return tipoIzquierdo;
}

// Producción exp_rel → exp_add { ('<' | '>' | '<=' | '>=') exp_add }
private int exp_rel() {
    int tipoIzquierdo = exp_add();
    while (esOperadorRelacional(tokenActual.getClase())) {
        eat(tokenActual.getClase());
        int tipoDerecho = exp_add();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, tokenActual.getClase());
    }
    return tipoIzquierdo;
}

// Producción exp_add → exp_mul { ('+' | '-') exp_mul }
private int exp_add() {
    int tipoIzquierdo = exp_mul();
    while (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
        eat(tokenActual.getClase());
        int tipoDerecho = exp_mul();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.MAS);
    }
    return tipoIzquierdo;
}

// Producción exp_mul → exp_unary { ('*' | '/') exp_unary }
private int exp_mul() {
    int tipoIzquierdo = exp_unary();
    while (tokenActual.getClase() == ClaseLexica.MULTIPLICACION || tokenActual.getClase() == ClaseLexica.DIVISION) {
        eat(tokenActual.getClase());
        int tipoDerecho = exp_unary();
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.MULTIPLICACION);
    }
    return tipoIzquierdo;
}

// Producción exp_unary → ('-' | '!') exp_unary | primary
private int exp_unary() {
    if (tokenActual.getClase() == ClaseLexica.MENOS || tokenActual.getClase() == ClaseLexica.NOT) {
        eat(tokenActual.getClase());
        return exp_unary();
    } else {
        return primary();
    }
}

// Producción primary → '(' exp ')' | ID | LITERAL
private int primary() {
    if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
        eat(ClaseLexica.PARENTESIS_ABRE);
        int tipo = exp();
        eat(ClaseLexica.PARENTESIS_CIERRA);
        return tipo;
    } else if (tokenActual.getClase() == ClaseLexica.ID) {
        String id = tokenActual.getLexema();
        eat(ClaseLexica.ID);

        // Buscar en la tabla de símbolos
        Symbol simbolo = lookupSimbolo(id);
        if (simbolo == null) {
            error("Identificador no declarado: " + id);
        }
        return simbolo.getType();
    } else if (esLiteral(tokenActual.getClase())) {
        int tipo = getTipoLiteral(tokenActual.getClase());
        eat(tokenActual.getClase());
        return tipo;
    } else {
        error("Expresión no válida.");
        return -1; // Inaccesible
    }
}

// Métodos auxiliares para validación y compatibilidad de tipos

private int validarCompatibilidadTipos(int tipoIzquierdo, int tipoDerecho, ClaseLexica operacion) {
    // Usar la lógica de promoción y compatibilidad de tipos
    Type tipoPromocionado = Type.getPromotedType(
        typeTable.getType(tipoIzquierdo),
        typeTable.getType(tipoDerecho)
    );

    if (tipoPromocionado == null) {
        error("Tipos incompatibles en operación: " + operacion);
    }

    return tipoPromocionado.getId();
}

    private void agregarSimbolo(String id, Symbol symbol) {
        Optional<SymbolTable> tablaActual = Optional.ofNullable(stackSymbolTable.peek());
        if (tablaActual.isPresent()) {
            tablaActual.get().addSymbol(id, symbol);
        } else {
            error("No hay tabla de símbolos activa.");
        }
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
        if (clase == ClaseLexica.MENOR || 
            clase == ClaseLexica.MENOR_IGUAL || 
            clase == ClaseLexica.MAYOR || 
            clase == ClaseLexica.MAYOR_IGUAL) {
            return true;
        } else {
            return false;
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
               clase == ClaseLexica.STRING ||
               clase == ClaseLexica.VOID;
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


private int getTipoLiteral(ClaseLexica clase) {
    switch (clase) {
        case LITERAL_ENTERA: return 1;
        
        case LITERAL_FLOTANTE: return 2;
        
        case LITERAL_DOUBLE: return 3;
        
        case LITERAL_CADENA: return 4;
      
        case TRUE: return 5;
       
        case FALSE: return 5; // Booleanos
       
        default: return -1;
    }
}

private boolean esLiteral(ClaseLexica clase) {
    return clase == ClaseLexica.LITERAL_ENTERA ||
           clase == ClaseLexica.LITERAL_FLOTANTE ||
           clase == ClaseLexica.LITERAL_DOUBLE ||
           clase == ClaseLexica.LITERAL_CADENA ||
           clase == ClaseLexica.TRUE ||
           clase == ClaseLexica.FALSE;
}
    private Symbol lookupSimbolo(String id) {
        SymbolTable tablaActual = stackSymbolTable.peek();
        while (tablaActual != null) {
            Optional<Symbol> simbolo = tablaActual.getSymbol(id);
            if (simbolo.isPresent()) {
                return simbolo.get();
            }
            // Moverse a la tabla de símbolos del scope anterior
            stackSymbolTable.pop();
            tablaActual = stackSymbolTable.peek();
        }
        return null; // No encontrado
}

private Symbol obtenerFuncionActual(SymbolTable tabla) {
    // Busca una función en la tabla actual; asume que es la última declarada
    for (Symbol symbol : tabla.getAllSymbols()) {
        if ("funcion".equals(symbol.getCat())) {
            return symbol;
        }
    }
    return null;
}
}
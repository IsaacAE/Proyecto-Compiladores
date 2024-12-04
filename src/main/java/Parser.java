package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        inicializarTypeTable();
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
        System.out.println("TABLA DE SIMBOLOS");
        imprimirTablaDeSimbolos(stackSymbolTable.peek());
        decl_func();
    }

    private void decl_proto() {
        while (tokenActual.getClase() == ClaseLexica.PROTO) {
            eat(ClaseLexica.PROTO);
            int tipoRetorno = tipo(); // Tipo de retorno del prototipo
            String idPrototipo = tokenActual.getLexema(); // Nombre del prototipo
            eat(ClaseLexica.ID);
    
            SymbolTable tablaGlobal = stackSymbolTable.base(); // Obtener la tabla global
    
            // Verificar si ya existe un prototipo con el mismo ID en la tabla global
            Optional<Symbol> simboloExistenteOpt = tablaGlobal.getSymbol(idPrototipo);
            if (simboloExistenteOpt.isPresent() && "prototipo".equals(simboloExistenteOpt.get().getCat())) {
                error("El prototipo '" + idPrototipo + "' ya está declarado en la tabla global.");
            }
    
            eat(ClaseLexica.PARENTESIS_ABRE);
    
            // Crear una nueva tabla de símbolos para los argumentos del prototipo
            SymbolTable tablaPrototipo = new SymbolTable();
            stackSymbolTable.push(tablaPrototipo); // Empujar la tabla a la pila
    
            // Procesar los argumentos del prototipo y registrar en la tabla
            List<String> argumentos = argumentos();
    
            // Registrar el prototipo en la tabla global con la lista de tipos de argumentos
            Symbol simboloPrototipo = new Symbol(-1, tipoRetorno, "prototipo", argumentos);
            tablaGlobal.addSymbol(idPrototipo, simboloPrototipo);
    
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
    
            System.out.println("Prototipo '" + idPrototipo + "' registrado en la tabla global.");
            System.out.println("Tabla de símbolos para el prototipo '" + idPrototipo + "':");
            imprimirTablaDeSimbolos(tablaPrototipo);
    
            stackSymbolTable.pop(); // Retirar la tabla de argumentos del prototipo
        }
    }
    

    // Producción decl_var
    private void decl_var() {
        while (esTipo(tokenActual.getClase())) {
            int tipo = tipo();
            System.out.println("El tipo de las siguientes variables es:"+ tipo);
            List<String> variables = lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);

            // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "variable", null);
                agregarSimbolo(var, varSymbol);
            }
        }
    }

    private void decl_func() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            eat(ClaseLexica.FUNC);
    
            // Obtener el tipo de retorno de la función
            int tipoRetorno = tipo();
    
            // Obtener el nombre de la función
            String idFuncion = tokenActual.getLexema();
            eat(ClaseLexica.ID);
    
            // Registrar la función en la tabla de símbolos global
            SymbolTable tablaGlobal = stackSymbolTable.base();
            // Verificar si ya existe un prototipo con el mismo ID en la tabla global
            Optional<Symbol> simboloExistenteOpt = tablaGlobal.getSymbol(idFuncion);
            if (simboloExistenteOpt.isPresent() && "funcion".equals(simboloExistenteOpt.get().getCat())) {
                error("El prototipo '" + idFuncion + "' ya está declarado en la tabla global.");
            }
            eat(ClaseLexica.PARENTESIS_ABRE);
            
    
            // Crear una nueva tabla de símbolos para el ámbito de la función
            SymbolTable tablaFuncion = new SymbolTable();
            stackSymbolTable.push(tablaFuncion);
            List<String> argumentos = argumentos();
           
            // Crear el símbolo de la función
            Symbol simboloFuncion = new Symbol(-1, tipoRetorno, "funcion", argumentos);
            tablaGlobal.addSymbol(idFuncion, simboloFuncion);
            SymbolTable tablaLocal = stackSymbolTable.peek();
            tablaLocal.addSymbol(idFuncion, simboloFuncion);
    
            System.out.println("Función '" + idFuncion + "' agregada a la tabla de símbolos global.");

            // Procesar el bloque de la función
            eat(ClaseLexica.PARENTESIS_CIERRA);
            bloque();
    
            // Mantener la tabla de la función en la pila (no se elimina)
            System.out.println("Tabla de símbolos para la función '" + idFuncion + "':");
            imprimirTablaDeSimbolos(stackSymbolTable.peek());
        }
    
        // Procesar funciones adicionales
        decl_func_prima();
    }
    
    private void decl_func_prima() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            decl_func();
        }
    }
    
    

    private int tipo() {
        int tipoBase;
    
        if (esTipoBasico(tokenActual.getClase())) {
            System.out.println("Entrando a tipo basico");
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
            puntero();
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
        switch (tokenActual.getClase()) {
            case INT: tipo = 1;
            break;
            case FLOAT: tipo = 2;
            break;
            case DOUBLE: tipo = 3;
            break;
            case STRING: tipo = 4;
            break;
            case TRUE: tipo = 5;
            break;
            case FALSE: tipo = 5;
            break;
            case VOID: tipo = 0;
            break;
            default: tipo = 8;
        }
        eat(tokenActual.getClase());
        
        
        
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



private List<String> argumentos() {
    List<String> listaArgumentos = new ArrayList<>();

    if (esTipo(tokenActual.getClase())) {
        int tipo = tipo(); // Obtener el tipo del argumento
        String idArgumento = tokenActual.getLexema();
        eat(ClaseLexica.ID);

        // Crear y registrar el símbolo del argumento en la tabla de símbolos
        Symbol argumento = new Symbol(-1, tipo, "argumento", null);
        stackSymbolTable.peek().addSymbol(idArgumento, argumento);

        // Agregar el argumento en formato "tipo nombre" a la lista
        listaArgumentos.add(getTipoFromInt(tipo) + " " + idArgumento);

        // Procesar argumentos adicionales separados por comas
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);

            tipo = tipo();
            idArgumento = tokenActual.getLexema();
            eat(ClaseLexica.ID);

            // Crear y registrar el símbolo del argumento
            argumento = new Symbol(-1, tipo, "argumento", null);
            stackSymbolTable.peek().addSymbol(idArgumento, argumento);

            // Agregar a la lista
            listaArgumentos.add(getTipoFromInt(tipo) + " " + idArgumento);
        }
    }

    return listaArgumentos;
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
    
            Symbol simbolo = stackSymbolTable.lookup(id);
            if (simbolo == null) {
                imprimirTablaDeSimbolos(stackSymbolTable.peek());
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
            //if (tipoCondicion != 5) { // 5 representa boolean
               // error("La condición del 'if' debe ser de tipo boolean.");
           // }
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
            if (tipoCondicion == 4 ) {
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
            if (tipoCondicion == 4) {
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
            int tipoReturn = 0; // Asumimos que void es por defecto
            if (esInicioExpresion()) {
                tipoReturn = exp();
            }
            eat(ClaseLexica.PUNTO_Y_COMA);
    
            // Validar el tipo de retorno con el tipo de la función actual
            SymbolTable tablaActual = stackSymbolTable.base();
            Symbol funcionActual = obtenerFuncionActual(tablaActual);
            if (funcionActual == null) {
                error("No se encontró la función actual para validar el retorno.");
            }
            if (tipoReturn != funcionActual.getType() ) { // 0 es void
                error("El tipo de retorno no coincide con el tipo de la función: esperado "
                        + funcionActual.getType() + ", encontrado " + tipoReturn);
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.SWITCH) {
            // Sentencia switch
            eat(ClaseLexica.SWITCH);
            eat(ClaseLexica.PARENTESIS_ABRE);
            int tipoExpresion = exp();
            System.out.println(tipoExpresion);
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
            int tipoCaso = exp(); // Evaluar el tipo del caso
            if (tipoCaso != tipoSwitch) {
                error("El tipo del caso no coincide con el tipo de la expresión del switch. "
                        + "Esperado: " + tipoSwitch + ", encontrado: " + tipoCaso);
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
            Symbol simbolo = stackSymbolTable.lookup(id);;
            if (simbolo == null) {
                error("Identificador no declarado: " + id);
            }
            // Manejo adicional si es necesario (arrays, structs, etc.)
        } else {
            error("Se esperaba una parte izquierda válida.");
        }
    }
    
    
// Evaluación de Expresiones
private int exp() {
    System.out.println("Entrando en exp");
    int tipo = exp_or();
    System.out.println("Saliendo de exp con tipo: " + tipo);
    return tipo;
}

// Producción exp_or → exp_and { '||' exp_and }
private int exp_or() {
    System.out.println("Entrando en exp_or");
    int tipoIzquierdo = exp_and();
    System.out.println("Tipo izquierdo de exp_or: " + tipoIzquierdo);

    while (tokenActual.getClase() == ClaseLexica.OR) {
        System.out.println("Operador '||' encontrado");
        eat(ClaseLexica.OR);
        int tipoDerecho = exp_and();
        System.out.println("Tipo derecho de exp_or: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.OR);
    }

    System.out.println("Saliendo de exp_or con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_and → exp_eq { '&&' exp_eq }
private int exp_and() {
    System.out.println("Entrando en exp_and");
    int tipoIzquierdo = exp_eq();
    System.out.println("Tipo izquierdo de exp_and: " + tipoIzquierdo);

    while (tokenActual.getClase() == ClaseLexica.AND) {
        System.out.println("Operador '&&' encontrado");
        eat(ClaseLexica.AND);
        int tipoDerecho = exp_eq();
        System.out.println("Tipo derecho de exp_and: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.AND);
    }

    System.out.println("Saliendo de exp_and con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_eq → exp_rel { ('==' | '!=') exp_rel }
private int exp_eq() {
    System.out.println("Entrando en exp_eq");
    int tipoIzquierdo = exp_rel();
    System.out.println("Tipo izquierdo de exp_eq: " + tipoIzquierdo);

    while (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
        ClaseLexica operador = tokenActual.getClase();
        System.out.println("Operador " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_rel();
        System.out.println("Tipo derecho de exp_eq: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
    }

    System.out.println("Saliendo de exp_eq con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_rel → exp_add { ('<' | '>' | '<=' | '>=') exp_add }
private int exp_rel() {
    System.out.println("Entrando en exp_rel");
    int tipoIzquierdo = exp_add();
    System.out.println("Tipo izquierdo de exp_rel: " + tipoIzquierdo);

    while (esOperadorRelacional(tokenActual.getClase())) {
        ClaseLexica operador = tokenActual.getClase();
        System.out.println("Operador relacional " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_add();
        System.out.println("Tipo derecho de exp_rel: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
    }

    System.out.println("Saliendo de exp_rel con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_add → exp_mul { ('+' | '-') exp_mul }
private int exp_add() {
    System.out.println("Entrando en exp_add");
    int tipoIzquierdo = exp_mul();
    System.out.println("Tipo izquierdo de exp_add: " + tipoIzquierdo);

    while (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
        System.out.println("Operador encontrado: " + tokenActual.getClase());
        eat(tokenActual.getClase());
        int tipoDerecho = exp_mul();
        System.out.println("Tipo derecho de exp_add: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.MAS);
    }

    System.out.println("Saliendo de exp_add con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_mul → exp_unary { ('*' | '/' | '%' | '//') exp_unary }
private int exp_mul() {
    System.out.println("Entrando en exp_mul");
    int tipoIzquierdo = exp_unary();
    System.out.println("Tipo izquierdo de exp_mul: " + tipoIzquierdo);

    while (tokenActual.getClase() == ClaseLexica.MULTIPLICACION || tokenActual.getClase() == ClaseLexica.DIVISION) {
        System.out.println("Operador encontrado: " + tokenActual.getClase());
        eat(tokenActual.getClase());
        int tipoDerecho = exp_unary();
        System.out.println("Tipo derecho de exp_mul: " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.MULTIPLICACION);
    }

    System.out.println("Saliendo de exp_mul con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_unary → ('-' | '!') exp_unary | primary
private int exp_unary() {
    System.out.println("Entrando en exp_unary");

    if (tokenActual.getClase() == ClaseLexica.MENOS || tokenActual.getClase() == ClaseLexica.NOT) {
        System.out.println("Operador unario encontrado: " + tokenActual.getClase());
        eat(tokenActual.getClase());
        return exp_unary();
    } else {
        return primary();
    }
}

private int primary() {
    System.out.println("Entrando en primary");
    if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
        eat(ClaseLexica.PARENTESIS_ABRE);
        int tipo = exp(); // Evaluar la subexpresión
        eat(ClaseLexica.PARENTESIS_CIERRA);
        System.out.println("Saliendo de primary con tipo (expresion entre paréntesis): " + tipo);
        return tipo;

    } else if (tokenActual.getClase() == ClaseLexica.ID) {
        String id = tokenActual.getLexema();
        eat(ClaseLexica.ID);

        // Verificar si es una llamada a función
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            System.out.println("Identificador '" + id + "' detectado como una posible llamada a función.");
            return llamada(id); // Procesar la llamada a la función
        }

        // Recuperar el tipo del identificador desde la tabla de símbolos
        Symbol simbolo = stackSymbolTable.lookup(id);;
        if (simbolo == null) {
            imprimirTablaDeSimbolos(stackSymbolTable.peek());
            error("Identificador no declarado: " + id);
        }
        System.out.println("El simbolo hallado es: " + simbolo);
        System.out.println("Saliendo de primary con tipo de identificador: " + simbolo.getType());
        return simbolo.getType();

    } else if (esLiteral(tokenActual.getClase())) {
        int tipo = getTipoLiteral(tokenActual.getClase()); // Obtener el tipo del literal
        eat(tokenActual.getClase());
        System.out.println("Saliendo de primary con tipo de literal: " + tipo);
        return tipo;

    } else {
        error("Expresión no válida.");
        return -1; // Código inaccesible
    }
}

private int llamada(String idFuncion) {
    eat(ClaseLexica.PARENTESIS_ABRE);

    // Recuperar la función de la tabla de símbolos global
    Symbol simboloFuncion = stackSymbolTable.lookup(idFuncion);
    if (simboloFuncion == null || !"funcion".equals(simboloFuncion.getCat())) {
        error("La función '" + idFuncion + "' no está declarada.");
    }

    // Obtener la lista de argumentos declarados en la función
    List<String> argumentosDeclarados = simboloFuncion.getArgs();
    if (argumentosDeclarados == null) {
        argumentosDeclarados = new ArrayList<>();
    }

    // Validar argumentos pasados en la llamada
    List<Integer> tiposArgumentosLlamada = new ArrayList<>();
    if (esInicioExpresion()) {
        tiposArgumentosLlamada.add(exp());
        while (tokenActual.getClase() == ClaseLexica.COMA) {
            eat(ClaseLexica.COMA);
            tiposArgumentosLlamada.add(exp());
        }
    }
    eat(ClaseLexica.PARENTESIS_CIERRA);

    // Verificar número de argumentos
    if (tiposArgumentosLlamada.size() != argumentosDeclarados.size()) {
        error("La función '" + idFuncion + "' espera " + argumentosDeclarados.size() +
              " argumentos, pero se pasaron " + tiposArgumentosLlamada.size() + ".");
    }

    // Verificar tipos de argumentos
    for (int i = 0; i < tiposArgumentosLlamada.size(); i++) {
        int tipoArgumentoLlamada = tiposArgumentosLlamada.get(i);
        int tipoArgumentoDeclarado = getTipoFromString(argumentosDeclarados.get(i));
        if (tipoArgumentoLlamada != tipoArgumentoDeclarado) {
            error("El argumento " + (i + 1) + " de la llamada a la función '" + idFuncion +
                  "' no coincide en tipo. Esperado: " + tipoArgumentoDeclarado +
                  ", encontrado: " + tipoArgumentoLlamada + ".");
        }
    }

    System.out.println("Llamada a función '" + idFuncion + "' validada correctamente.");
    return simboloFuncion.getType(); // Retornar el tipo de retorno de la función
}




// Métodos auxiliares para validación y compatibilidad de tipos

private int validarCompatibilidadTipos(int tipoIzquierdo, int tipoDerecho, ClaseLexica operacion) {
    // Usar la lógica de promoción y compatibilidad de tipos
    System.out.println("tipoIzquierdo:"+ tipoIzquierdo + " tipoDerecho:"+tipoDerecho);


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
    

private Symbol obtenerFuncionActual(SymbolTable tabla) {
    // Busca una función en la tabla actual; asume que es la última declarada
    System.out.println("###BUSCANDO FUNCION ACTUAL###");
    
    tabla = stackSymbolTable.peek();
    imprimirTablaDeSimbolos(tabla);
    for (Symbol symbol : tabla.getAllSymbols()) {
        if ("funcion".equals(symbol.getCat())) {
            return symbol;
        }
    }
    return null;
}


private int getTipoFromString(String tipo) {
     tipo = tipo.split(" ")[0];
    switch (tipo) {
        case "int": return 1;
        case "float": return 2;
        case "double": return 3;
        case "string": return 4;
        case "boolean": return 5;
        case "void": return 0;
        default: error("Tipo desconocido: " + tipo); return -1;
    }
}


private String getTipoFromInt(int tipo) {
    switch (tipo) {
        case 1: return "int";
        case 2: return "float";
        case 3: return "double";
        case 4: return "string";
        case 5: return "boolean";
        case 0: return "void";
        default: error("Tipo desconocido: " + tipo); return "desconocido";
    }
}

// Inicializar la tabla de tipos
private void inicializarTypeTable() {
    typeTable = new TypeTable();
    
    // Registrar tipos básicos
    typeTable.addType(1, 0, 3); // int
    typeTable.addType(2, 0, 3); // float
    typeTable.addType(3, 0, 3); // double
    typeTable.addType(4, 0, 4); // string
    typeTable.addType(5, 0, 5); // boolean
    typeTable.addType(0, 0, 0); // void

   
}

private void imprimirTablaDeSimbolos(SymbolTable tabla) {
    if (tabla != null) {
        System.out.println("Tabla de símbolos:");
        
        // Obtener todos los IDs
        Set<String> ids = tabla.getAllIds();
        
        // Imprimir cada ID junto con su símbolo correspondiente
        for (String id : ids) {
            Symbol symbol = tabla.getSymbolSecure(id);  // Obtener el símbolo por el id
            if (symbol != null) {
                System.out.println("ID: " + id + ", " + symbol);
            }
        }
    } else {
        System.out.println("La tabla de símbolos está vacía o no existe.");
    }
}




}
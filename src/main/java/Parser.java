package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Optional;

import main.jflex.Lexer;

public class Parser {
    private Lexer lexer;
    private Token tokenActual;

    private SymbolTableStack stackSymbolTable = new SymbolTableStack();
    private TypeTable typeTable = new TypeTable();
    private Map<String, SymbolTable> structTables = new HashMap<>();


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
        imprimirTablaDeSimbolos(stackSymbolTable.base());
        imprimirTablaDeTipos();
        throw new RuntimeException("Error: " + mensaje);
        
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
            
            // Manejo de struct anónimo (token actual es '}')
        if (tokenActual.getClase() == ClaseLexica.LLAVE_CIERRA) {
            eat(ClaseLexica.LLAVE_CIERRA); // Consumimos la llave de cierre
            tipo = 9; // El último tipo registrado corresponde al struct
        } 
            System.out.println("El tipo de las siguientes variables es:"+ tipo);
            List<String> variables = lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);

            if(tipo < -1){

                // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "arreglo", null);
                agregarSimbolo(var, varSymbol);
            }
            }else{
            // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "variable", null);
                agregarSimbolo(var, varSymbol);
            }
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
        int tipoBase=-1;
    
        if (esTipoBasico(tokenActual.getClase())) {
           // System.out.println("Entrando a tipo basico");
            tipoBase = basico(); // Identificar el tipo básico
            tipoBase = tipo_prima(tipoBase); // Manejar tipos compuestos, si los hay
        } else if (tokenActual.getClase() == ClaseLexica.STRUCT) {
            eat(ClaseLexica.STRUCT);
            int structId = typeTable.size(); // Usar el tamaño actual de TypeTable como ID único
            String structName = "struct_" + structId; // Generar el nombre único
            // Registrar el tipo en TypeTable
            typeTable.addType(0, 0, null);
            System.out.println("Struct '" + structName + "' registrado con ID: " + structId);
            tipoBase = structId;
            SymbolTable structTable = new SymbolTable();
            stackSymbolTable.push(structTable); // Crear un nuevo ámbito para el struct
            eat(ClaseLexica.LLAVE_ABRE);
            decl_var(); // Procesar las variables internas del struct
            eat(ClaseLexica.LLAVE_CIERRA);

            // Registrar la tabla de símbolos del struct en el HashMap
            structTables.put(structName, stackSymbolTable.pop());

            
            imprimirTablaDeSimbolos(structTables.get(structName));
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
    
        // Inicializar la cadena que representará las dimensiones
        String dimensiones = "";
    
        if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
            int dimension = Integer.parseInt(tokenActual.getLexema());
            eat(ClaseLexica.LITERAL_ENTERA);
    
            dimensiones += dimension; // Agregar la dimensión inicial
        } else {
            error("Se esperaba un literal entero como tamaño del arreglo.");
        }
    
        eat(ClaseLexica.CORCHETE_CIERRA);
    
        // Pasar el tipo base y las dimensiones acumuladas a compuesto_prima
        return compuesto_prima(tipoBase, dimensiones);
    }
    
    

    private int compuesto_prima(int tipoBase, String dimensiones) {
        if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
            eat(ClaseLexica.CORCHETE_ABRE);
    
            if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
                int dimension = Integer.parseInt(tokenActual.getLexema());
                eat(ClaseLexica.LITERAL_ENTERA);
    
                dimensiones += dimension; // Concatenar la nueva dimensión
            } else {
                error("Se esperaba un literal entero como tamaño del arreglo.");
            }
    
            eat(ClaseLexica.CORCHETE_CIERRA);
    
            // Recursión para manejar más dimensiones
            return compuesto_prima(tipoBase, dimensiones);
        }
    
        // Construir el identificador único del tipo compuesto
        String tipoCompuestoId = "-" + tipoBase + dimensiones;
    
        // Registrar en la tabla de tipos y retornar el ID único
         // Calcular el número total de ítems multiplicando los dígitos de las dimensiones
        int totalItems = calcularTotalItems(dimensiones);
        int idCompuesto = Integer.parseInt(tipoCompuestoId);
        Integer tipoPadre = typeTable.getType(tipoBase).getParent();
        if (!typeTable.contains(idCompuesto)) {
            typeTable.addTypeArray(idCompuesto, totalItems, 0, tipoPadre); // Añadir el tipo compuesto
        }
    
        return idCompuesto;
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
            case RUNE: tipo = 5;
            break;
            case TRUE: tipo = 6;
            break;
            case FALSE: tipo = 6;
            break;
            case COMPLEX: tipo = 7;
            break;
            case VOID: tipo = 0;
            break;
            default: tipo = -1;
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
        //System.out.println("Entrando a sentencia");
        int compatibilidad;
        if (tokenActual.getClase() == ClaseLexica.ID) {
                // Manejar parte izquierda
                int tipoIzquierdo = parteIzquierda(); // Obtener el tipo de la parte izquierda
        
                 
        
                   
            if (tokenActual.getClase() == ClaseLexica.ASIGNACION) {
                eat(ClaseLexica.ASIGNACION);
                int tipoExpresion = exp();
                 // Validar compatibilidad entre la parte izquierda y la expresión
                 compatibilidad= validarCompatibilidadTipos(tipoIzquierdo, tipoExpresion, ClaseLexica.ASIGNACION );
                 if(compatibilidad== -1){
                    error("Incompatibilidad para asignación ");
                 }
                eat(ClaseLexica.PUNTO_Y_COMA);
                System.out.println("Asignación procesada correctamente.");
            } else {
                error("Se esperaba '=' después de la parte izquierda.");
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
       // System.out.println("Saliendo de sentencia");
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
    

    private int parteIzquierda() {
        if (tokenActual.getClase() == ClaseLexica.ID) {
            String id = tokenActual.getLexema();
            eat(ClaseLexica.ID);
    
            Symbol simbolo = stackSymbolTable.lookup(id);
            if (simbolo == null) {
                error("Identificador no declarado: " + id);
            }
    
            int tipo = simbolo.getType();
    
            // Verificar si la parte izquierda es un arreglo o estructura
            if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE || tokenActual.getClase() == ClaseLexica.PUNTO) {
                tipo = localizacion(id); // Procesar localización
            }
    
            return tipo; // Retornar el tipo final
        } else {
            error("Se esperaba una parte izquierda válida.");
            return -1; // Código inaccesible
        }
    }
    
    
    
    
// Evaluación de Expresiones
private int exp() {
   // System.out.println("Entrando en exp");
    int tipo = exp_or();  // Expresión OR
   // System.out.println("Saliendo de exp con tipo: " + tipo);
    return tipo;
}

// Producción exp_or → exp_and exp_or'
private int exp_or() {
   // System.out.println("Entrando en exp_or");
    int tipoIzquierdo = exp_and(); // Expresión AND
   // System.out.println("Tipo izquierdo de exp_or: " + tipoIzquierdo);
    tipoIzquierdo = exp_or_prima(tipoIzquierdo); // Llamada a la producción exp_or'
   // System.out.println("Saliendo de exp_or con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_or' → || exp_and exp_or' | epsilon
private int exp_or_prima(int tipoIzquierdo) {
    if (tokenActual.getClase() == ClaseLexica.OR) {
       // System.out.println("Operador '||' encontrado");
        eat(ClaseLexica.OR);
        int tipoDerecho = exp_and(); // Expresión AND
       // System.out.println("Tipo derecho de exp_or': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.OR);
        tipoIzquierdo = exp_or_prima(tipoIzquierdo); // Recursión para manejar más operadores OR
    }
    return tipoIzquierdo;
}

// Producción exp_and → exp_eq exp_and'
private int exp_and() {
   // System.out.println("Entrando en exp_and");
    int tipoIzquierdo = exp_eq(); // Expresión de igualdad
   // System.out.println("Tipo izquierdo de exp_and: " + tipoIzquierdo);
    tipoIzquierdo = exp_and_prima(tipoIzquierdo); // Llamada a la producción exp_and'
   // System.out.println("Saliendo de exp_and con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_and' → && exp_eq exp_and' | epsilon
private int exp_and_prima(int tipoIzquierdo) {
    if (tokenActual.getClase() == ClaseLexica.AND) {
        //System.out.println("Operador '&&' encontrado");
        eat(ClaseLexica.AND);
        int tipoDerecho = exp_eq(); // Expresión de igualdad
      //  System.out.println("Tipo derecho de exp_and': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, ClaseLexica.AND);
        tipoIzquierdo = exp_and_prima(tipoIzquierdo); // Recursión para manejar más operadores AND
    }
    return tipoIzquierdo;
}

// Producción exp_eq → exp_rel exp_eq'
private int exp_eq() {
   // System.out.println("Entrando en exp_eq");
    int tipoIzquierdo = exp_rel(); // Expresión relacional
   // System.out.println("Tipo izquierdo de exp_eq: " + tipoIzquierdo);
    tipoIzquierdo = exp_eq_prima(tipoIzquierdo); // Llamada a la producción exp_eq'
   // System.out.println("Saliendo de exp_eq con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_eq' → == exp_rel exp_eq' | != exp_rel exp_eq' | epsilon
private int exp_eq_prima(int tipoIzquierdo) {
    if (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
        ClaseLexica operador = tokenActual.getClase();
       // System.out.println("Operador " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_rel(); // Expresión relacional
       // System.out.println("Tipo derecho de exp_eq': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
        tipoIzquierdo = exp_eq_prima(tipoIzquierdo); // Recursión para manejar más operadores de igualdad
    }
    return tipoIzquierdo;
}

// Producción exp_rel → exp_add exp_rel'
private int exp_rel() {
   // System.out.println("Entrando en exp_rel");
    int tipoIzquierdo = exp_add(); // Expresión aritmética
   // System.out.println("Tipo izquierdo de exp_rel: " + tipoIzquierdo);
    tipoIzquierdo = exp_rel_prima(tipoIzquierdo); // Llamada a la producción exp_rel'
   // System.out.println("Saliendo de exp_rel con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_rel' → < exp_add exp_rel' | <= exp_add exp_rel' | >= exp_add exp_rel' | > exp_add exp_rel' | epsilon
private int exp_rel_prima(int tipoIzquierdo) {
    if (esOperadorRelacional(tokenActual.getClase())) {
        ClaseLexica operador = tokenActual.getClase();
      //  System.out.println("Operador relacional " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_add(); // Expresión aritmética
       // System.out.println("Tipo derecho de exp_rel': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
        tipoIzquierdo = exp_rel_prima(tipoIzquierdo); // Recursión para manejar más operadores relacionales
    }
    return tipoIzquierdo;
}

// Producción exp_add → exp_mul exp_add'
private int exp_add() {
   // System.out.println("Entrando en exp_add");
    int tipoIzquierdo = exp_mul(); // Expresión de multiplicación
   // System.out.println("Tipo izquierdo de exp_add: " + tipoIzquierdo);
    tipoIzquierdo = exp_add_prima(tipoIzquierdo); // Llamada a la producción exp_add'
   // System.out.println("Saliendo de exp_add con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_add' → + exp_mul exp_add' | - exp_mul exp_add' | epsilon
private int exp_add_prima(int tipoIzquierdo) {
    if (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
        ClaseLexica operador = tokenActual.getClase();
       // System.out.println("Operador " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_mul(); // Expresión de multiplicación
       // System.out.println("Tipo derecho de exp_add': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
        tipoIzquierdo = exp_add_prima(tipoIzquierdo); // Recursión para manejar más operadores de suma
    }
    return tipoIzquierdo;
}

// Producción exp_mul → exp_unary exp_mul'
private int exp_mul() {
   // System.out.println("Entrando en exp_mul");
    int tipoIzquierdo = exp_unary(); // Expresión unaria
   // System.out.println("Tipo izquierdo de exp_mul: " + tipoIzquierdo);
    tipoIzquierdo = exp_mul_prima(tipoIzquierdo); // Llamada a la producción exp_mul'
   // System.out.println("Saliendo de exp_mul con tipo: " + tipoIzquierdo);
    return tipoIzquierdo;
}

// Producción exp_mul' → * exp_unary exp_mul' | / exp_unary exp_mul' | % exp_unary exp_mul' | // exp_unary exp_mul' | epsilon
private int exp_mul_prima(int tipoIzquierdo) {
    if (tokenActual.getClase() == ClaseLexica.MULTIPLICACION || tokenActual.getClase() == ClaseLexica.DIVISION ||
        tokenActual.getClase() == ClaseLexica.MODULO || tokenActual.getClase() == ClaseLexica.DIVISION_ENTERA) {
        ClaseLexica operador = tokenActual.getClase();
       // System.out.println("Operador " + operador + " encontrado");
        eat(operador);
        int tipoDerecho = exp_unary(); // Expresión unaria
        //System.out.println("Tipo derecho de exp_mul': " + tipoDerecho);
        tipoIzquierdo = validarCompatibilidadTipos(tipoIzquierdo, tipoDerecho, operador);
        tipoIzquierdo = exp_mul_prima(tipoIzquierdo); // Recursión para manejar más operadores de multiplicación
    }
    return tipoIzquierdo;
}

// Producción exp_unary → ! exp_unary | - exp_unary | primary
private int exp_unary() {
   // System.out.println("Entrando en exp_unary");

    if (tokenActual.getClase() == ClaseLexica.MENOS || tokenActual.getClase() == ClaseLexica.NOT) {
        System.out.println("Operador unario encontrado: " + tokenActual.getClase());
        eat(tokenActual.getClase());
        return exp_unary(); // Recursión para manejar operadores unarios
    } else {
        return primary(); // Llamada a la producción primary
    }
}



// Producción primary → ( exp ) | id localizacion | false | literal_cadena | true | literal_runa | literal_entera | literal_flotante | literal_doble | literal_compleja | id ( parametros ) | id
private int primary() {
   // System.out.println("Entrando en primary");

    // Caso 1: Expresión entre paréntesis
    if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
        eat(ClaseLexica.PARENTESIS_ABRE);
        int tipo = exp(); // Evaluar la subexpresión
        eat(ClaseLexica.PARENTESIS_CIERRA);
       // System.out.println("Saliendo de primary con tipo (expresión entre paréntesis): " + tipo);
        return tipo;

    // Caso 2: Identificador con posible llamada a función
    } else if (tokenActual.getClase() == ClaseLexica.ID) {
        String id = tokenActual.getLexema();
        eat(ClaseLexica.ID);  // Comer el identificador

        // Verificar si es una llamada a función (siguiente token es un paréntesis de apertura)
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            eat(ClaseLexica.PARENTESIS_ABRE); // Comer el paréntesis de cierre
         //   System.out.println("Identificador '" + id + "' detectado como una llamada a función.");
            List<Integer> parametrosLlamada = parametros(); // Procesar los parámetros
            eat(ClaseLexica.PARENTESIS_CIERRA); // Comer el paréntesis de cierre
            return llamada(id, parametrosLlamada); // Procesar la llamada a la función
        } else if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE || tokenActual.getClase() == ClaseLexica.PUNTO) {
            // Localización
            return localizacion(id);


        } else {
            // Identificador simple
            return getTipoVariable(id);
        }
        
    // Caso 3: Literales
    } else if (esLiteral(tokenActual.getClase())) {
        int tipo = getTipoLiteral(tokenActual.getClase()); // Obtener el tipo del literal
        eat(tokenActual.getClase());
       // System.out.println("Saliendo de primary con tipo de literal: " + tipo);
        return tipo;

    // Caso 4: Error si no es ninguno de los anteriores
    } else {
        error("Expresión no válida.");
        return -1; // Código inaccesible
    }
}


private int localizacion(String id) {
    System.out.println("Entrando en localizacion con id: " + id);

    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        return arreglo(id);
    } else if (tokenActual.getClase() == ClaseLexica.PUNTO) {
        return estructurado(id);
    } else {
        error("Se esperaba '[' o '.', pero se encontró: " + tokenActual.getClase());
        return -1;
    }
}


private int arreglo(String id) {
    eat(ClaseLexica.CORCHETE_ABRE);
    exp(); // Procesar la expresión dentro del corchete
    eat(ClaseLexica.CORCHETE_CIERRA);

    // Procesar dimensiones adicionales
    return arreglo_prima(id);
}

private int arreglo_prima(String id) {
    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        eat(ClaseLexica.CORCHETE_ABRE);
        exp(); // Procesar la expresión dentro del corchete
        eat(ClaseLexica.CORCHETE_CIERRA);
        return arreglo_prima(id); // Recursión
    }
    return 0; // Finaliza el procesamiento del arreglo
}



private int estructurado(String id) {
    eat(ClaseLexica.PUNTO);

    // Obtener el primer campo después del punto
    String campo = tokenActual.getLexema();
    eat(ClaseLexica.ID);
    System.out.println("EL PRIMER CAMPO ES: " + campo);

    // Verificar que el identificador inicial (`id`) es una estructura
    Symbol simbolo = stackSymbolTable.lookup(id);
    if (simbolo == null) {
        error("El identificador '" + id + "' no está declarado.");
    }

    int tipoCampo = simbolo.getType();
    String structName = "struct_" + tipoCampo;

    // Verificar si el tipo corresponde a un struct registrado
    SymbolTable structTable = structTables.get(structName);
    if (structTable == null) {
        error("El tipo '" + structName + "' no está registrado como struct.");
    } else {
        System.out.println("Tabla de símbolos para " + structName);
        imprimirTablaDeSimbolos(structTable);
    }

    // Buscar el primer campo en el struct
    Symbol siguienteCampo = structTable.getSymbolSecure(campo);
    if (siguienteCampo == null) {
        error("El campo '" + campo + "' no está definido en el struct '" + structName + "'.");
    }

    System.out.println("Siguiente campo es: "+ "struct_" + siguienteCampo.getType());

    // Continuar con accesos adicionales si los hay
    return estructurado_prima(siguienteCampo);
}

private int estructurado_prima(Symbol simboloActual) {
    
    if (tokenActual.getClase() == ClaseLexica.PUNTO) {
        System.out.println("Estructurado prima: " + "struct_" + simboloActual.getType());
        eat(ClaseLexica.PUNTO);

        // Obtener el campo que sigue al punto
        String campo = tokenActual.getLexema();
        System.out.println("Procesando campo: " + campo);
        eat(ClaseLexica.ID);

        // Obtener el tipo del struct actual
        int tipoActual = simboloActual.getType();
        String structName = "struct_" + tipoActual;

        // Verificar si el tipo corresponde a un struct registrado
        SymbolTable structTable = structTables.get(structName);
        if (structTable == null) {
            error("El tipo '" + structName + "' no está registrado como struct.");
        } else {
            System.out.println("Tabla de símbolos para " + structName);
            imprimirTablaDeSimbolos(structTable);
        }

        // Buscar el campo actual en la tabla del struct correspondiente
        Symbol siguienteCampo = structTable.getSymbolSecure(campo);
        if (siguienteCampo == null) {
            //structName= "struct_9";
            // Verificar si el tipo corresponde a un struct registrado
        SymbolTable structTableAux = structTables.get(structName);
        if (structTableAux == null) {
            imprimirTablasDeStructs();
            error("El tipo '" + structName + "' no está registrado como struct.");
        } else {
            System.out.println("Tabla de símbolos para " + structName);
            imprimirTablaDeSimbolos(structTableAux);
        }
            imprimirTablaDeTipos();
            error("El campo '" + campo + "' no está definido en el struct '" + structName + "'.");
        }

        // Continuar con accesos adicionales si los hay
        return estructurado_prima(siguienteCampo);
    }

    return simboloActual.getType(); // Finaliza el procesamiento de accesos estructurados
}


private Symbol buscarEnStructAnterior(String campo, int tipoAnterior) {
    // Verificar si el tipo anterior corresponde a un struct
    if (tipoAnterior != 9) {
        error("El tipo anterior no corresponde a un struct. Tipo encontrado: " + tipoAnterior);
        return null; // Error no recuperable
    }

    // Obtener el nombre del struct basado en su ID
    String structName = "struct_" + tipoAnterior;
    SymbolTable structTable = structTables.get(structName);

    if (structTable == null) {
        error("El struct '" + structName + "' no está registrado.");
        return null;
    }

    // Buscar el campo en la tabla del struct
    Optional<Symbol> simboloCampo = structTable.getSymbol(campo);
    if (simboloCampo.isEmpty()) {
        error("El campo '" + campo + "' no está definido en el struct '" + structName + "'.");
        return null;
    }

    return simboloCampo.get(); // Retorna el símbolo encontrado
}




// Función para obtener los parámetros de una llamada
private List<Integer> parametros() {
    List<Integer> tiposParametros = new ArrayList<>();

    // Si hay expresiones (argumentos), procesarlas
    if (esInicioExpresion()) {
        tiposParametros.add(exp()); // Primer parámetro
        lista_param_prima(tiposParametros); // Llamada a la producción lista_param'
    }

    
    return tiposParametros;
}

// Función para manejar lista_param' → , exp lista_param' | epsilon
private void lista_param_prima(List<Integer> tiposParametros) {
    if (tokenActual.getClase() == ClaseLexica.COMA) {
        eat(ClaseLexica.COMA);
        tiposParametros.add(exp()); // Añadir el siguiente parámetro
        lista_param_prima(tiposParametros); // Recursión para manejar más parámetros
    }
}

// Función para manejar llamadas a funciones
private int llamada(String idFuncion, List<Integer> parametrosLlamada) {
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

    // Verificar número de argumentos
    if (parametrosLlamada.size() != argumentosDeclarados.size()) {
        error("La función '" + idFuncion + "' espera " + argumentosDeclarados.size() +
              " argumentos, pero se pasaron " + parametrosLlamada.size() + ".");
    }

    // Verificar tipos de los argumentos
    for (int i = 0; i < parametrosLlamada.size(); i++) {
        int tipoArgumentoLlamada = parametrosLlamada.get(i);
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
    System.out.println("tipoIzquierdo:" + tipoIzquierdo + " tipoDerecho:" + tipoDerecho);

    // Operadores relacionales (==, !=, <, >, <=, >=)
    if (operacion == ClaseLexica.IGUAL || operacion == ClaseLexica.DIFERENTE ||
        operacion == ClaseLexica.MENOR || operacion == ClaseLexica.MENOR_IGUAL ||
        operacion == ClaseLexica.MAYOR || operacion == ClaseLexica.MAYOR_IGUAL) {
        if (tipoIzquierdo == tipoDerecho ) {
            return 6; // boolean
        } else {
            System.out.println("Error: Tipos incompatibles para el operador relacional.");
            return -1; // Error
        }
    }

    // Operadores condicionales (&&, ||)
    if (operacion == ClaseLexica.AND || operacion == ClaseLexica.OR) {
        if (tipoIzquierdo == 6 && tipoDerecho == 6) { // Ambos son boolean
            return 6; // boolean
        } else {
            System.out.println("Error: Tipos incompatibles para operadores condicionales.");
            return -1; // Error
        }
    }
// Operadores aritméticos (+, -, *, /)
if (operacion == ClaseLexica.MAS || operacion == ClaseLexica.MENOS ||
operacion == ClaseLexica.MULTIPLICACION || operacion == ClaseLexica.DIVISION) {
Type tipoPromovido = Type.getPromotedType(typeTable.getType(tipoIzquierdo), typeTable.getType(tipoDerecho));
if (tipoPromovido != null) {
    return tipoPromovido.getId(); // Retorna el tipo promovido
} else {
    error("Error: Tipos incompatibles para operadores aritméticos: " + 
          getTipoFromInt(tipoIzquierdo) + " y " + getTipoFromInt(tipoDerecho));
}
}

// Operador de asignación (=)
if (operacion == ClaseLexica.ASIGNACION) {
// Verificar si el tipo derecho puede ser promovido al tipo izquierdo
if (Type.canPromote(typeTable.getType(tipoDerecho), typeTable.getType(tipoIzquierdo))) {
    return tipoIzquierdo; // La asignación es válida y retorna el tipo del lado izquierdo
} else {
    error("Error: El tipo del lado derecho (" + getTipoFromInt(tipoDerecho) + 
          ") no puede ser promovido al tipo del lado izquierdo (" + getTipoFromInt(tipoIzquierdo) + ").");
}
}

// Caso predeterminado para otras operaciones
error("Operación no válida o tipos incompatibles: " + operacion);
return -1; // Código inaccesible
    
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
               clase == ClaseLexica.RUNE || // runa
               clase == ClaseLexica.COMPLEX || // complex
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
            case LITERAL_RUNA: return 5;
            case TRUE:
            case FALSE: return 6; // boolean
            case LITERAL_COMPLEJA: return 7; // complex
            default: return -1;
        }
    }
    

private boolean esLiteral(ClaseLexica clase) {
    return clase == ClaseLexica.LITERAL_ENTERA ||
           clase == ClaseLexica.LITERAL_FLOTANTE ||
           clase == ClaseLexica.LITERAL_DOUBLE ||
           clase == ClaseLexica.LITERAL_CADENA ||
           clase == ClaseLexica.LITERAL_RUNA ||
           clase == ClaseLexica.LITERAL_COMPLEJA ||
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
     if ("struct".equals(tipo)) {
        return 9;
    }
    switch (tipo) {
        case "int": return 1;
        case "float": return 2;
        case "double": return 3;
        case "string": return 4;
        case "rune": return 5;
        case "boolean": return 6;
        case "complex": return 7;
        case "struct": return 9;
        case "void": return 0;
        default: error("Tipo desconocido: " + tipo); return -1;
    }
}


private String getTipoFromInt(int tipo) {
    if (tipo > 7 && tipo <= typeTable.size()) {
        return "struct";
    }
    switch (tipo) {
        case 1: return "int";
        case 2: return "float";
        case 3: return "double";
        case 4: return "string";
        case 5: return "rune";
        case 6: return "boolean";
        case 7: return "complex";
        case 0: return "void";
        default: error("Tipo desconocido: " + tipo); return "desconocido";
    }
}


// Función para obtener el tipo de una variable (cuando no es una llamada a función)
private int getTipoVariable(String id) {
    Symbol simbolo = stackSymbolTable.lookup(id);
    if (simbolo == null) {
        imprimirTablaDeSimbolos(stackSymbolTable.peek());
        error("Identificador no declarado: " + id);
    }
    return simbolo.getType(); // Retorna el tipo de la variable
}

// Inicializar la tabla de tipos
private void inicializarTypeTable() {
    typeTable = new TypeTable();
    
    // Registrar tipos básicos
    typeTable.addType(0, 0, 0); // void
    typeTable.addType(1, 0, 3); // int
    typeTable.addType(2, 0, 3); // float
    typeTable.addType(3, 0, 3); // double
    typeTable.addType(4, 0, 4); // string
    typeTable.addType(5, 0, 5); // runa
    typeTable.addType(6, 0, 6); // boolean
    typeTable.addType(7, 0, 7); // complex
    

   
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


public Map<String, Integer> getStructMembers(int structId) {
    Type structType = typeTable.getType(structId);
    return structType != null ? structType.getMembers() : null;
}


private void imprimirTablaDeTipos() {
    System.out.println("Tabla de Tipos:");

    // Iterar sobre todos los IDs en la tabla de tipos
    for (int id : typeTable.getAllIds()) {
        Type tipo = typeTable.getType(id);

        if (tipo != null) {
            System.out.println("ID: " + id + ", " + tipo);
        } else {
            System.out.println("ID: " + id + " está vacío o no es válido.");
        }
    }
}

private void imprimirTablasDeStructs() {
    System.out.println("Tablas de Structs:");

    // Iterar sobre las claves (nombres de los structs) en el mapa
    for (String structName : structTables.keySet()) {
        System.out.println("Struct: " + structName);

        // Obtener la tabla de símbolos asociada a la clave
        SymbolTable structTable = structTables.get(structName);

        // Imprimir la tabla de símbolos del struct
        if (structTable != null) {
            imprimirTablaDeSimbolos(structTable);
        } else {
            System.out.println("Tabla vacía o no registrada para " + structName);
        }

        System.out.println("-------------------------------");
    }
}

private int calcularTotalItems(String dimensiones) {
    int total = 1;
    for (char c : dimensiones.toCharArray()) {
        total *= Character.getNumericValue(c); // Multiplica el valor numérico de cada dígito
    }
    return total;
}


}
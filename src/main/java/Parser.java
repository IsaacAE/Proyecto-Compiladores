package main.java;

//Importación de bibliotecas necesarias
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Optional;

//Importación del Lexer desarrollado
import main.jflex.Lexer;

//Clase para realizar el parseo de un archivo de texto
public class Parser {
    private Lexer lexer; //instancia del lexer
    private Token tokenActual; //token revisado actualmente

    private ArbolSemantico arbolSemantico; //árbol de análisis semántico
    private NodoArbol nodoActual; // nodo actual del árbol de análisis semántico

    private Symbol simboloEstructurado = null; // Variable global para almacenar el último símbolo estructurado

    private SymbolTableStack stackSymbolTable = new SymbolTableStack(); //pila de tabla de símbolos
    private TypeTable typeTable = new TypeTable(); //tabla de tipos
    private Map<String, SymbolTable> structTables = new HashMap<>(); //hashmap que relaciona una tabla de símbolos con un struct
    private List<Map.Entry<String, Symbol>> prototiposGlobales = new ArrayList<>(); //lista de prototipos globales declarados



    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.arbolSemantico = new ArbolSemantico(null);
    }

    
    /**
 * Consume el token actual si coincide con la clase léxica esperada.
 * Avanza al siguiente token si la comparación es exitosa.
 * 
 * @param claseEsperada La clase léxica que se espera encontrar en el token actual.
 */
private void eat(ClaseLexica claseEsperada) {
    // Verificar si el token actual coincide con la clase esperada
    if (tokenActual.getClase() == claseEsperada) {
        try {
            // Avanzar al siguiente token usando el analizador léxico (lexer)
            tokenActual = lexer.yylex();
        } catch (IOException ioe) {
            // Manejar errores de entrada/salida al intentar leer el siguiente token
            System.err.println("Error al leer el siguiente token");
        }
    } else {
        // Generar un error si el token actual no coincide con la clase esperada
        error("Se esperaba: " + claseEsperada + ", pero se encontró: " + tokenActual.getClase());
    }
}


   /**
 * Lanza un error crítico con un mensaje específico.
 * 
 * Este método se utiliza para manejar errores durante el análisis léxico o sintáctico,
 * deteniendo la ejecución del programa y proporcionando información sobre el problema.
 *
 * @param mensaje El mensaje que describe el error ocurrido.
 */
private void error(String mensaje) {
    // Lanza una excepción de tipo RuntimeException con el mensaje del error
    throw new RuntimeException("Error: " + mensaje);
}


   /**
 * Método principal del analizador sintáctico.
 * 
 * Este método realiza las siguientes acciones:
 * 1. Inicializa el análisis léxico y obtiene el primer token.
 * 2. Configura la tabla global de símbolos y la tabla de tipos.
 * 3. Llama al método principal de la gramática (`programa`).
 * 4. Verifica que toda la entrada haya sido procesada correctamente (EOF).
 * 5. Valida los prototipos de funciones, asigna direcciones a las variables y genera los archivos de salida.
 */
public void parse() {
    try {
        // Obtener el primer token de la entrada usando el analizador léxico
        this.tokenActual = lexer.yylex();
    } catch (IOException ioe) {
        // Manejar errores de entrada/salida al intentar leer el primer token
        System.err.println("Error: No fue posible obtener el primer token de la entrada.");
        System.exit(1); // Terminar el programa si ocurre un error crítico
    }

    // Inicializar la tabla global de símbolos en la pila
    stackSymbolTable.push(new SymbolTable());
    // Inicializar la tabla de tipos
    inicializarTypeTable();
    // Procesar el programa usando la gramática
    programa();

    // Verificar si se alcanzó el final del archivo (EOF)
    if (this.tokenActual.getClase() == ClaseLexica.EOF) {
        // Validar que los prototipos definidos coincidan con las funciones implementadas
        validarPrototiposConFunciones(stackSymbolTable.base());
        System.out.println("La cadena es aceptada");

        // Asignar direcciones de memoria a las variables en la tabla global de símbolos
        int direccion = 0;
        for (Symbol symbol : stackSymbolTable.base().getAllSymbols()) {
            symbol.setAddress(direccion);
            // Incrementar la dirección usando el tamaño del tipo asociado
            Type t = typeTable.getType(symbol.getType());
            direccion += t.getTam(); 
        }

        // TO DO: Asignar direcciones a las variables en tablas de símbolos locales (no globales)
        
        // Generar archivos de salida:
        // 1. Árbol semántico
        guardarArbolSemanticoEnArchivo(arbolSemantico, "ASA.txt");
        // 2. Tabla de símbolos
        guardarTablaDeSimbolosEnArchivo(stackSymbolTable.base(), "TablaDeSimbolos.txt");
        // 3. Tabla de tipos
        guardarTablaDeTiposEnArchivo("TablaDeTipos.txt");
    } else {
        // Si no se alcanzó EOF, se lanza un error indicando que faltó procesar parte de la entrada
        error("Se esperaba el final del archivo");
    }
}


    // Producción principal
    private void programa() {
        NodoArbol nodoPrograma = new NodoArbol("PROGRAMA", null);
        arbolSemantico.setRaiz(nodoPrograma);
        nodoActual = nodoPrograma;

        decl_proto();
        decl_var();
        decl_func();
    }

    private void decl_proto() {
        // Mientras el token actual sea de clase PROTO, se procesa el prototipo
        while (tokenActual.getClase() == ClaseLexica.PROTO) {
            eat(ClaseLexica.PROTO); // Consumir el token 'PROTO'
    
            // Obtener el tipo de retorno del prototipo
            int tipoRetorno = tipo();
    
            // Obtener el identificador (nombre) del prototipo
            String idPrototipo = tokenActual.getLexema();
            eat(ClaseLexica.ID); // Consumir el token identificador
    
            // Crear un nodo en el árbol semántico para el prototipo
            NodoArbol nodoPrototipo = new NodoArbol("PROTO", idPrototipo);
            arbolSemantico.agregarHijo(nodoActual, nodoPrototipo);
            nodoActual = nodoPrototipo; // Actualizar el nodo actual al nodo del prototipo
    
            // Obtener la tabla de símbolos global
            SymbolTable tablaGlobal = stackSymbolTable.base();
    
            // Verificar si ya existe un prototipo con el mismo ID en la tabla global
            Optional<Symbol> simboloExistenteOpt = tablaGlobal.getSymbol(idPrototipo);
            if (simboloExistenteOpt.isPresent() && "prototipo".equals(simboloExistenteOpt.get().getCat())) {
                // Si el prototipo ya está declarado, generar un error
                error("El prototipo '" + idPrototipo + "' ya está declarado en la tabla global.");
            }
    
            eat(ClaseLexica.PARENTESIS_ABRE); // Consumir el paréntesis de apertura '('
    
            // Crear una nueva tabla de símbolos para los argumentos del prototipo
            SymbolTable tablaPrototipo = new SymbolTable();
            stackSymbolTable.push(tablaPrototipo); // Agregar la tabla de símbolos a la pila
    
            // Procesar los argumentos del prototipo y almacenarlos en una lista
            List<String> argumentos = argumentos();
    
            // Registrar el prototipo en la tabla global con su tipo de retorno y lista de argumentos
            Symbol simboloPrototipo = new Symbol(-1, tipoRetorno, "prototipo", argumentos);
            tablaGlobal.addSymbol(idPrototipo, simboloPrototipo); // Agregar el prototipo a la tabla global
    
            // Guardar el prototipo en la lista global de prototipos
            prototiposGlobales.add(Map.entry(idPrototipo, simboloPrototipo));
    
            eat(ClaseLexica.PARENTESIS_CIERRA); // Consumir el paréntesis de cierre ')'
            eat(ClaseLexica.PUNTO_Y_COMA);      // Consumir el punto y coma ';'
    
            // Retirar la tabla de símbolos del prototipo de la pila
            stackSymbolTable.pop();
    
            // Volver al nodo padre en el árbol semántico
            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
        }
    }
    
    

    // Producción decl_var: Declaración de variables
private void decl_var() {
    // Mientras el token actual sea un tipo válido, se procesa la declaración
    while (esTipo(tokenActual.getClase())) {
        // Obtener el tipo de la variable
        int tipo = tipo();

        // Manejo de struct anónimo (cuando el token actual es '}')
        if (tokenActual.getClase() == ClaseLexica.LLAVE_CIERRA) {
            eat(ClaseLexica.LLAVE_CIERRA); // Consumir la llave de cierre
            tipo = 9; // El tipo 9 representa un struct anónimo
        }

        // System.out.println("El tipo de las siguientes variables es: " + tipo);

        // Obtener la lista de variables declaradas
        List<String> variables = lista_var();
        eat(ClaseLexica.PUNTO_Y_COMA); // Consumir el punto y coma ';' al final de la declaración

        // Convertir el número del tipo a String
        String numeroStr = String.valueOf(tipo);

        // Si el tipo es menor que -1, se maneja como puntero o arreglo
        if (tipo < -1) {
            // Verificar si el tipo es un puntero (el segundo carácter del número es '8')
            if (numeroStr.charAt(1) == '8') {
                // Registrar cada variable como un puntero en la tabla de símbolos actual
                for (String var : variables) {
                    Symbol varSymbol = new Symbol(-1, tipo, "puntero", null);
                    agregarSimbolo(var, varSymbol);

                    // Crear un nodo en el árbol semántico para la variable
                    NodoArbol nodoVar = new NodoArbol("VAR", var);
                    arbolSemantico.agregarHijo(nodoActual, nodoVar);
                    arbolSemantico.anotarNodo(nodoVar, "Tipo: Puntero a " + getTipoFromInt(tipo));
                }
            } else {
                // Registrar cada variable como un arreglo en la tabla de símbolos actual
                for (String var : variables) {
                    Symbol varSymbol = new Symbol(-1, tipo, "arreglo", null);
                    agregarSimbolo(var, varSymbol);

                    // Crear un nodo en el árbol semántico para la variable
                    NodoArbol nodoVar = new NodoArbol("VAR", var);
                    arbolSemantico.agregarHijo(nodoActual, nodoVar);
                    arbolSemantico.anotarNodo(nodoVar, "Tipo: Arreglo de " + getTipoFromInt(tipo));
                }
            }
        } else {
            // Si no es un puntero o arreglo, registrar como una variable normal
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "variable", null);
                agregarSimbolo(var, varSymbol);

                // Crear un nodo en el árbol semántico para la variable
                NodoArbol nodoVar = new NodoArbol("VAR", var);
                arbolSemantico.agregarHijo(nodoActual, nodoVar);
                arbolSemantico.anotarNodo(nodoVar, "Tipo: " + getTipoFromInt(tipo));
            }
        }
    }
}


    // Producción decl_func: Declaración de funciones
private void decl_func() {
    // Verificar si el token actual es 'FUNC' para procesar una declaración de función
    if (tokenActual.getClase() == ClaseLexica.FUNC) {
        eat(ClaseLexica.FUNC); // Consumir el token 'FUNC'

        // Obtener el tipo de retorno de la función
        int tipoRetorno = tipo();

        // Obtener el nombre de la función
        String idFuncion = tokenActual.getLexema();
        eat(ClaseLexica.ID); // Consumir el token identificador de la función

        // Crear un nodo en el árbol semántico para la función
        NodoArbol nodoFuncion = new NodoArbol("FUNC", idFuncion);
        arbolSemantico.agregarHijo(nodoActual, nodoFuncion);
        nodoActual = nodoFuncion; // Actualizar el nodo actual al nodo de la función

        // Obtener la tabla de símbolos global
        SymbolTable tablaGlobal = stackSymbolTable.base();

        // Verificar si ya existe una función con el mismo ID en la tabla global
        Optional<Symbol> simboloExistenteOpt = tablaGlobal.getSymbol(idFuncion);
        if (simboloExistenteOpt.isPresent() && "funcion".equals(simboloExistenteOpt.get().getCat())) {
            error("El prototipo '" + idFuncion + "' ya está declarado en la tabla global.");
        }

        eat(ClaseLexica.PARENTESIS_ABRE); // Consumir el paréntesis de apertura '('

        // Crear una nueva tabla de símbolos para el ámbito local de la función
        SymbolTable tablaFuncion = new SymbolTable();
        stackSymbolTable.push(tablaFuncion); // Agregar la tabla local a la pila

        // Procesar los argumentos de la función y registrarlos
        List<String> argumentos = argumentos();

        // Crear el símbolo de la función con su tipo de retorno y argumentos
        Symbol simboloFuncion = new Symbol(-1, tipoRetorno, "funcion", argumentos);
        
        // Registrar la función en la tabla de símbolos global
        tablaGlobal.addSymbol(idFuncion, simboloFuncion);
        
        // Registrar la función en la tabla de símbolos local (para el ámbito de la función)
        SymbolTable tablaLocal = stackSymbolTable.peek();
        tablaLocal.addSymbol(idFuncion, simboloFuncion);

        // System.out.println("Función '" + idFuncion + "' agregada a la tabla de símbolos global.");

        eat(ClaseLexica.PARENTESIS_CIERRA); // Consumir el paréntesis de cierre ')'

        // Procesar el bloque de instrucciones de la función
        bloque();


        // Volver al nodo padre en el árbol semántico después de procesar la función
        nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

    }

    // Procesar funciones adicionales (si hay más definiciones de funciones)
    decl_func_prima();
}

// Producción decl_func_prima: Declaración de funciones adicionales
private void decl_func_prima() {
    // Si el token actual es 'FUNC', se procesa otra declaración de función
    if (tokenActual.getClase() == ClaseLexica.FUNC) {
        decl_func();
    }
}

    
    
// Función tipo: Identifica y procesa el tipo de una variable o función
private int tipo() {
    int tipoBase = -1; // Valor por defecto para el tipo

    // Verificar si el token actual es un tipo básico
    if (esTipoBasico(tokenActual.getClase())) {
        // Procesar tipo básico
        tipoBase = basico(); // Identificar el tipo básico
        tipoBase = tipo_prima(tipoBase); // Manejar tipos compuestos (arreglos), si los hay
    } 
    // Manejar definición de un struct
    else if (tokenActual.getClase() == ClaseLexica.STRUCT) {
        eat(ClaseLexica.STRUCT); // Consumir el token 'STRUCT'

        // Crear un ID único para el struct basado en el tamaño actual de la tabla de tipos
        int structId = typeTable.size();
        String structName = "struct_" + structId; // Nombre único del struct

        // Registrar el struct en la tabla de tipos con valores por defecto
        typeTable.addTypeStruct(structId, 0, 0, null);

        // Inicializar el tipo base como el ID del struct
        tipoBase = structId;

        // Crear una nueva tabla de símbolos para el ámbito del struct
        SymbolTable structTable = new SymbolTable();
        stackSymbolTable.push(structTable); // Agregar la tabla a la pila

        eat(ClaseLexica.LLAVE_ABRE); // Consumir '{'
        decl_var(); // Procesar las variables dentro del struct
        eat(ClaseLexica.LLAVE_CIERRA); // Consumir '}'

        // Registrar la tabla de símbolos del struct en el HashMap
        structTables.put(structName, stackSymbolTable.pop());
        actualizarDatosStruct(structName); // Actualizar los datos del struct en la tabla de tipos
    } 
    // Manejar punteros
    else if (tokenActual.getClase() == ClaseLexica.PTR) {
        int tipoBasico = puntero(); // Procesar el tipo del puntero

        // Construir el identificador del puntero como una cadena
        String tipoPtr = "-8" + Integer.toString(tipoBasico);
        tipoBase = Integer.valueOf(tipoPtr); // Convertir a entero

        // Obtener información del tipo base
        int tam = typeTable.getTam(tipoBasico);
        int item = typeTable.getItems(tipoBasico);
        int parent = typeTable.getParent(tipoBasico);

        // Registrar el tipo puntero en la tabla de tipos
        typeTable.addTypeStruct(tipoBase, item, tam, parent);
    } 
    // Si no es un tipo válido, lanzar un error
    else {
        error("Se esperaba un tipo válido.");
        tipoBase = -1;
    }

    return tipoBase; // Devolver el tipo identificado
}

// Función tipo_prima: Maneja tipos compuestos (arreglos) si los hay
private int tipo_prima(int tipoBase) {
    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        tipoBase = compuesto(tipoBase); // Procesar el tipo compuesto (arreglo)
    }
    return tipoBase; // Retornar el tipo final (básico o compuesto)
}

// Función compuesto: Procesa el primer nivel de un tipo compuesto (arreglo)
private int compuesto(int tipoBase) {
    eat(ClaseLexica.CORCHETE_ABRE); // Consumir '['

    // Inicializar la cadena que representará las dimensiones
    String dimensiones = "";

    // Verificar si el token actual es un literal entero (tamaño del arreglo)
    if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
        int dimension = Integer.parseInt(tokenActual.getLexema());
        eat(ClaseLexica.LITERAL_ENTERA); // Consumir el literal entero
        dimensiones += dimension; // Agregar la dimensión inicial
    } else {
        error("Se esperaba un literal entero como tamaño del arreglo.");
    }

    eat(ClaseLexica.CORCHETE_CIERRA); // Consumir ']'

    // Pasar el tipo base y las dimensiones acumuladas a compuesto_prima
    return compuesto_prima(tipoBase, dimensiones);
}

// Función compuesto_prima: Procesa dimensiones adicionales para tipos compuestos
private int compuesto_prima(int tipoBase, String dimensiones) {
    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        eat(ClaseLexica.CORCHETE_ABRE); // Consumir '['

        // Verificar si el token actual es un literal entero
        if (tokenActual.getClase() == ClaseLexica.LITERAL_ENTERA) {
            int dimension = Integer.parseInt(tokenActual.getLexema());
            eat(ClaseLexica.LITERAL_ENTERA); // Consumir el literal entero
            dimensiones += dimension; // Concatenar la nueva dimensión
        } else {
            error("Se esperaba un literal entero como tamaño del arreglo.");
        }

        eat(ClaseLexica.CORCHETE_CIERRA); // Consumir ']'

        // Recursión para manejar más dimensiones
        return compuesto_prima(tipoBase, dimensiones);
    }

    // Construir el identificador único del tipo compuesto
    String tipoCompuestoId = "-" + tipoBase + dimensiones;

    // Calcular el número total de elementos multiplicando las dimensiones
    int totalItems = calcularTotalItems(dimensiones);
    int idCompuesto = Integer.parseInt(tipoCompuestoId);

    // Obtener el tipo padre del tipo base
    Integer tipoPadre = typeTable.getType(tipoBase).getParent();

    // Obtener el tamaño del tipo base
    Type tipoBaseType = typeTable.getType(tipoBase);
    int tamBase = tipoBaseType.getTam();

    // Calcular el tamaño total del arreglo
    int tamTotal = totalItems * tamBase;

    // Registrar el tipo compuesto en la tabla si no existe ya
    if (!typeTable.contains(idCompuesto)) {
        typeTable.addTypeArray(idCompuesto, totalItems, tamTotal, tipoPadre);
    }

    return idCompuesto; // Retornar el ID del tipo compuesto
}

// Función basico: Identifica tipos básicos como int, float, etc.
private int basico() {
    int tipo;
    switch (tokenActual.getClase()) {
        case INT: tipo = 1; break;
        case FLOAT: tipo = 2; break;
        case DOUBLE: tipo = 3; break;
        case STRING: tipo = 4; break;
        case RUNE: tipo = 5; break;
        case TRUE: tipo = 6; break;
        case FALSE: tipo = 6; break;
        case COMPLEX: tipo = 7; break;
        case VOID: tipo = 0; break;
        default: tipo = -1; // Tipo desconocido
    }
    eat(tokenActual.getClase()); // Consumir el token actual
    return tipo; // Devolver el tipo básico identificado
}

private int puntero() {
    eat(ClaseLexica.PTR);
    return basico();
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

// Producción argumentos
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

// Producción bloque
private void bloque() {
    eat(ClaseLexica.LLAVE_ABRE);
    decl_var();
    instrucciones();
    eat(ClaseLexica.LLAVE_CIERRA);
}

// Producción instrucciones
private void instrucciones() {
    while (esInicioSentencia()) {
        sentencia();
    }
}

   
    private void sentencia() {
        //System.out.println("Entrando a sentencia");
        int compatibilidad;
        if (tokenActual.getClase() == ClaseLexica.ID) {
            // Guardar el lexema del identificador para buscarlo en la tabla de símbolos
                 String lexemaId = tokenActual.getLexema();
                // Manejar parte izquierda
                int tipoIzquierdo = parteIzquierda(); // Obtener el tipo de la parte izquierda

                String tipoStr = String.valueOf(tipoIzquierdo);
                
            if (tokenActual.getClase() == ClaseLexica.ASIGNACION) {
                eat(ClaseLexica.ASIGNACION);
                TipoValor t = exp(true);
                int tipoExpresion = t.getTipo();
               // System.out.println(tipoIzquierdo);
               // System.out.println(tipoExpresion);
               
                String valorExpresion = t.getValor();


                 // Validar compatibilidad entre la parte izquierda y la expresión
                 compatibilidad= validarCompatibilidadTipos(tipoIzquierdo, tipoExpresion, ClaseLexica.ASIGNACION );
                 if(compatibilidad== -1){
                    error("Incompatibilidad para asignación ");
                 }
                    // Buscar el símbolo en la pila de tablas de símbolos y actualizar su valor
                Symbol simbolo = stackSymbolTable.lookup(lexemaId);
                if (simbolo != null) {
                    
                   // System.out.println(valorExpresion);
                    if(simboloEstructurado== null){
                    simbolo.setValue(valorExpresion);
                    }else{  
                        if (simboloEstructurado.getType() >8){
                            error("No se puede asignar un valor a un elemento de tipo struct");
                        }
                        simboloEstructurado.setValue(valorExpresion);
                    }
                    System.out.println("El valor de '" + lexemaId + "' ha sido actualizado a: " + valorExpresion);
                } else {
                    error("El identificador '" + lexemaId + "' no está declarado.");
                }
                eat(ClaseLexica.PUNTO_Y_COMA);
               // System.out.println("Asignación procesada correctamente.");
            } else {
                error("Se esperaba '=' después de la parte izquierda.");
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.IF) {
            // Sentencia if
            eat(ClaseLexica.IF);
            NodoArbol nodoIf = new NodoArbol("IF", null);
            arbolSemantico.agregarHijo(nodoActual, nodoIf);
            nodoActual = nodoIf;

            eat(ClaseLexica.PARENTESIS_ABRE);

            NodoArbol nodoCondicion = new NodoArbol("CONDICION", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCondicion);
            nodoActual = nodoCondicion;

            TipoValor t = exp(false);
            int tipoCondicion = t.getTipo();
             
            //if (tipoCondicion != 5) { // 5 representa boolean
               // error("La condición del 'if' debe ser de tipo boolean.");
           // }
            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

            eat(ClaseLexica.PARENTESIS_CIERRA);

            NodoArbol nodoThen = new NodoArbol("THEN", null);
            arbolSemantico.agregarHijo(nodoActual, nodoThen);
            nodoActual = nodoThen;

            sentencia();

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

            if (tokenActual.getClase() == ClaseLexica.ELSE) {
                eat(ClaseLexica.ELSE);

                NodoArbol nodoElse = new NodoArbol("ELSE", null);
                arbolSemantico.agregarHijo(nodoActual, nodoElse);
                nodoActual = nodoElse;

                sentencia();

                nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
            }
    
        } else if (tokenActual.getClase() == ClaseLexica.WHILE) {
            // Sentencia while
            eat(ClaseLexica.WHILE);

            NodoArbol nodoWhile = new NodoArbol("WHILE", null);
            arbolSemantico.agregarHijo(nodoActual, nodoWhile);
            nodoActual = nodoWhile;

            eat(ClaseLexica.PARENTESIS_ABRE);

            NodoArbol nodoCondicion = new NodoArbol("CONDICION", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCondicion);
            nodoActual = nodoCondicion;

            TipoValor t = exp(false);
            int tipoCondicion = t.getTipo();
           
            if (tipoCondicion == 4 ) {
                error("La condición del 'while' debe ser de tipo boolean.");
            }

            eat(ClaseLexica.PARENTESIS_CIERRA);

            NodoArbol nodoCuerpo = new NodoArbol("CUERPO", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCuerpo);
            nodoActual = nodoCuerpo;

            sentencia();

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
    
        } else if (tokenActual.getClase() == ClaseLexica.DO) {
            // Sentencia do-while
            eat(ClaseLexica.DO);

            NodoArbol nodoDoWhile = new NodoArbol("DO-WHILE", null);
            arbolSemantico.agregarHijo(nodoActual, nodoDoWhile);
            nodoActual = nodoDoWhile;

            NodoArbol nodoCuerpo = new NodoArbol("CUERPO", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCuerpo);
            nodoActual = nodoCuerpo;
            
            sentencia();

            eat(ClaseLexica.WHILE);
            eat(ClaseLexica.PARENTESIS_ABRE);

            NodoArbol nodoCondicion = new NodoArbol("CONDICION", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCondicion);
            nodoActual = nodoCondicion;

            TipoValor t = exp(false);
            int tipoCondicion = t.getTipo();
            if (tipoCondicion == 4) {
                error("La condición del 'do-while' debe ser de tipo boolean.");
            }

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
            
            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

        } else if (tokenActual.getClase() == ClaseLexica.BREAK) {
            // Sentencia break

            eat(ClaseLexica.BREAK);
            eat(ClaseLexica.PUNTO_Y_COMA);
    
        } else if (tokenActual.getClase() == ClaseLexica.RETURN) {
            // Sentencia return
            eat(ClaseLexica.RETURN);
            int tipoReturn = 0; // Asumimos que void es por defecto
            if (esInicioExpresion()) {
                TipoValor t = exp(true);
                tipoReturn = t.getTipo();
                
            }
            eat(ClaseLexica.PUNTO_Y_COMA);
    
            // Validar el tipo de retorno con el tipo de la función actual
            SymbolTable tablaActual = stackSymbolTable.base();
            Symbol funcionActual = obtenerFuncionActual(tablaActual);
            if (funcionActual == null) {
                error("No se encontró la función actual para validar el retorno.");
            }
            if (tipoReturn != funcionActual.getType()) { // 0 es void
                // Si los tipos no coinciden, verificar si ambos son structs (mayores a 7)
                if (tipoReturn > 7 && funcionActual.getType() > 7) {
                    // Construir los nombres de las tablas de símbolos basados en el tipo
                    String returnStructName = "struct_" + tipoReturn;
                    String funcStructName = "struct_" + funcionActual.getType();
            
                    // Verificar que ambos structs tienen tablas de símbolos asociadas
                    SymbolTable returnStructTable = structTables.get(returnStructName);
                    SymbolTable funcStructTable = structTables.get(funcStructName);
            
                    if (returnStructTable == null || funcStructTable == null) {
                        error("No se encontraron tablas de símbolos asociadas para los structs retornados: "
                                + returnStructName + " o " + funcStructName);
                    } else {
                        // Obtener los símbolos de ambas tablas
                        Set<String> returnStructIds = returnStructTable.getAllIds();
                        Set<String> funcStructIds = funcStructTable.getAllIds();
            
                        // Verificar si ambas tablas tienen los mismos IDs
                        if (returnStructIds.equals(funcStructIds)) {
                            boolean match = true;
                            for (String id : returnStructIds) {
                                Symbol returnSymbol = returnStructTable.getSymbolSecure(id);
                                Symbol funcSymbol = funcStructTable.getSymbolSecure(id);
            
                                // Comparar tipos de los símbolos
                                if (returnSymbol.getType() != funcSymbol.getType()) {
                                    match = false;
                                    error("El símbolo '" + id + "' tiene diferentes tipos en los structs: "
                                            + returnSymbol.getType() + " y " + funcSymbol.getType());
                                    break;
                                }
                            }
            
                            if (match) {
                                // Los structs tienen las mismas variables con los mismos tipos, el retorno es válido
                            }
                        } else {
                            error("Los structs retornados no tienen las mismas variables: "
                                    + returnStructName + " y " + funcStructName);
                        }
                    }
                } else {
                    // Si los tipos no coinciden y no son structs válidos, lanzar error
                    error("El tipo de retorno no coincide con el tipo de la función: esperado "
                            + funcionActual.getType() + ", encontrado " + tipoReturn);
                }
            }
            
    
        } else if (tokenActual.getClase() == ClaseLexica.SWITCH) {
            // Sentencia switch
            eat(ClaseLexica.SWITCH);

            NodoArbol nodoSwitch = new NodoArbol("SWITCH", null);
            arbolSemantico.agregarHijo(nodoActual, nodoSwitch);
            nodoActual = nodoSwitch;
            
            eat(ClaseLexica.PARENTESIS_ABRE);
            
            NodoArbol nodoExpresion = new NodoArbol("EXPRESION", null);
            arbolSemantico.agregarHijo(nodoActual, nodoExpresion);
            nodoActual = nodoExpresion;

            TipoValor t = exp(false);
            int tipoExpresion = t.getTipo();
            
            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);

            //System.out.println(tipoExpresion);
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.LLAVE_ABRE);
            casos(tipoExpresion); // Validar casos del switch con la expresión
            eat(ClaseLexica.LLAVE_CIERRA);
    
        } else if (tokenActual.getClase() == ClaseLexica.PRINT) {
            // Sentencia print
            eat(ClaseLexica.PRINT);
            TipoValor t = exp(false);
            int tipo = t.getTipo();
            
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

            NodoArbol nodoCaso = new NodoArbol("CASO", null);
            arbolSemantico.agregarHijo(nodoActual, nodoCaso);
            nodoActual = nodoCaso;

            TipoValor t = exp(false);
            int tipoCaso = t.getTipo();
            
            if (tipoCaso != tipoSwitch) {
                error("El tipo del caso no coincide con el tipo de la expresión del switch. "
                        + "Esperado: " + tipoSwitch + ", encontrado: " + tipoCaso);
            }
            eat(ClaseLexica.DOS_PUNTOS);
            instrucciones(); // Procesar las instrucciones del caso

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
        }
        if (tokenActual.getClase() == ClaseLexica.DEFAULT) {
            eat(ClaseLexica.DEFAULT);
            eat(ClaseLexica.DOS_PUNTOS);

            NodoArbol nodoDefault = new NodoArbol("DEFAULT", null);
            arbolSemantico.agregarHijo(nodoActual, nodoDefault);
            nodoActual = nodoDefault;

            instrucciones(); // Procesar las instrucciones del caso default

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
        }
    }
    

    private int parteIzquierda() {
        simboloEstructurado= null;
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
                
                TipoValor t = localizacion(id); // Procesar localización
                tipo = t.getTipo();
            }
    
            return tipo; // Retornar el tipo final
        } else {
            error("Se esperaba una parte izquierda válida.");
            return -1; // Código inaccesible
        }
    }
    
    
    
    
    private TipoValor exp(boolean evaluar) {
        return exp_or(evaluar);
    }
    
    private TipoValor exp_or(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_and(evaluar);
        return exp_or_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_or_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (tokenActual.getClase() == ClaseLexica.OR) {
            eat(ClaseLexica.OR);
            TipoValor tipoDerecho = exp_and(evaluar);
    
            
                int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), ClaseLexica.OR);
                if(evaluar){
                boolean resultado = Boolean.parseBoolean(tipoIzquierdo.getValor()) || Boolean.parseBoolean(tipoDerecho.getValor());
                tipoIzquierdo = new TipoValor(tipoResultado, String.valueOf(resultado));
            }
            tipoIzquierdo = new TipoValor(tipoResultado, "");
            
        }
        return tipoIzquierdo;
    }
    
    private TipoValor exp_and(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_eq(evaluar);
        return exp_and_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_and_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (tokenActual.getClase() == ClaseLexica.AND) {
            eat(ClaseLexica.AND);
            TipoValor tipoDerecho = exp_eq(evaluar);
    
            
                int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), ClaseLexica.AND);
               
               if(evaluar){
                boolean resultado = Boolean.parseBoolean(tipoIzquierdo.getValor()) && Boolean.parseBoolean(tipoDerecho.getValor());
                tipoIzquierdo = new TipoValor(tipoResultado, String.valueOf(resultado));
            }
            tipoIzquierdo = new TipoValor(tipoResultado, "");
            
        }
        return tipoIzquierdo;
    }
    
    private TipoValor exp_eq(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_rel(evaluar);
        return exp_eq_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_eq_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (tokenActual.getClase() == ClaseLexica.IGUAL || tokenActual.getClase() == ClaseLexica.DIFERENTE) {
            ClaseLexica operador = tokenActual.getClase();
            eat(operador);
            TipoValor tipoDerecho = exp_rel(evaluar);
    
                
                int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), operador);
                if(evaluar){
                    boolean resultado = evaluarOperacionRelacional(tipoIzquierdo.getValor(), tipoDerecho.getValor(), operador);
                    tipoIzquierdo = new TipoValor(tipoResultado, String.valueOf(resultado));
                }
                tipoIzquierdo = new TipoValor(tipoResultado, "");
            
        }
        return tipoIzquierdo;
    }
    
    private TipoValor exp_rel(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_add(evaluar);
        return exp_rel_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_rel_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (esOperadorRelacional(tokenActual.getClase())) {
            ClaseLexica operador = tokenActual.getClase();
            eat(operador);
            TipoValor tipoDerecho = exp_add(evaluar);
    
            
                int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), operador);
                if(evaluar){
                    boolean resultado = evaluarOperacionRelacional(tipoIzquierdo.getValor(), tipoDerecho.getValor(), operador);
                    tipoIzquierdo = new TipoValor(tipoResultado, String.valueOf(resultado));
                }

                tipoIzquierdo = new TipoValor(tipoResultado, "");
                   
            
        }

        return tipoIzquierdo;
    }
    
    private TipoValor exp_add(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_mul(evaluar);
        return exp_add_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_add_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (tokenActual.getClase() == ClaseLexica.MAS || tokenActual.getClase() == ClaseLexica.MENOS) {
            ClaseLexica operador = tokenActual.getClase();
            eat(operador);
            TipoValor tipoDerecho = exp_mul(evaluar);
            int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), operador);
            String valorResultado = "";
            if (evaluar) {
               
                valorResultado = realizarOperacionAritmetica(tipoIzquierdo.getValor(), tipoDerecho.getValor(), operador);
               
            }

            tipoIzquierdo = new TipoValor(tipoResultado, valorResultado);
        }
        return tipoIzquierdo;
    }
    
    private TipoValor exp_mul(boolean evaluar) {
        TipoValor tipoIzquierdo = exp_unary(evaluar);
        return exp_mul_prima(evaluar, tipoIzquierdo);
    }
    
    private TipoValor exp_mul_prima(boolean evaluar, TipoValor tipoIzquierdo) {
        while (tokenActual.getClase() == ClaseLexica.MULTIPLICACION ||
               tokenActual.getClase() == ClaseLexica.DIVISION ||
               tokenActual.getClase() == ClaseLexica.DIVISION_ENTERA ||
               tokenActual.getClase() == ClaseLexica.MODULO) {
            ClaseLexica operador = tokenActual.getClase();
            eat(operador);
            TipoValor tipoDerecho = exp_unary(evaluar);
    
            int tipoResultado = validarCompatibilidadTipos(tipoIzquierdo.getTipo(), tipoDerecho.getTipo(), operador);
            String valorResultado = "";
            if (evaluar) {
               
                valorResultado = realizarOperacionAritmetica(tipoIzquierdo.getValor(), tipoDerecho.getValor(), operador);
               
            }

            tipoIzquierdo = new TipoValor(tipoResultado, valorResultado);
        }
        return tipoIzquierdo;
    }
    
    private TipoValor exp_unary(boolean evaluar) {
        if (tokenActual.getClase() == ClaseLexica.MENOS || tokenActual.getClase() == ClaseLexica.NOT) {
            ClaseLexica operador = tokenActual.getClase();
            eat(operador);
            TipoValor tipoDerecho = exp_unary(evaluar);
    
            if (evaluar) {
                if (operador == ClaseLexica.MENOS) {
                    String valorDerecho = tipoDerecho.getValor();
                    if (esComplejo(valorDerecho)) {
                        // Para números complejos, anteponer el signo "-"
                        return new TipoValor(tipoDerecho.getTipo(), "-" + valorDerecho);
                    } else {
                        // Para números reales, aplicar el operador unario
                        double valor = Double.parseDouble(valorDerecho);
                        return new TipoValor(tipoDerecho.getTipo(), String.valueOf(-valor));
                    }
                } else if (operador == ClaseLexica.NOT) {
                    boolean valor = Boolean.parseBoolean(tipoDerecho.getValor());
                    return new TipoValor(tipoDerecho.getTipo(), String.valueOf(!valor));
                }
            }
        }
        return primary();
    }
    
    

private TipoValor primary() {
    if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
        eat(ClaseLexica.PARENTESIS_ABRE);
        TipoValor tipoValor = exp(true); // Evaluar la subexpresión
        eat(ClaseLexica.PARENTESIS_CIERRA);
        return tipoValor;

    } else if (tokenActual.getClase() == ClaseLexica.ID) {
        String id = tokenActual.getLexema();
        eat(ClaseLexica.ID);

        // Verificar si es una llamada a función
        if (tokenActual.getClase() == ClaseLexica.PARENTESIS_ABRE) {
            eat(ClaseLexica.PARENTESIS_ABRE);
            List<Integer> parametrosLlamada = parametros();
            eat(ClaseLexica.PARENTESIS_CIERRA);
            return llamada(id, parametrosLlamada);
        } else if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE || tokenActual.getClase() == ClaseLexica.PUNTO) {
            return localizacion(id);
        } else {
            Symbol simbolo = stackSymbolTable.lookup(id);
            return new TipoValor(getTipoVariable(id), simbolo.getValue());
        }

    } else if (esLiteral(tokenActual.getClase())) {
        int tipo = getTipoLiteral(tokenActual.getClase());
        String valor = tokenActual.getLexema();
        eat(tokenActual.getClase());
        return new TipoValor(tipo, valor);

    } else {
        error("Expresión no válida.");
        return new TipoValor(-1, "");
    }
}

private TipoValor localizacion(String id) {
    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        return arreglo(id);
    } else if (tokenActual.getClase() == ClaseLexica.PUNTO) {
        return estructurado(id);
    } else {
        error("Se esperaba '[' o '.', pero se encontró: " + tokenActual.getClase());
        return new TipoValor(-1, null);
    }
}

private TipoValor arreglo(String id) {
    eat(ClaseLexica.CORCHETE_ABRE);
    TipoValor tipoIndice = exp(true); // Procesar la expresión dentro del corchete

    // Convertir el valor a un entero válido
    int indiceActual = convertirAEntero(tipoIndice.getValor());

    eat(ClaseLexica.CORCHETE_CIERRA);

    int dimensiones = 1;
    // Obtener el valor en la posición igual a dimensiones + 2
    int tipo = getTipoVariable(id);
    String tipoIdStr = Integer.toString(tipo);
    char limiteCaracter = tipoIdStr.charAt(dimensiones + 1); // dimensiones + 2 en índice 0-based
    int limiteDimension = Character.getNumericValue(limiteCaracter);
    // Verificar si el índice actual excede el límite permitido
    if ( (indiceActual > limiteDimension - 1) || (indiceActual < 0)) {
        error("El índice " + indiceActual + " excede el límite permitido para la dimensión " + dimensiones + ": " + (limiteDimension - 1));
    }
    return arreglo_prima(id, dimensiones, indiceActual);
}

private TipoValor arreglo_prima(String id, int dimensiones, int indiceActual) {
    if (tokenActual.getClase() == ClaseLexica.CORCHETE_ABRE) {
        eat(ClaseLexica.CORCHETE_ABRE);
        TipoValor tipoIndice = exp(true);

        int nuevoIndice = convertirAEntero(tipoIndice.getValor());

        eat(ClaseLexica.CORCHETE_CIERRA);

        return arreglo_prima(id, dimensiones + 1, nuevoIndice);
    }

    int tipo = getTipoVariable(id);
    String tipoIdStr = Integer.toString(tipo);

    if (tipoIdStr.length() < dimensiones + 2) {
        error("El arreglo no tiene tantas dimensiones.");
    }

    // Obtener el valor en la posición igual a dimensiones + 2
    char limiteCaracter = tipoIdStr.charAt(dimensiones + 1); // dimensiones + 2 en índice 0-based
    int limiteDimension = Character.getNumericValue(limiteCaracter);

    // Verificar si el índice actual excede el límite permitido
    if ((indiceActual > limiteDimension - 1) || (indiceActual < 0)) {
        error("El índice " + indiceActual + " excede el límite permitido para la dimensión " + dimensiones + ": " + (limiteDimension - 1));
    }

       // Obtener el segundo carácter y convertirlo a un número
    char segundoCaracter = tipoIdStr.charAt(1);
    int tipoDesdeSegundoCaracter = Character.getNumericValue(segundoCaracter);

    // Devolver un TipoValor con el tipo obtenido y el id como valor
    return new TipoValor(tipoDesdeSegundoCaracter, id);
}





private TipoValor estructurado(String id) {
    eat(ClaseLexica.PUNTO);

    // Obtener el primer campo después del punto
    String campo = tokenActual.getLexema();
    eat(ClaseLexica.ID);
    //System.out.println("EL PRIMER CAMPO ES: " + campo);

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
        //System.out.println("Tabla de símbolos para " + structName);
        //imprimirTablaDeSimbolos(structTable);
    }

    // Buscar el primer campo en el struct
    Symbol siguienteCampo = structTable.getSymbolSecure(campo);
    if (siguienteCampo == null) {
        error("El campo '" + campo + "' no está definido en el struct '" + structName + "'.");
    }

    //System.out.println("Siguiente campo es: " + "struct_" + siguienteCampo.getType());

    // Continuar con accesos adicionales si los hay
    return estructurado_prima(siguienteCampo);
}

private TipoValor estructurado_prima(Symbol simboloActual) {
    if (tokenActual.getClase() == ClaseLexica.PUNTO) {
       // System.out.println("Estructurado prima: " + "struct_" + simboloActual.getType());
        eat(ClaseLexica.PUNTO);

        // Obtener el campo que sigue al punto
        String campo = tokenActual.getLexema();
        //System.out.println("Procesando campo: " + campo);
        eat(ClaseLexica.ID);

        // Obtener el tipo del struct actual
        int tipoActual = simboloActual.getType();
        String structName = "struct_" + tipoActual;

        // Verificar si el tipo corresponde a un struct registrado
        SymbolTable structTable = structTables.get(structName);
        if (structTable == null) {
            error("El tipo '" + structName + "' no está registrado como struct.");
        } else {
            //System.out.println("Tabla de símbolos para " + structName);
            //imprimirTablaDeSimbolos(structTable);
        }

        // Buscar el campo actual en la tabla del struct correspondiente
        Symbol siguienteCampo = structTable.getSymbolSecure(campo);
        if (siguienteCampo == null) {
            error("El campo '" + campo + "' no está definido en el struct '" + structName + "'.");
        }

        // Continuar con accesos adicionales si los hay
        return estructurado_prima(siguienteCampo);
    }
    simboloEstructurado = simboloActual;
    return new TipoValor(simboloActual.getType(), simboloActual.getValue()); // Finaliza el procesamiento de accesos estructurados
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
        TipoValor t = exp(false);
        
        tiposParametros.add(t.getTipo()); // Primer parámetro
        lista_param_prima(tiposParametros); // Llamada a la producción lista_param'
    }

    
    return tiposParametros;
}

// Función para manejar lista_param' → , exp lista_param' | epsilon
private void lista_param_prima(List<Integer> tiposParametros) {
    if (tokenActual.getClase() == ClaseLexica.COMA) {
        eat(ClaseLexica.COMA);
        TipoValor t = exp(false);
        
        tiposParametros.add(t.getTipo());// Añadir el siguiente parámetro
        lista_param_prima(tiposParametros); // Recursión para manejar más parámetros
    }
}

// Función para manejar llamadas a funciones
private TipoValor llamada(String idFuncion, List<Integer> parametrosLlamada) {
    // Recuperar la función de la tabla de símbolos global
    Symbol simboloFuncion = stackSymbolTable.lookup(idFuncion);
    if (simboloFuncion == null || ( !"funcion".equals(simboloFuncion.getCat()) && !"prototipo".equals(simboloFuncion.getCat())) ) {
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
    int tipo= simboloFuncion.getType(); // Retornar el tipo de retorno de la función
    return new TipoValor(tipo, "");
}


// Métodos auxiliares para validación y compatibilidad de tipos

private int validarCompatibilidadTipos(int tipoIzquierdo, int tipoDerecho, ClaseLexica operacion) {
   //System.out.println("tipoIzquierdo:" + tipoIzquierdo + " tipoDerecho:" + tipoDerecho);

   String tipoStrE = String.valueOf(tipoDerecho);

   String tipoStr = String.valueOf(tipoIzquierdo);

   // Verificar que el número es menor a -1 y tiene al menos tres caracteres (signo incluido)
   if (tipoIzquierdo < -1 ) {
       // Verificar si el segundo carácter es un '8'
       if (tipoStr.charAt(1) == '8') {
           // Asignar el valor del tercer carácter como nuevo valor
           tipoIzquierdo = Character.getNumericValue(tipoStr.charAt(2));
       }
   }

   // Verificar que el número es menor a -1 y tiene al menos tres caracteres (signo incluido)
   if (tipoDerecho < -1 ) {
       // Verificar si el segundo carácter es un '8'
       if (tipoStrE.charAt(1) == '8') {
           // Asignar el valor del tercer carácter como nuevo valor
           tipoDerecho = Character.getNumericValue(tipoStrE.charAt(2));
       }
   }


    // Operadores relacionales (==, !=, <, >, <=, >=)
    if (operacion == ClaseLexica.IGUAL || operacion == ClaseLexica.DIFERENTE ||
        operacion == ClaseLexica.MENOR || operacion == ClaseLexica.MENOR_IGUAL ||
        operacion == ClaseLexica.MAYOR || operacion == ClaseLexica.MAYOR_IGUAL) {
        if (tipoIzquierdo == tipoDerecho ) {
            return 6;
        }else if( (Type.canPromote(typeTable.getType(tipoDerecho), typeTable.getType(tipoIzquierdo))) || (Type.canPromote(typeTable.getType(tipoIzquierdo), typeTable.getType(tipoDerecho)))){ 
            return 6;
        } else {
            error("Error: Tipos incompatibles para operadores relacionales: " + 
                  getTipoFromInt(tipoIzquierdo) + " y " + getTipoFromInt(tipoDerecho));
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
operacion == ClaseLexica.MULTIPLICACION || operacion == ClaseLexica.DIVISION || operacion == ClaseLexica.DIVISION_ENTERA) {
Type tipoPromovido = Type.getPromotedType(typeTable.getType(tipoIzquierdo), typeTable.getType(tipoDerecho));
if (tipoPromovido != null) {
    return tipoPromovido.getId(); // Retorna el tipo promovido
} else {
    error("Error: Tipos incompatibles para operadores aritméticos: " + 
          getTipoFromInt(tipoIzquierdo) + " y " + getTipoFromInt(tipoDerecho));
}
}

if(operacion == ClaseLexica.MODULO){
    if(tipoIzquierdo == 1 && tipoDerecho ==1){
        return 1;
    }else{
        error("El modulo sólo trabaja con tipos enteros");
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
               tokenActual.getClase() == ClaseLexica.FALSE ||
               tokenActual.getClase() == ClaseLexica.MENOS ||
               tokenActual.getClase() == ClaseLexica.NOT;
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
               tokenActual.getClase() == ClaseLexica.SCAN || 
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
   
    tabla = stackSymbolTable.peek();
   // imprimirTablaDeSimbolos(tabla);
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

    if (tipo < -1) {
        String tipoStr = String.valueOf(tipo);
    
        // Verificar si el segundo carácter es un '8'
        if (tipoStr.length() >= 3 && tipoStr.charAt(1) == '8') { // El índice 1 corresponde al segundo carácter
            return "ptr";
        } else {
            return "arreglo";
        }
    }

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
        //imprimirTablaDeSimbolos(stackSymbolTable.peek());
        error("Identificador no declarado: " + id);
    }
    return simbolo.getType(); // Retorna el tipo de la variable
}

// Inicializar la tabla de tipos con los tipos básicos
private void inicializarTypeTable() {
    typeTable = new TypeTable();
    
    // Registrar tipos básicos
    typeTable.addType(1, 1, 0); // void
    typeTable.addType(1, 4, 3); // int
    typeTable.addType(1, 4, 3); // float
    typeTable.addType(1, 8, 3); // double
    typeTable.addType(1, 2, 4); // string
    typeTable.addType(1, 1, 5); // runa
    typeTable.addType(1, 1, 6); // boolean
    typeTable.addType(1, 16, 7); // complex
    

   
}

//FUnción para imprimir la tabla de símbolos dada
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

//Función para imprimir la tabla de tipos
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
            //imprimirTablaDeSimbolos(structTable);
        } else {
            System.out.println("Tabla vacía o no registrada para " + structName);
        }

        System.out.println("-------------------------------");
    }
}

//Función para calcular los items de un compuesto
private int calcularTotalItems(String dimensiones) {
    int total = 1;
    for (char c : dimensiones.toCharArray()) {
        total *= Character.getNumericValue(c); // Multiplica el valor numérico de cada dígito
    }
    return total;
}

//FUnción auxiliar para verificar que hay al menos tantas funciones declaradas como prototipos declarados
public void validarPrototiposConFunciones(SymbolTable tablaGlobal) {
    // Obtener funciones directamente desde la tabla de símbolos
    List<Map.Entry<String, Symbol>> funciones = tablaGlobal.getSymbolsByCategory("funcion");

    // Verificar que haya al menos tantas funciones como prototipos
    if (funciones.size() < prototiposGlobales.size()) {
        error("El número de funciones implementadas (" + funciones.size() + 
              ") es menor que el número de prototipos declarados (" + prototiposGlobales.size() + ").");
    }

    // Verificar que cada prototipo tenga una función correspondiente
    for (Map.Entry<String, Symbol> prototipoEntry : prototiposGlobales) {
        String idPrototipo = prototipoEntry.getKey();
        Symbol prototipo = prototipoEntry.getValue();

        // Buscar una función con el mismo ID en la tabla de símbolos
        Optional<Map.Entry<String, Symbol>> funcionOpt = funciones.stream()
            .filter(funcionEntry -> idPrototipo.equals(funcionEntry.getKey()))
            .findFirst();

        if (funcionOpt.isEmpty()) {
            error("El prototipo '" + idPrototipo + "' no tiene una función implementada.");
        } else {
            Symbol funcion = funcionOpt.get().getValue();

            // Verificar que los argumentos coincidan
            List<String> argsPrototipo = prototipo.getArgs();
            List<String> argsFuncion = funcion.getArgs();

            if (!argsPrototipo.equals(argsFuncion)) {
                error("Los argumentos de la función '" + idPrototipo + 
                      "' no coinciden con los del prototipo. " +
                      "Esperados: " + argsPrototipo + ", Encontrados: " + argsFuncion);
            }

            // Verificar que los tipos coincidan
            int tipoPrototipo = prototipo.getType();
            int tipoFuncion = funcion.getType();

            if (tipoPrototipo != tipoFuncion) {
                error("El tipo de retorno de la función '" + idPrototipo + 
                      "' no coincide con el del prototipo. " +
                      "Esperado: " + getTipoFromInt(tipoPrototipo) + 
                      ", Encontrado: " + getTipoFromInt(tipoFuncion));
            }
        }
    }

   
}


//FUnción auxiliar para realizar una operación aritmética con tipos aritméticos o complex
private String realizarOperacionAritmetica(String valorIzquierdo, String valorDerecho, ClaseLexica operador) {
    try {

        
        // Detectar si son números complejos
        boolean esComplejoIzq = esComplejo(valorIzquierdo);
        boolean esComplejoDer = esComplejo(valorDerecho);


       

        if (esComplejoIzq && esComplejoDer) {
            // Parsear números complejos
            Complejo complejo1 = parseComplejo(valorIzquierdo);
            Complejo complejo2 = parseComplejo(valorDerecho);

            Complejo resultado;
            switch (operador) {
                case MAS:
                    resultado = complejo1.sumar(complejo2);
                    break;
                case MENOS:
                    resultado = complejo1.restar(complejo2);
                    break;
                case MULTIPLICACION:
                    resultado = complejo1.multiplicar(complejo2);
                    break;
                case DIVISION:
                    resultado = complejo1.dividir(complejo2);
                    break;
                default:
                    error("Operador aritmético no válido para números complejos: " + operador);
                    return null;
            }
            return resultado.toString(); // Convertir a String
        } else {
            // Operaciones normales para números reales
            return realizarOperacionNumerica(valorIzquierdo, valorDerecho, operador);
        }
    } catch (Exception e) {
        error("Error en operación aritmética: " + e.getMessage());
        return null;
    }
}

//Función para parsear de string a número entero
private Number parseNumero(String valor) throws NumberFormatException {
    // Eliminar sufijos 'f', 'F', 'd', 'D' si están presentes
    String valorLimpio = valor.replaceAll("[fFdD]$", "");

    // Verificar si es un número entero
    if (valorLimpio.matches("^-?\\d+$")) {
        return Integer.parseInt(valorLimpio);
    }
    // Verificar si es un número flotante o doble
    else if (valorLimpio.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
        return Double.parseDouble(valorLimpio);
    }
    // Buscar el valor en la pila de tablas de símbolos si no coincide con los patrones
    else {
        Symbol simbolo = stackSymbolTable.lookup(valor);
        if (simbolo != null) {
            String valorSimbolo = simbolo.getValue();
            if (valorSimbolo.matches("^-?\\d+$")) {
                return Integer.parseInt(valorSimbolo);
            } else if (valorSimbolo.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
                return Double.parseDouble(valorSimbolo);
            } else {
                throw new NumberFormatException("El valor del símbolo '" + valor + "' no es un número válido: " + valorSimbolo);
            }
        }
        throw new NumberFormatException("Formato de número no reconocido y símbolo no encontrado: " + valor);
    }
}



//Función para verificar si un string es de tipo complex
private boolean esComplejo(String valor) {
    // Expresión regular para números complejos con un signo opcional al inicio
    String regex = "^-?" + // Signo negativo opcional al inicio
                   "([0-9]+(\\.[0-9]{1,7})?([eE][+-]?[0-9]+)?[fF]?|[0-9]+(\\.[0-9]{1,16})?([eE][+-]?[0-9]+)?[dD]?)" +
                   "[+-]" +
                   "([0-9]+(\\.[0-9]{1,7})?([eE][+-]?[0-9]+)?[fF]?|[0-9]+(\\.[0-9]{1,16})?([eE][+-]?[0-9]+)?[dD]?)i$";

    // Verificar si el valor coincide con el patrón de número complejo
    boolean t = valor.matches(regex);

    if (!t) {
        // Si no coincide, verificar si es un símbolo en la tabla de símbolos
        Symbol simbolo = stackSymbolTable.lookup(valor);
        if (simbolo != null) {
            String valorSimbolo = simbolo.getValue();
            t = valorSimbolo.matches(regex);
        }
    }

    return t; 
}


//FUnción auxiliar para hacer el parseo de literales tipo complex
private Complejo parseComplejo(String valor) {
    try {
        valor = valor.trim(); // Eliminar espacios en blanco

        

        // Eliminar la 'i' al final para simplificar el procesamiento
        String sinImaginario = valor.substring(0, valor.length() - 1);

        // Buscar el último signo '+' o '-' que separa la parte real de la imaginaria
        int posSigno = Math.max(sinImaginario.lastIndexOf('+'), sinImaginario.lastIndexOf('-'));

        if (posSigno <= 0) {
            throw new NumberFormatException("Formato inválido para el número complejo: " + valor);
        }

        // Separar la parte real y la parte imaginaria
        String parteReal = sinImaginario.substring(0, posSigno);
        String parteImaginaria = sinImaginario.substring(posSigno);

        // Convertir ambas partes a números
        double real = Double.parseDouble(parteReal.trim());
        double imaginario = Double.parseDouble(parteImaginaria.trim());

        return new Complejo(real, imaginario);
    } catch (Exception e) {
        // Si ocurre un error, verificar si el valor está en la tabla de símbolos
        Symbol simbolo = stackSymbolTable.lookup(valor);
        if (simbolo != null) {
            String valorSimbolo = simbolo.getValue();
            return parseComplejo(valorSimbolo); // Intentar parsear el valor del símbolo
        }
        // Lanzar la excepción original si no se encuentra el símbolo
        throw new NumberFormatException("Error al parsear número complejo: " + e.getMessage() +
                                         ". Además, el símbolo '" + valor + "' no fue encontrado.");
    }
}

//Función para realizar una operación aritmética con tipos adecuados
private String realizarOperacionNumerica(String valorIzquierdo, String valorDerecho, ClaseLexica operador) {
    Number op1 = parseNumero(valorIzquierdo);
    Number op2 = parseNumero(valorDerecho);
    double resultado;

    switch (operador) {
        case MAS:
            resultado = op1.doubleValue() + op2.doubleValue();
            break;
        case MENOS:
            resultado = op1.doubleValue() - op2.doubleValue();
            break;
        case MULTIPLICACION:
            resultado = op1.doubleValue() * op2.doubleValue();
            break;
        case DIVISION:
            if (op2.doubleValue() == 0) {
                error("División por cero");
            }
            resultado = op1.doubleValue() / op2.doubleValue();
            break;
        case MODULO:
            if (op2.doubleValue() == 0) {
                error("Módulo por cero");
            }
            resultado = op1.doubleValue() % op2.doubleValue();
            break;
        case DIVISION_ENTERA:
            // División entera: aplicar división de enteros (sin decimales)
            int resultadoEntero = (int) op1 / (int) op2;
            resultado = (double) resultadoEntero;
            break;
        default:
            error("Operador aritmético no válido: " + operador);
            return null;
    }
    return String.valueOf(resultado);
}



//FUnción auxiliar para evaluar una expresión con operaodr relacional
private boolean evaluarOperacionRelacional(String valorIzquierdo, String valorDerecho, ClaseLexica operador) {
    try {
        // Convertir ambos valores a double para comparaciones numéricas
        double op1 = Double.parseDouble(valorIzquierdo);
        double op2 = Double.parseDouble(valorDerecho);

        switch (operador) {
            case MENOR:
                return op1 < op2;
            case MENOR_IGUAL:
                return op1 <= op2;
            case MAYOR:
                return op1 > op2;
            case MAYOR_IGUAL:
                return op1 >= op2;
            case IGUAL:
                return op1 == op2;
            case DIFERENTE:
                return op1 != op2;
            default:
                error("Operador relacional no válido para números: " + operador);
                return false; // Este return nunca se alcanzará
        }
    } catch (NumberFormatException e) {
        // Si la conversión a double falla, verificar si los valores son booleanos
        boolean op1 = Boolean.parseBoolean(valorIzquierdo);
        boolean op2 = Boolean.parseBoolean(valorDerecho);

        switch (operador) {
            case IGUAL:
                return op1 == op2;
            case DIFERENTE:
                return op1 != op2;
            default:
                error("Operador relacional no válido para booleanos: " + operador);
                return false; // Este return nunca se alcanzará
        }
    }
}

// Función auxiliar para convertir un String a un entero válido
private int convertirAEntero(String valor) {
    try {
        double valorDouble = Double.parseDouble(valor);

        // Verificar si el valorDouble es un número entero
        if (valorDouble % 1 != 0) {
            error("El valor del índice no es un número entero válido: " + valor);
        }

        return (int) valorDouble;
    } catch (NumberFormatException e) {
        error("No se pudo convertir el valor a un número entero: " + valor);
        return -1; // Este return nunca se alcanzará debido al error
    }
}

//Función para guardar el resultado de la tabla de símbolos (en este caso la global) en un archivo de texto en el directorio raíz
public void guardarTablaDeSimbolosEnArchivo(SymbolTable tabla, String archivo) {
    if (tabla != null) {
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write("Tabla de símbolos:\n");
            
            // Obtener todos los IDs
            Set<String> ids = tabla.getAllIds();
            
            // Escribir cada ID junto con su símbolo correspondiente
            for (String id : ids) {
                Symbol symbol = tabla.getSymbolSecure(id); // Obtener el símbolo por el id
                if (symbol != null) {
                    writer.write("ID: " + id + ", " + symbol + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al escribir la tabla de símbolos en el archivo: " + e.getMessage());
        }
    } else {
        System.out.println("La tabla de símbolos está vacía o no existe.");
    }
}

//Función para guardar el resultado de la tabla de tipos en un archivo de texto en el directorio raíz
public void guardarTablaDeTiposEnArchivo(String archivo) {
    try (FileWriter writer = new FileWriter(archivo)) {
        writer.write("Tabla de Tipos:\n");

        // Iterar sobre todos los IDs en la tabla de tipos
        for (int id : typeTable.getAllIds()) {
            Type tipo = typeTable.getType(id);

            if (tipo != null) {
                writer.write("ID: " + id + ", " + tipo + "\n");

                // Si el tipo es una estructura, listar sus miembros
                Map<String, Integer> miembros = getStructMembers(id);
                if (miembros != null) {
                    writer.write("  Miembros:\n");
                    for (Map.Entry<String, Integer> miembro : miembros.entrySet()) {
                        writer.write("    - " + miembro.getKey() + ": Tipo ID " + miembro.getValue() + "\n");
                    }
                }
            } else {
                writer.write("ID: " + id + " está vacío o no es válido.\n");
            }
        }
    } catch (IOException e) {
        System.err.println("Error al escribir la tabla de tipos en el archivo: " + e.getMessage());
    }
}

//Función para actualizar los datos de un tipo struct una vez que se ha terminado de procesar
private void actualizarDatosStruct(String structName) {
    // Obtener la tabla de símbolos asociada al struct
    SymbolTable structTable = structTables.get(structName);
    if (structTable == null) {
        error("El struct '" + structName + "' no está registrado.");
        return;
    }

    // Obtener el ID del tipo asociado al struct
    int structId = Integer.parseInt(structName.split("_")[1]);
    System.out.println("STRUCT ID : " +structId);
    Type structType = typeTable.getType(structId);
    if (structType == null) {
        imprimirTablaDeTipos();
        error("El tipo asociado al struct '" + structName + "' no existe en la tabla de tipos.");
        
        return;
    }

    // Calcular el número total de items y el tamaño total del struct
    int totalItems = 0;
    int totalTam = 0;

    for (String memberName : structTable.getAllIds()) {
        Symbol member = structTable.getSymbolSecure(memberName);
        if (member != null) {
            int memberTypeId = member.getType();
            Type memberType = typeTable.getType(memberTypeId);
            if (memberType != null) {
                totalItems += memberType.getItems();
                totalTam += memberType.getTam();
            } else {
                error("El tipo del miembro '" + memberName + "' no está definido.");
            }
        }
    }

    // Actualizar los valores en la tabla de tipos
    structType.setItems(totalItems);
    structType.setTam(totalTam);

    System.out.println("Datos actualizados para el struct '" + structName + "': Items = " 
                        + totalItems + ", Tamaño = " + totalTam);
}

//Función para guardar el resultado del árbol semántico en un archivo de texto en el directorio raíz
public void guardarArbolSemanticoEnArchivo(Object arbolSemantico, String archivo) {
    if (arbolSemantico == null) {
        System.err.println("El árbol semántico está vacío.");
        return;
    }

    try (FileWriter writer = new FileWriter(archivo)) {
        // Escribir la representación del árbol en el archivo
        writer.write("Árbol semántico:\n");
        writer.write(arbolSemantico.toString());
        System.out.println("Árbol semántico guardado en: " + archivo);
    } catch (IOException e) {
        System.err.println("Error al escribir el árbol semántico en el archivo: " + e.getMessage());
    }
}
}

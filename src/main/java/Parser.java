package main.java;

import java.io.FileWriter;
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

    private ArbolSemantico arbolSemantico;
    private NodoArbol nodoActual;

    private Symbol simboloEstructurado = null; // Variable global para almacenar el último símbolo estructurado

    private SymbolTableStack stackSymbolTable = new SymbolTableStack();
    private TypeTable typeTable = new TypeTable();
    private Map<String, SymbolTable> structTables = new HashMap<>();
    private List<Map.Entry<String, Symbol>> prototiposGlobales = new ArrayList<>();



    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.arbolSemantico = new ArbolSemantico(null);
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
        //imprimirTablaDeSimbolos(stackSymbolTable.base());
        //imprimirTablaDeTipos();
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
            validarPrototiposConFunciones(stackSymbolTable.base());
            System.out.println("La cadena es aceptada");

            // Asignar direcciones a las variables en la tabla de símbolos global
            int direccion = 0;
            for (Symbol symbol : stackSymbolTable.base().getAllSymbols()) {
                symbol.setAddress(direccion);
                Type t = typeTable.getType(symbol.getType());
                direccion += t.getTam(); 
            }

            // TO DO
            // Asignar direcciones a las variables en las tablas de símbolos no globales
           
            // OUTPUT DEL ÁRBOL SEMÁNTICO
            //System.out.println("Árbol semántico:");
            //System.out.println(arbolSemantico.toString());
            //System.out.println(stackSymbolTable.toString());
            //System.out.println(typeTable.toString());
            guardarArbolSemanticoEnArchivo(arbolSemantico, "ASA.txt");
            guardarTablaDeSimbolosEnArchivo(stackSymbolTable.base(), "TablaDeSimbolos.txt");
            guardarTablaDeTiposEnArchivo("TablaDeTipos.txt");

        } else {
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
        while (tokenActual.getClase() == ClaseLexica.PROTO) {
            eat(ClaseLexica.PROTO);
            int tipoRetorno = tipo(); // Tipo de retorno del prototipo
            String idPrototipo = tokenActual.getLexema(); // Nombre del prototipo
            eat(ClaseLexica.ID);

            NodoArbol nodoPrototipo = new NodoArbol("PROTO", idPrototipo);
            arbolSemantico.agregarHijo(nodoActual, nodoPrototipo);
            nodoActual = nodoPrototipo;
    
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
            prototiposGlobales.add(Map.entry(idPrototipo, simboloPrototipo));  // Guardar en la lista global
            
            
            eat(ClaseLexica.PARENTESIS_CIERRA);
            eat(ClaseLexica.PUNTO_Y_COMA);
    
            //System.out.println("Prototipo '" + idPrototipo + "' registrado en la tabla global.");
            //System.out.println("Tabla de símbolos para el prototipo '" + idPrototipo + "':");
            //imprimirTablaDeSimbolos(tablaPrototipo);
    
            stackSymbolTable.pop(); // Retirar la tabla de argumentos del prototipo
            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual); // Retroceder al nodo padre
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
           // System.out.println("El tipo de las siguientes variables es:"+ tipo);
            List<String> variables = lista_var();
            eat(ClaseLexica.PUNTO_Y_COMA);

            // Convertir el número a String
            String numeroStr = String.valueOf(tipo); // Asegurar que sea positivo para evitar problemas con '-'

            if(tipo < -1){
                if(numeroStr.charAt(1) == '8'){
                    // Registrar cada variable en la tabla de símbolos actual
                for (String var : variables) {
                    Symbol varSymbol = new Symbol(-1, tipo, "puntero", null);
                    agregarSimbolo(var, varSymbol);

                    NodoArbol nodoVar = new NodoArbol("VAR", var);
                    arbolSemantico.agregarHijo(nodoActual, nodoVar);
                    arbolSemantico.anotarNodo(nodoVar, "Tipo: Puntero a " + getTipoFromInt(tipo));
                }
                }else{

                      // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "arreglo", null);
                agregarSimbolo(var, varSymbol);

                NodoArbol nodoVar = new NodoArbol("VAR", var);
                    arbolSemantico.agregarHijo(nodoActual, nodoVar);
                    arbolSemantico.anotarNodo(nodoVar, "Tipo: Arreglo de " + getTipoFromInt(tipo));

            }
                }
              
            }else{
            // Registrar cada variable en la tabla de símbolos actual
            for (String var : variables) {
                Symbol varSymbol = new Symbol(-1, tipo, "variable", null);
                agregarSimbolo(var, varSymbol);

                NodoArbol nodoVar = new NodoArbol("VAR", var);
                arbolSemantico.agregarHijo(nodoActual, nodoVar);
                arbolSemantico.anotarNodo(nodoVar, "Tipo: " + getTipoFromInt(tipo));
            }
        }
        }
    }

    private void decl_func() {
        if (tokenActual.getClase() == ClaseLexica.FUNC) {
            eat(ClaseLexica.FUNC);
    
            int tipoRetorno = tipo();   
            // Obtener el nombre de la función
            String idFuncion = tokenActual.getLexema();
            eat(ClaseLexica.ID);

            NodoArbol nodoFuncion = new NodoArbol("FUNC", idFuncion);
            arbolSemantico.agregarHijo(nodoActual, nodoFuncion);
            nodoActual = nodoFuncion;

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
    
            //System.out.println("Función '" + idFuncion + "' agregada a la tabla de símbolos global.");

            // Procesar el bloque de la función
            eat(ClaseLexica.PARENTESIS_CIERRA);
            bloque();
    
            // stackSymbolTable.pop(); 
            // Mantener la tabla de la función en la pila (no se elimina)

            nodoActual = arbolSemantico.getPadreNodoArbol(nodoActual);
            //System.out.println("Tabla de símbolos para la función '" + idFuncion + "':");
            //imprimirTablaDeSimbolos(stackSymbolTable.peek());
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
            typeTable.addTypeStruct(structId, 0, 0, null);
           // System.out.println("Struct '" + structName + "' registrado con ID: " + structId);
            tipoBase = structId;
            SymbolTable structTable = new SymbolTable();
            stackSymbolTable.push(structTable); // Crear un nuevo ámbito para el struct
            eat(ClaseLexica.LLAVE_ABRE);
            decl_var(); // Procesar las variables internas del struct
            eat(ClaseLexica.LLAVE_CIERRA);

            // Registrar la tabla de símbolos del struct en el HashMap
            structTables.put(structName, stackSymbolTable.pop());
            actualizarDatosStruct(structName);

            
           // imprimirTablaDeSimbolos(structTables.get(structName));
        } else if (tokenActual.getClase() == ClaseLexica.PTR) {
            // Manejar punteros
            int tipoBasico = puntero();
            String tipoPtr = "-8";
            tipoPtr += Integer.toString(tipoBasico);
            tipoBase = Integer.valueOf(tipoPtr);
            int tam = typeTable.getTam(tipoBasico);
            int item = typeTable.getItems(tipoBasico);
            int parent = typeTable.getParent(tipoBasico);
            typeTable.addTypeStruct(tipoBase, item, tam, parent);
           // System.out.println("TIPO PUNTERO: "+tipoBase);
          
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

        String idStr = String.valueOf(idCompuesto);
    
        char segundoCaracter = idStr.charAt(1);
        tipoBase = Character.getNumericValue(segundoCaracter);

        // Obtener el tamaño del tipo base usando la tabla de tipos
        Type tipoBaseType = typeTable.getType(tipoBase);
        
            int tamBase = tipoBaseType.getTam();
            
            // Calcular el tamaño total
            int tamTotal = totalItems * tamBase;



        if (!typeTable.contains(idCompuesto)) {
            typeTable.addTypeArray(idCompuesto, totalItems, tamTotal, tipoPadre); // Añadir el tipo compuesto
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

// Inicializar la tabla de tipos
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
            //imprimirTablaDeSimbolos(structTable);
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

    // Imprimir las listas de prototipos y funciones
   /*  System.out.println("\nPrototipos:");
    for (Map.Entry<String, Symbol> entry : prototiposGlobales) {
        System.out.println(entry.getKey() + " -> " + entry.getValue());
    }

    System.out.println("\nFunciones:");
    for (Map.Entry<String, Symbol> entry : funciones) {
        System.out.println(entry.getKey() + " -> " + entry.getValue());
    }

    System.out.println("Validación de prototipos y funciones completada con éxito.");*/
}


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

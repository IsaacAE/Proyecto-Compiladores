<p  align="center">
  <img  width="200"  src="https://www.fciencias.unam.mx/sites/default/files/logoFC_2.png"  alt="">  <br>Compiladores  2025-1 <br>
  Proyecto Final <br> Profesora: Ariel Adara Mercado Martínez
</p>

## Alumnos
Alcántara Estrada Kevin Isaac
Sandoval Mendoza Angel
Menchaca Carrillo Rodolfo Josue

### Estructura del directorio

``` 
Proyecto-Final
├── README.md // (Este archivo) con la información del proyecto
├── Gramática_de_proyecto.pdf // Archivo con el procesamiento hecho para la gramática dada
├── diagramas_sintaxis.xhtml // Archivo con los diagramas de sintaxis de la gramática final
├── grammar.ebnf // Archivo con la gramática en forma EBNF
├── ASA.txt // Archivo con el árbol de análisis semántico resultante
├── TablaDeSimbolos.txt // Archivo con la tabla de símbolos global resultante
├── TablaDeTipos.txt // Archivo con la tabla de símbolos resultante
├── src
│   └── main
│       ├── java
│       │   ├── ArbolSemantico.java // Clase que representa el árbol semántico
│       │   ├── ClaseLexica.java // Clase que reemplaza el enum de P2
│       │   ├── Main.java // Clase con el método main
│       │   ├── NodoArbol.java // Clase que representa un nodo en el árbol semántico
│       │   ├── Parser.java // Implementación del An. Sintáctico
│       │   ├── Symbol.java // Clase que representa símbolos utilizados en el análisis
│       │   ├── SymbolTable.java // Implementación de la tabla de símbolos
│       │   ├── SymbolTableStack.java // Pila de tablas de símbolos
│       │   ├── TipoValor.java // Clase que asocia tipos con valores
│       │   ├── Token.java // Clase para componentes léxicos
│       │   ├── Type.java // Clase que representa los tipos del lenguaje
│       │   ├── TypeTable.java // Tabla para la gestión de tipos
│       └── jflex
│           └── Lexer.flex // Definición del An. Léxico
└── tst
    ├── prueba.txt // Archivo de entrada prueba que debe ser aceptado por el parser
    ├── prueba2.txt // Archivo de entrada que es aceptado por el parser
    ├── prueba3.txt // Archivo de entrada que es aceptado por el parser
    ├── prueba4.txt // Archivo de entrada que es aceptado por el parser
    ├── pruebaStruct.txt // Archivo de entrada que es aceptado por el parser y verifica tipos struct
    ├── pruebaPtr.txt // Archivo de entrada que es aceptado por el parser y verifica tipos puntero
    └── pruebasCompuestos.txt // Archivo de entrada que es aceptado por el parser y verifica tipos compuestos
```

### Uso

#### Compilacion

***NOTA:*** ubicarse en el directorio raíz y tener instalado JFlex (y una versión de jdk 8 superior). 

Para obtener más detalles y descargar la última versión de JFlex, puedes visitar la siguiente página oficial:

[Instrucciones para instalar JFlex](https://jflex.de/download.html)


```bash
$ jflex src/main/jflex/Lexer.flex
$ javac --source-path src -d build src/main/java/Main.java
```

#### Ejecucion

```bash
$ java -cp build main.java.Main tst/<nombre_archivo>.txt
```



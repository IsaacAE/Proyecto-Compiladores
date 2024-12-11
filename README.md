<p  align="center">
  <img  width="200"  src="https://www.fciencias.unam.mx/sites/default/files/logoFC_2.png"  alt="">  <br>Compiladores  2025-1 <br>
  Proyecto Final <br> Profesora: Ariel Adara Mercado Martínez
</p>

## Alumnos
Alcántara Estrada Kevin Isaac
Sandoval Mendoza Angel

### Estructura del directorio

``` 
P3
├── README.md
├── Práctica3.pdf // Archivo con los ejercicios referentes a la gramática y su justificación
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
    └── pruebasCompuestos.txt // Archivo de entrada que es aceptado por el parser y verifica tipos compuestos
```

### Uso

#### Compilacion

```bash
$ jflex src/main/jflex/Lexer.flex
$ javac --source-path src -d build src/main/java/Main.java
```

#### Ejecucion

```bash
$ java -cp build main.java.Main tst/<nombre_archivo>.txt
```



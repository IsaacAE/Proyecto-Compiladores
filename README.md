<p  align="center">
  <img  width="200"  src="https://www.fciencias.unam.mx/sites/default/files/logoFC_2.png"  alt="">  <br>Compiladores  2025-1 <br>
  Proyecto Final <br> Profesora: Ariel Adara Mercado Martínez
</p>

## Alumnos
Alcántara Estrada Kevin Isaac



### Estructura del directorio
```c++
P3
├── README.md
├── Práctica3.pdf // Archivo con los ejercicios referentes a la gramática y su justificación
├── src
│   └── main
│       ├── java
│       │   ├── ClaseLexica.java // Clase que reemplaza el enum de P2
│       │   ├── Main.java // Clase con el método main
│       │   ├── Parser.java // Implementación del An. Sintáctico
│       │   ├── Token.java // Clase para componentes léxicos
│       └── jflex
│           └── Lexer.flex // Definición del An. Léxico
└── tst
    ├── prueba.txt // Archivo de entrada prueba que debe ser aceptado por el parser
    ├── valido1.txt // Archivo de entrada que es aceptado por el parser
    ├── valido2.txt // Archivo de entrada que es aceptado por el parser
    ├── invalido1.txt // Archivo de entrada que NO es aceptado por el parser
    └── invalido2.txt // Archivo de entrada que NO es aceptado por el parser
   

```

### Uso

#### Compilacion

```bash
$ jflex src/main/jflex/Lexer.flex
$ javac --source-path src -d build src/main/jflex/Main.java
```

#### Ejecucion

```bash
$ java -cp build main.java.Main tst/<nombre_archivo>.txt
```



package main.java;

public enum ClaseLexica {
    PROGRAMA,  // Palabra clave "programa"
    PROTO,     // Palabra clave "proto"
    FUNC,      // Palabra clave "func"
    IF,        // Palabra clave "if"
    ELSE,      // Palabra clave "else"
    WHILE,     // Palabra clave "while"
    DO,        // Palabra clave "do"
    BREAK,     // Palabra clave "break"
    RETURN,    // Palabra clave "return"
    SWITCH,    // Palabra clave "switch"
    CASE,      // Palabra clave "case"
    DEFAULT,   // Palabra clave "default"
    PRINT,     // Palabra clave "print"
    SCAN,      // Palabra clave "scan"
    STRUCT,    // Palabra clave "struct"
    PTR,       // Palabra clave "ptr"

    // Tipos básicos
    INT,       // Palabra clave "int"
    FLOAT,     // Palabra clave "float"
    DOUBLE,    // Palabra clave "double"
    COMPLEX,   // Palabra clave "complex"
    RUNE,      // Palabra clave "rune"
    VOID,      // Palabra clave "void"
    STRING,    // Palabra clave "string"
    TRUE,      // Palabra clave "true"
    FALSE,     // Palabra clave "false"

    // Operadores y símbolos
    ASIGNACION,       // Operador "="
    MAS,              // Operador "+"
    MENOS,            // Operador "-"
    MULTIPLICACION,   // Operador "*"
    DIVISION,         // Operador "/"
    MODULO,           // Operador "%"
    DIVISION_ENTERA,  // Operador "//"
    AND,              // Operador "&&"
    OR,               // Operador "||"
    NOT,              // Operador "!"
    IGUAL,            // Operador "=="
    DIFERENTE,        // Operador "!="
    MENOR,            // Operador "<"
    MENOR_IGUAL,      // Operador "<="
    MAYOR,            // Operador ">"
    MAYOR_IGUAL,      // Operador ">="
    COMA,             // Símbolo ","
    PUNTO_Y_COMA,     // Símbolo ";"
    PUNTO,            // Símbolo "."
    PARENTESIS_ABRE,  // Símbolo "("
    PARENTESIS_CIERRA,// Símbolo ")"
    LLAVE_ABRE,       // Símbolo "{"
    LLAVE_CIERRA,     // Símbolo "}"
    CORCHETE_ABRE,    // Símbolo "["
    CORCHETE_CIERRA,  // Símbolo "]"
    DOS_PUNTOS,       // Símbolo ":"

    // Literales
    LITERAL_ENTERA,   // Números enteros
    LITERAL_FLOTANTE, // Números flotantes
    LITERAL_CADENA,   // Cadenas de texto
    LITERAL_RUNA,     // Literales tipo "rune"

    // Identificadores
    ID,                // Identificadores
    
    EOF
}

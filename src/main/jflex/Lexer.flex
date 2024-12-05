
package main.jflex;

import main.java.ClaseLexica;
import main.java.Token;

%%

%{

public Token actual;

%}

%class Lexer
%unicode
%public
%type Token

%%



// Ignorar espacios en blanco
[ \t\n\r\f]+            { /* ignorar */ }



// Palabras clave
"programa"              { System.out.println("Token encontrado: PROGRAMA (" + yytext() + ")"); return new Token(ClaseLexica.PROGRAMA, yytext()); }
"proto"                 { System.out.println("Token encontrado: PROTO (" + yytext() + ")"); return new Token(ClaseLexica.PROTO, yytext()); }
"func"                  { System.out.println("Token encontrado: FUNC (" + yytext() + ")"); return new Token(ClaseLexica.FUNC, yytext()); }
"if"                    { System.out.println("Token encontrado: IF (" + yytext() + ")"); return new Token(ClaseLexica.IF, yytext()); }
"else"                  { System.out.println("Token encontrado: ELSE (" + yytext() + ")"); return new Token(ClaseLexica.ELSE, yytext()); }
"while"                 { System.out.println("Token encontrado: WHILE (" + yytext() + ")"); return new Token(ClaseLexica.WHILE, yytext()); }
"do"                    { System.out.println("Token encontrado: DO (" + yytext() + ")"); return new Token(ClaseLexica.DO, yytext()); }
"break"                 { System.out.println("Token encontrado: BREAK (" + yytext() + ")"); return new Token(ClaseLexica.BREAK, yytext()); }
"return"                { System.out.println("Token encontrado: RETURN (" + yytext() + ")"); return new Token(ClaseLexica.RETURN, yytext()); }
"switch"                { System.out.println("Token encontrado: SWITCH (" + yytext() + ")"); return new Token(ClaseLexica.SWITCH, yytext()); }
"case"                  { System.out.println("Token encontrado: CASE (" + yytext() + ")"); return new Token(ClaseLexica.CASE, yytext()); }
"default"               { System.out.println("Token encontrado: DEFAULT (" + yytext() + ")"); return new Token(ClaseLexica.DEFAULT, yytext()); }
"print"                 { System.out.println("Token encontrado: PRINT (" + yytext() + ")"); return new Token(ClaseLexica.PRINT, yytext()); }
"scan"                  { System.out.println("Token encontrado: SCAN (" + yytext() + ")"); return new Token(ClaseLexica.SCAN, yytext()); }
"struct"                { System.out.println("Token encontrado: STRUCT (" + yytext() + ")"); return new Token(ClaseLexica.STRUCT, yytext()); }
"ptr"                   { System.out.println("Token encontrado: PTR (" + yytext() + ")"); return new Token(ClaseLexica.PTR, yytext()); }

// Tipos básicos
"int"                   { System.out.println("Token encontrado: INT (" + yytext() + ")"); return new Token(ClaseLexica.INT, yytext()); }
"float"                 { System.out.println("Token encontrado: FLOAT (" + yytext() + ")"); return new Token(ClaseLexica.FLOAT, yytext()); }
"double"                { System.out.println("Token encontrado: DOUBLE (" + yytext() + ")"); return new Token(ClaseLexica.DOUBLE, yytext()); }
"complex"               { System.out.println("Token encontrado: COMPLEX (" + yytext() + ")"); return new Token(ClaseLexica.COMPLEX, yytext()); }
"rune"                  { System.out.println("Token encontrado: RUNE (" + yytext() + ")"); return new Token(ClaseLexica.RUNE, yytext()); }
"void"                  { System.out.println("Token encontrado: VOID (" + yytext() + ")"); return new Token(ClaseLexica.VOID, yytext()); }
"string"                { System.out.println("Token encontrado: STRING (" + yytext() + ")"); return new Token(ClaseLexica.STRING, yytext()); }
"true"                  { System.out.println("Token encontrado: TRUE (" + yytext() + ")"); return new Token(ClaseLexica.TRUE, yytext()); }
"false"                 { System.out.println("Token encontrado: FALSE (" + yytext() + ")"); return new Token(ClaseLexica.FALSE, yytext()); }

// Operadores y símbolos
"="                     { System.out.println("Token encontrado: ASIGNACION (" + yytext() + ")"); return new Token(ClaseLexica.ASIGNACION, yytext()); }
"+"                     { System.out.println("Token encontrado: MAS (" + yytext() + ")"); return new Token(ClaseLexica.MAS, yytext()); }
"-"                     { System.out.println("Token encontrado: MENOS (" + yytext() + ")"); return new Token(ClaseLexica.MENOS, yytext()); }
"*"                     { System.out.println("Token encontrado: MULTIPLICACION (" + yytext() + ")"); return new Token(ClaseLexica.MULTIPLICACION, yytext()); }
"/"                     { System.out.println("Token encontrado: DIVISION (" + yytext() + ")"); return new Token(ClaseLexica.DIVISION, yytext()); }
"%"                     { System.out.println("Token encontrado: MODULO (" + yytext() + ")"); return new Token(ClaseLexica.MODULO, yytext()); }
"//"                    { System.out.println("Token encontrado: DIVISION_ENTERA (" + yytext() + ")"); return new Token(ClaseLexica.DIVISION_ENTERA, yytext()); }
"&&"                    { System.out.println("Token encontrado: AND (" + yytext() + ")"); return new Token(ClaseLexica.AND, yytext()); }
"||"                    { System.out.println("Token encontrado: OR (" + yytext() + ")"); return new Token(ClaseLexica.OR, yytext()); }
"!"                     { System.out.println("Token encontrado: NOT (" + yytext() + ")"); return new Token(ClaseLexica.NOT, yytext()); }
"=="                    { System.out.println("Token encontrado: IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.IGUAL, yytext()); }
"!="                    { System.out.println("Token encontrado: DIFERENTE (" + yytext() + ")"); return new Token(ClaseLexica.DIFERENTE, yytext()); }
"<"                     { System.out.println("Token encontrado: MENOR (" + yytext() + ")"); return new Token(ClaseLexica.MENOR, yytext()); }
"<="                    { System.out.println("Token encontrado: MENOR_IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.MENOR_IGUAL, yytext()); }
">"                     { System.out.println("Token encontrado: MAYOR (" + yytext() + ")"); return new Token(ClaseLexica.MAYOR, yytext()); }
">="                    { System.out.println("Token encontrado: MAYOR_IGUAL (" + yytext() + ")"); return new Token(ClaseLexica.MAYOR_IGUAL, yytext()); }
","                     { System.out.println("Token encontrado: COMA (" + yytext() + ")"); return new Token(ClaseLexica.COMA, yytext()); }
";"                     { System.out.println("Token encontrado: PUNTO_Y_COMA (" + yytext() + ")"); return new Token(ClaseLexica.PUNTO_Y_COMA, yytext()); }
":"                     { System.out.println("Token encontrado: DOS_PUNTOS (" + yytext() + ")"); return new Token(ClaseLexica.DOS_PUNTOS, yytext()); }
"."                     { System.out.println("Token encontrado: PUNTO (" + yytext() + ")"); return new Token(ClaseLexica.PUNTO, yytext()); }
"("                     { System.out.println("Token encontrado: PARENTESIS_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.PARENTESIS_ABRE, yytext()); }
")"                     { System.out.println("Token encontrado: PARENTESIS_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.PARENTESIS_CIERRA, yytext()); }
"{"                     { System.out.println("Token encontrado: LLAVE_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.LLAVE_ABRE, yytext()); }
"}"                     { System.out.println("Token encontrado: LLAVE_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.LLAVE_CIERRA, yytext()); }
"["                     { System.out.println("Token encontrado: CORCHETE_ABRE (" + yytext() + ")"); return new Token(ClaseLexica.CORCHETE_ABRE, yytext()); }
"]"                     { System.out.println("Token encontrado: CORCHETE_CIERRA (" + yytext() + ")"); return new Token(ClaseLexica.CORCHETE_CIERRA, yytext()); }


// Literales y otros patrones

// Identificadores (incluyendo caracteres especiales de idiomas como español)
[a-zA-ZñÑáéíóúÁÉÍÓÚüÜöÖ_][a-zA-ZñÑáéíóúÁÉÍÓÚüÜöÖ_0-9]* {
    System.out.println("Token encontrado: ID (" + yytext() + ")");
    return new Token(ClaseLexica.ID, yytext());
}

// Literales enteros
[0-9]+ {
    System.out.println("Token encontrado: LITERAL_ENTERA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_ENTERA, yytext());
}

// Literales flotantes (1-7 decimales, con o sin notación exponencial)
[0-9]+(\.[0-9]{1,7})?([eE][+-]?[0-9]+)?([fF])? {
    System.out.println("Token encontrado: LITERAL_FLOTANTE (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_FLOTANTE, yytext());
}

// Literales dobles (1-16 decimales, con o sin notación exponencial)
[0-9]+(\.[0-9]{1,16})?([eE][+-]?[0-9]+)?([dD])? {
    System.out.println("Token encontrado: LITERAL_DOUBLE (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_DOUBLE, yytext());
}

([0-9]+(\.[0-9]{1,7})([eE][+-]?[0-9]+)?[fF]|[0-9]+(\.[0-9]{1,16})([eE][+-]?[0-9]+)?[dD]?) 
[+-] 
([0-9]+(\.[0-9]{1,7})([eE][+-]?[0-9]+)?[fF]|[0-9]+(\.[0-9]{1,16})([eE][+-]?[0-9]+)?[dD]?)i {
    System.out.println("Token encontrado: LITERAL_COMPLEJA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_COMPLEJA, yytext());
}

'([a-zA-ZñÑáéíóúÁÉÍÓÚüÜöÖ@$!?&#%\\|]|[\b\t])' {
    System.out.println("Token encontrado: LITERAL_RUNA (" + yytext() + ")");
    return new Token(ClaseLexica.LITERAL_RUNA, yytext());
}

// Literales cadenas
\"([^\"\\]|\\.)*\"          { System.out.println("Token encontrado: LITERAL_CADENA (" + yytext() + ")"); return new Token(ClaseLexica.LITERAL_CADENA, yytext()); }

// Comentarios
"//" [^\n]*             { /* ignorar comentarios de una línea */ }
"/*" [^*]* "*" ([^/]* "*" )* "/" { /* ignorar comentarios de varias líneas */ }

<<EOF>>           { return new Token(ClaseLexica.EOF, yytext());}

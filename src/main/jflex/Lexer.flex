
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

// Palabras clave
"programa"              { return new Token(ClaseLexica.PROGRAMA, yytext()); }
"proto"                 { return new Token(ClaseLexica.PROTO, yytext()); }
"func"                  { return new Token(ClaseLexica.FUNC, yytext()); }
"if"                    { return new Token(ClaseLexica.IF, yytext()); }
"else"                  { return new Token(ClaseLexica.ELSE, yytext()); }
"while"                 { return new Token(ClaseLexica.WHILE, yytext()); }
"do"                    { return new Token(ClaseLexica.DO, yytext()); }
"break"                 { return new Token(ClaseLexica.BREAK, yytext()); }
"return"                { return new Token(ClaseLexica.RETURN, yytext()); }
"switch"                { return new Token(ClaseLexica.SWITCH, yytext()); }
"case"                  { return new Token(ClaseLexica.CASE, yytext()); }
"default"               { return new Token(ClaseLexica.DEFAULT, yytext()); }
"print"                 { return new Token(ClaseLexica.PRINT, yytext()); }
"scan"                  { return new Token(ClaseLexica.SCAN, yytext()); }
"struct"                { return new Token(ClaseLexica.STRUCT, yytext()); }
"ptr"                   { return new Token(ClaseLexica.PTR, yytext()); }

// Tipos básicos
"int"                   { return new Token(ClaseLexica.INT, yytext()); }
"float"                 { return new Token(ClaseLexica.FLOAT, yytext()); }
"double"                { return new Token(ClaseLexica.DOUBLE, yytext()); }
"complex"               { return new Token(ClaseLexica.COMPLEX, yytext()); }
"rune"                  { return new Token(ClaseLexica.RUNE, yytext()); }
"void"                  { return new Token(ClaseLexica.VOID, yytext()); }
"string"                { return new Token(ClaseLexica.STRING, yytext()); }
"true"                  { return new Token(ClaseLexica.TRUE, yytext()); }
"false"                 { return new Token(ClaseLexica.FALSE, yytext()); }

// Operadores y símbolos
"="                     { return new Token(ClaseLexica.ASIGNACION, yytext()); }
"+"                     { return new Token(ClaseLexica.MAS, yytext()); }
"-"                     { return new Token(ClaseLexica.MENOS, yytext()); }
"*"                     { return new Token(ClaseLexica.MULTIPLICACION, yytext()); }
"/"                     { return new Token(ClaseLexica.DIVISION, yytext()); }
"%"                     { return new Token(ClaseLexica.MODULO, yytext()); }
"//"                    { return new Token(ClaseLexica.DIVISION_ENTERA, yytext()); }
"&&"                    { return new Token(ClaseLexica.AND, yytext()); }
"||"                    { return new Token(ClaseLexica.OR, yytext()); }
"!"                     { return new Token(ClaseLexica.NOT, yytext()); }
"=="                    { return new Token(ClaseLexica.IGUAL, yytext()); }
"!="                    { return new Token(ClaseLexica.DIFERENTE, yytext()); }
"<"                     { return new Token(ClaseLexica.MENOR, yytext()); }
"<="                    { return new Token(ClaseLexica.MENOR_IGUAL, yytext()); }
">"                     { return new Token(ClaseLexica.MAYOR, yytext()); }
">="                    { return new Token(ClaseLexica.MAYOR_IGUAL, yytext()); }
","                     { return new Token(ClaseLexica.COMA, yytext()); }
";"                     { return new Token(ClaseLexica.PUNTO_Y_COMA, yytext()); }
"."                     { return new Token(ClaseLexica.PUNTO, yytext()); }
"("                     { return new Token(ClaseLexica.PARENTESIS_ABRE, yytext()); }
")"                     { return new Token(ClaseLexica.PARENTESIS_CIERRA, yytext()); }
"{"                     { return new Token(ClaseLexica.LLAVE_ABRE, yytext()); }
"}"                     { return new Token(ClaseLexica.LLAVE_CIERRA, yytext()); }
"["                     { return new Token(ClaseLexica.CORCHETE_ABRE, yytext()); }
"]"                     { return new Token(ClaseLexica.CORCHETE_CIERRA, yytext()); }

// Identificadores y literales
[0-9]+                  { return new Token(ClaseLexica.LITERAL_ENTERA,yytext()); }
[0-9]+"."[0-9]+         { return new Token(ClaseLexica.LITERAL_FLOTANTE, yytext()); }
"\""(.)* "\""           { return new Token(ClaseLexica.LITERAL_CADENA, yytext()); }
\'[^\']\'               { return new Token(ClaseLexica.LITERAL_RUNA, yytext()); }
[a-zA-Z_][a-zA-Z0-9_]*  { return new Token(ClaseLexica.ID, yytext()); }

// Ignorar espacios en blanco
[ \t\n\r\f]+            { /* ignorar */ }

// Comentarios
"//" [^\n]*             { /* ignorar comentarios de una línea */ }
"/*" [^*]* "*" ([^/]* "*" )* "/" { /* ignorar comentarios de varias líneas */ }

package main.java;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import main.jflex.Lexer;
import main.java.Parser;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        ArbolSemantico arbol = null; // Inicializamos arbol fuera del bloque try-catch
        try {
            // Verificamos si se proporcionó un archivo de entrada como argumento
            if (args.length == 0) {
                System.err.println("Error: Se debe proporcionar un archivo de entrada.");
                System.exit(1);
            }
            
            // Se crea un objeto Parser y se pasa el archivo de entrada
            Parser parser = new Parser(new Lexer(new FileReader(args[0])));
            parser.parse(); // El parse devuelve un ArbolSemantico

            // Guardar el árbol en un archivo
            //guardarArbolEnArchivo(arbol);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error: No fue posible leer del archivo de entrada: " + args[0]);
            System.exit(1);
        }
    }
    
   
}


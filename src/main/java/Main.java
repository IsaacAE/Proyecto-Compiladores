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
            arbol = parser.parse(); // El parse devuelve un ArbolSemantico

            // Guardar el árbol en un archivo
            guardarArbolEnArchivo(arbol);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error: No fue posible leer del archivo de entrada: " + args[0]);
            System.exit(1);
        }
    }
    
    // Método para guardar el árbol en el archivo ASA.txt
    public static void guardarArbolEnArchivo(ArbolSemantico arbol) {
        if (arbol == null) {
            System.err.println("Error: El árbol semántico no se ha generado.");
            return;
        }

        // Crear el archivo y el PrintWriter para escribir
        File archivo = new File("ASA.txt");
        try (PrintWriter writer = new PrintWriter(archivo)) {
            // Escribir el árbol en el archivo
            writer.print(arbol); // El método toString() se invoca automáticamente aquí
            System.out.println("El árbol ha sido guardado en ASA.txt.");
        } catch (IOException e) {
            System.err.println("Error al guardar el árbol en el archivo: " + e.getMessage());
        }
    }
}


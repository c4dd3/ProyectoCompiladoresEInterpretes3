package parser;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import scanner.Scanner;

public class MainParser {

    private static final boolean SHOW_TOKENS = false; // si luego querés mostrar tokens, lo vemos aparte

    public static void run(String sourcePath) {
        System.out.println("=== Analizando (P3): " + sourcePath + " ===");

        // Fase 1: reset de estructuras semánticas y generador
        SymbolTable.reset();
        SemanticAnalyzer.reset();
        CodeGenerator.reset();
        SemanticStack.reset();

        Scanner sc = null;
        Parser p = null;

        // 1) Construir scanner y parser
        try (BufferedReader br = Files.newBufferedReader(Paths.get(sourcePath), StandardCharsets.UTF_8)) {

            sc = new Scanner(br);   // scanner de JFlex
            p  = new Parser(sc);    // parser de CUP

            try {
                p.parse();          // corre el análisis sintáctico
            } catch (Exception ex) {
                System.err.println("[ABORT] CUP lanzó una excepción: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.err.println("[IO/RUN] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println();

        // 2) (Opcional) mostrar tokens aceptados por el scanner
        if (SHOW_TOKENS && sc != null) {
            System.out.println("=== TOKENS ACEPTADOS (scanner) ===");
            sc.imprimirTokens();
            System.out.println();
        }

        // 3) Errores léxicos
        System.out.println("=== ERRORES LEXICOS ===");
        if (sc != null && !sc.getErrores().isEmpty()) {
            for (String err : sc.getErrores()) {
                System.out.println(err);
            }
        } else {
            System.out.println("(ninguno)");
        }
        System.out.println();

        // 4) Errores sintácticos
        System.out.println("=== ERRORES SINTACTICOS ===");
        if (p != null && !p.getErroresSintacticos().isEmpty()) {
            for (String err : p.getErroresSintacticos()) {
                System.out.println(err);
            }
        } else {
            System.out.println("(ninguno)");
        }

        // 5) Errores semánticos
        System.out.println();
        System.out.println("=== ERRORES SEMANTICOS ===");
        SemanticAnalyzer.printErrors();

        // 6) Contenido de la tabla de símbolos
        System.out.println();
        SymbolTable.print();

        // 7) Decidir si se genera código
        boolean hayErroresLexicos   = (sc != null && !sc.getErrores().isEmpty());
        boolean hayErroresSintacticos = (p != null && !p.getErroresSintacticos().isEmpty());
        boolean hayErroresSemanticos = SemanticAnalyzer.hasErrors();

        System.out.println();
        System.out.println("=== RESUMEN ===");
        System.out.println("Errores léxicos   : " + (hayErroresLexicos ? sc.getErrores().size() : 0));
        System.out.println("Errores sintácticos: " + (hayErroresSintacticos ? p.getErroresSintacticos().size() : 0));
        System.out.println("Errores semánticos : " + (hayErroresSemanticos ? SemanticAnalyzer.getErrorCount() : 0));

        if (!hayErroresLexicos && !hayErroresSintacticos && !hayErroresSemanticos) {
            System.out.println();
            System.out.println("=== GENERACION DE CODIGO ===");

            // 7.1 Declarar variables globales (usa SymbolTable internamente)
            CodeGenerator.declareAllGlobalVariables();

            // 7.2 Construir nombre de archivo de salida (.asm)
            String outName;
            int dot = sourcePath.lastIndexOf('.');
            if (dot == -1) {
                outName = sourcePath + ".asm";
            } else {
                outName = sourcePath.substring(0, dot) + ".asm";
            }

            // 7.3 Generar archivo final
            CodeGenerator.generateFile(outName);

            // (Opcional) ver en consola lo generado
            // CodeGenerator.printGeneratedCode();
            // CodeGenerator.printStats();

        } else {
            System.out.println();
            System.out.println("✗ Hay errores en el programa. No se generará archivo de código.");
        }

        System.out.println();
        System.out.println("=== Fin del analisis P3 ===");
    }

    public static void main(String[] args) {
        String path = (args != null && args.length > 0)
                        ? args[0]
                        : "parser/testFile.abs";
        run(path);
    }
}

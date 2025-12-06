package parser;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import scanner.Scanner;

public class MainParser {

    private static final boolean SHOW_TOKENS = false; // si luego querés mostrar tokens, lo vemos aparte

    public static void run(String sourcePath) {
        System.out.println("=== Analizando (P2): " + sourcePath + " ===");

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

        // 4) Errores sintácticos (los que guarda tu parser.cup)
        System.out.println("=== ERRORES SINTACTICOS ===");
        if (p != null && !p.getErroresSintacticos().isEmpty()) {
            for (String err : p.getErroresSintacticos()) {
                System.out.println(err);
            }
        } else {
            System.out.println("(ninguno)");
        }

        System.out.println("=== Fin del analisis P2 ===");
    }

    public static void main(String[] args) {
        String path = (args != null && args.length > 0)
                        ? args[0]
                        : "parser/testFile.abs";
        run(path);
    }
}

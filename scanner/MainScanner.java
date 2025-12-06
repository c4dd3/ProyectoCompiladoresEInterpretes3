package scanner;

import java.io.FileReader;
import java.util.Scanner;
import java_cup.runtime.Symbol;
import parser.sym;

/**
 * Analizador léxico independiente para pruebas del scanner (P1)
 * Funciona con los tokens REALES definidos en scanner.flex y sym.java
 */
public class MainScanner {

    private static String nameOf(int id) {
        if (id >= 0 && id < sym.terminalNames.length) {
            return sym.terminalNames[id];
        }
        return "UNKNOWN(" + id + ")";
    }

    public static void main(String[] args) throws Exception {

        Scanner input = new Scanner(System.in);

        System.out.println("=== Pruebas del Analizador Léxico (Scanner) ===");
        System.out.print("Ingrese ruta del archivo a escanear (.abs): ");
        String path = input.nextLine();

        System.out.println("\n=== Escaneando: " + path + " ===\n");

        ScannerABS(path);

        System.out.println("\n=== Fin del análisis léxico ===\n");
    }

    private static void ScannerABS(String path) throws Exception {

        scanner.Scanner sc = new scanner.Scanner(new FileReader(path));

        while (true) {
            Symbol t = sc.next_token();

            String name = nameOf(t.sym);
            Object val = t.value;

            System.out.printf("(%3d:%-3d) %-20s %s\n",
                t.left, t.right, name,
                (val != null ? val.toString() : "")
            );

            if (t.sym == sym.EOF) break;
        }

        // Mostrar errores si existen
        System.out.println("\n--- ERRORES LÉXICOS ---");
        if (sc.getErrores().isEmpty()) {
            System.out.println("No se encontraron errores.");
        } else {
            sc.getErrores().forEach(System.out::println);
        }

        // Mostrar resumen de tokens válidos encontrados
        System.out.println("\n--- RESUMEN DE TOKENS ---");
        sc.imprimirTokens();
    }
}

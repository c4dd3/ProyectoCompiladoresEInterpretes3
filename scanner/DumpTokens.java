package scanner;

import java.io.FileReader;
import java_cup.runtime.Symbol;
import parser.sym;

public class DumpTokens {

    private static String nameOf(int id) {
        if (id >= 0 && id < sym.terminalNames.length) {
            return sym.terminalNames[id];
        }
        return "UNKNOWN(" + id + ")";
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("Uso: java scanner.DumpTokens <archivo.abs>");
            System.exit(1);
        }

        scanner.Scanner sc = new scanner.Scanner(new FileReader(args[0]));

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

        System.out.println("\n--- ERRORES LÉXICOS ---");
        if (sc.getErrores().isEmpty()) {
            System.out.println("No se encontraron errores.");
        } else {
            sc.getErrores().forEach(System.out::println);
        }

        System.out.println("\n--- RESUMEN DE TOKENS ---");
        sc.imprimirTokens();
    }
}

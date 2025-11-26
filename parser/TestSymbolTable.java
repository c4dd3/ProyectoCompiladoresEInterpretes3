package parser;

public class TestSymbolTable {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE TABLA DE SÍMBOLOS ===\n");
        
        // Reset inicial
        SymbolTable.reset();
        
        // ===== PRUEBA 1: Variables globales =====
        System.out.println("--- Prueba 1: Variables Globales ---");
        SymbolTable.addVariable("x", "INT", 5);
        SymbolTable.addVariable("y", "REAL", 6);
        SymbolTable.addVariable("nombre", "STRING", 7);
        
        System.out.println("¿Existe 'x'? " + SymbolTable.exists("x"));
        System.out.println("¿Existe 'z'? " + SymbolTable.exists("z"));
        
        // ===== PRUEBA 2: Doble definición (debe fallar) =====
        System.out.println("\n--- Prueba 2: Doble Definición ---");
        boolean added = SymbolTable.addVariable("x", "INT", 10);
        System.out.println("¿Se agregó 'x' de nuevo? " + added + " (esperado: false)");
        
        // ===== PRUEBA 3: Funciones =====
        System.out.println("\n--- Prueba 3: Funciones ---");
        SymbolTable.addFunction("suma", 15, 
            java.util.Arrays.asList("a", "b"),
            java.util.Arrays.asList("INT", "INT"),
            "INT"
        );
        
        SymbolTable.addProcedure("imprimir", 20,
            java.util.Arrays.asList("msg"),
            java.util.Arrays.asList("STRING")
        );
        
        // ===== PRUEBA 4: Variables locales en función =====
        System.out.println("\n--- Prueba 4: Variables Locales ---");
        SymbolTable.enterScope("suma");
        SymbolTable.addParameter("a", "INT", 16);
        SymbolTable.addParameter("b", "INT", 16);
        SymbolTable.addVariable("temp", "INT", 17);
        
        System.out.println("En scope 'suma', ¿existe 'temp'? " + SymbolTable.exists("temp"));
        System.out.println("En scope 'suma', ¿existe 'x' (global)? " + SymbolTable.exists("x"));
        
        SymbolTable.exitScope();
        System.out.println("Salimos de 'suma', ¿existe 'temp'? " + SymbolTable.exists("temp") + " (esperado: false)");
        
        // ===== PRUEBA 5: MAIN =====
        System.out.println("\n--- Prueba 5: MAIN ---");
        SymbolTable.enterScope("MAIN");
        SymbolTable.addVariable("contador", "INT", 30);
        SymbolTable.addVariable("x", "REAL", 31);  // redefinición local de 'x'
        
        SymbolTable.Symbol xMain = SymbolTable.lookupInCurrentScope("x");
        System.out.println("'x' en MAIN es de tipo: " + xMain.type + " (esperado: REAL)");
        
        SymbolTable.Symbol xGlobal = SymbolTable.lookupInScope("x", "GLOBAL");
        System.out.println("'x' en GLOBAL es de tipo: " + xGlobal.type + " (esperado: INT)");
        
        SymbolTable.exitScope();
        
        // ===== IMPRESIÓN FINAL =====
        SymbolTable.print();
        SymbolTable.printGlobalVariables();
        SymbolTable.printFunctions();
        
        // Exportar
        SymbolTable.exportToFile("tabla_simbolos.txt");
    }
}
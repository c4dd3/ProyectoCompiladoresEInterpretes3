package parser;

import java.util.*;

/**
 * Analizador Semántico para el Proyecto 3
 * Coordina las verificaciones semánticas usando SymbolTable
 * y gestiona los errores encontrados.
 */
public class SemanticAnalyzer {
    
    // ========================================================================
    // ALMACENAMIENTO DE ERRORES
    // ========================================================================
    
    private static List<SemanticError> errors = new ArrayList<>();
    
    /**
     * Clase interna para representar un error semántico
     */
    public static class SemanticError {
        public int line;
        public String message;
        public String type;  // "VAR_NO_DEFINIDA", "DOBLE_DEFINICION", etc.
        
        public SemanticError(int line, String message, String type) {
            this.line = line;
            this.message = message;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return String.format("Línea %d: %s", line, message);
        }
    }
    
    // ========================================================================
    // INICIALIZACIÓN
    // ========================================================================
    
    /**
     * Reinicia el analizador (para nuevos análisis)
     */
    public static void reset() {
        errors.clear();
    }
    
    /**
     * Agrega un error semántico
     */
    public static void addError(int line, String message, String type) {
        errors.add(new SemanticError(line, message, type));
        System.err.println("[SEMANTIC ERROR] Línea " + line + ": " + message);
    }
    
    /**
     * Agrega un error sin tipo específico
     */
    public static void addError(String message) {
        errors.add(new SemanticError(-1, message, "GENERAL"));
        System.err.println("[SEMANTIC ERROR] " + message);
    }
    
    /**
     * Verifica si hay errores
     */
    public static boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Obtiene la cantidad de errores
     */
    public static int getErrorCount() {
        return errors.size();
    }

    // ========================================================================
    // REPORTES
    // ========================================================================
    
    /**
     * Imprime todos los errores semánticos
     */
    public static void printErrors() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    ERRORES SEMÁNTICOS");
        System.out.println("=".repeat(80));
        
        if (errors.isEmpty()) {
            System.out.println("✓ No se encontraron errores semánticos");
            System.out.println("=".repeat(80));
            return;
        }
        
        // Agrupar por tipo
        Map<String, List<SemanticError>> byType = new LinkedHashMap<>();
        for (SemanticError err : errors) {
            byType.computeIfAbsent(err.type, k -> new ArrayList<>()).add(err);
        }
        
        // Imprimir por tipo
        for (Map.Entry<String, List<SemanticError>> entry : byType.entrySet()) {
            System.out.println("\n--- " + entry.getKey() + " ---");
            for (SemanticError err : entry.getValue()) {
                System.out.println("  " + err);
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Total de errores: " + errors.size());
        System.out.println("=".repeat(80));
    }
    
    /**
     * Imprime un resumen corto
     */
    public static void printSummary() {
        if (errors.isEmpty()) {
            System.out.println("✓ Análisis semántico exitoso (0 errores)");
        } else {
            System.out.println("✗ Análisis semántico con " + errors.size() + " error(es)");
        }
    }
    
    /**
     * Exporta errores a un archivo
     */
    public static void exportErrors(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("ERRORES SEMÁNTICOS");
            writer.println("=".repeat(80));
            
            if (errors.isEmpty()) {
                writer.println("No se encontraron errores");
            } else {
                for (SemanticError err : errors) {
                    writer.println(err);
                }
                writer.println("\nTotal: " + errors.size() + " errores");
            }
            
            System.out.println("[INFO] Errores exportados a: " + filename);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo exportar: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene lista de errores (para testing)
     */
    public static List<SemanticError> getErrors() {
        return new ArrayList<>(errors);
    }
}
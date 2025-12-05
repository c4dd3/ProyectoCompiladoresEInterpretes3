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
    
    
}
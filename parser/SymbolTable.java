package parser;

import java.util.*;

/**
 * Tabla de Símbolos para el Proyecto 3 - Análisis Semántico
 * Almacena variables, funciones, procedimientos y parámetros
 * con soporte para múltiples ámbitos (scopes).
 */
public class SymbolTable {
    
    // ========================================================================
    // CLASE INTERNA: Symbol (representa una entrada en la tabla)
    // ========================================================================
    
    public static class Symbol {
        // Información básica
        public String name;        // Nombre del identificador
        public String type;        // "INT", "REAL", "CHAR", "STRING"
        public String scope;       // "GLOBAL", "MAIN", "nombreFuncion"
        public String category;    // "VAR", "FUNCTION", "PROCEDURE", "PARAM"
        public int line;           // Línea donde fue declarado
        
        // Información extra para funciones/procedimientos
        public List<String> paramTypes;   // Tipos de parámetros ["INT", "REAL"]
        public List<String> paramNames;   // Nombres de parámetros ["x", "y"]
        public String returnType;         // Tipo de retorno (null si PROCEDURE)
        
        /**
         * Constructor básico para variables y parámetros
         */
        public Symbol(String name, String type, String scope, String category, int line) {
            this.name = name;
            this.type = type;
            this.scope = scope;
            this.category = category;
            this.line = line;
            this.paramTypes = new ArrayList<>();
            this.paramNames = new ArrayList<>();
            this.returnType = null;
        }
        
        /**
         * Constructor para funciones/procedimientos
         */
        public Symbol(String name, String scope, String category, int line,
                     List<String> paramNames, List<String> paramTypes, String returnType) {
            this.name = name;
            this.type = returnType;  // Para funciones, type = returnType
            this.scope = scope;
            this.category = category;
            this.line = line;
            this.paramNames = new ArrayList<>(paramNames);
            this.paramTypes = new ArrayList<>(paramTypes);
            this.returnType = returnType;
        }
        
        /**
         * Representación en string para debugging/reportes
         */
        @Override
        public String toString() {
            String params = "";
            if (!paramTypes.isEmpty()) {
                params = " (" + String.join(", ", paramTypes) + ")";
            }
            return String.format("%-15s %-10s %-15s %-12s %-6d%s", 
                name, 
                (type != null ? type : "-"), 
                scope, 
                category, 
                line,
                params
            );
        }
        
        /**
         * Compara si dos símbolos son iguales (por nombre y scope)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Symbol)) return false;
            Symbol other = (Symbol) obj;
            return this.name.equals(other.name) && this.scope.equals(other.scope);
        }
        
        @Override
        public int hashCode() {
            return (scope + "." + name).hashCode();
        }
    }
    
    // ========================================================================
    // ESTRUCTURA DE LA TABLA
    // ========================================================================
    
    /**
     * Tabla principal: clave = "scope.nombre", valor = Symbol
     * Ejemplo: "GLOBAL.x", "miFuncion.y", "MAIN.contador"
     */
    private static Map<String, Symbol> table = new LinkedHashMap<>();
    
    /**
     * Pila de scopes para manejar ámbitos anidados
     * Ejemplo: ["GLOBAL"] -> ["GLOBAL", "miFuncion"] -> ["GLOBAL", "MAIN"]
     */
    private static Deque<String> scopeStack = new ArrayDeque<>();
    
    /**
     * Scope actual (el tope de la pila)
     */
    private static String currentScope = "GLOBAL";
    
    // ========================================================================
    // MÉTODOS DE INICIALIZACIÓN Y GESTIÓN DE SCOPES
    // ========================================================================
    
    /**
     * Reinicia la tabla (útil para ejecutar múltiples análisis)
     */
    public static void reset() {
        table.clear();
        scopeStack.clear();
        scopeStack.push("GLOBAL");
        currentScope = "GLOBAL";
    }
    
    /**
     * Entra a un nuevo scope (cuando empieza una función o MAIN)
     */
    public static void enterScope(String scope) {
        scopeStack.push(scope);
        currentScope = scope;
        System.out.println("[DEBUG] Entrando a scope: " + scope + " | Pila: " + scopeStack);
    }
    
    /**
     * Sale del scope actual (cuando termina una función o MAIN)
     */
    public static void exitScope() {
        if (scopeStack.size() > 1) {  // Nunca sacar GLOBAL
            String exited = scopeStack.pop();
            currentScope = scopeStack.peek();
            System.out.println("[DEBUG] Saliendo de scope: " + exited + " | Actual: " + currentScope);
        } else {
            System.err.println("[WARNING] Intento de salir de GLOBAL ignorado");
        }
    }
    
    /**
     * Obtiene el scope actual
     */
    public static String getCurrentScope() {
        return currentScope;
    }
    
    /**
     * Obtiene la pila de scopes (para debugging)
     */
    public static List<String> getScopeStack() {
        return new ArrayList<>(scopeStack);
    }
    
    // ========================================================================
    // MÉTODOS DE INSERCIÓN
    // ========================================================================
    
    /**
     * Agrega un símbolo a la tabla
     * @return true si se agregó correctamente, false si ya existía en ese scope
     */
    public static boolean add(Symbol s) {
        String key = makeKey(s.scope, s.name);
        
        if (table.containsKey(key)) {
            return false;  // Ya existe
        }
        
        table.put(key, s);
        System.out.println("[DEBUG] Agregado: " + key + " -> " + s.category + " " + s.type);
        return true;
    }
    
    /**
     * Agrega una variable simple
     */
    public static boolean addVariable(String name, String type, int line) {
        Symbol s = new Symbol(name, type, currentScope, "VAR", line);
        return add(s);
    }
    
    /**
     * Agrega un parámetro de función
     */
    public static boolean addParameter(String name, String type, int line) {
        Symbol s = new Symbol(name, type, currentScope, "PARAM", line);
        return add(s);
    }
    
    /**
     * Agrega una función
     */
    public static boolean addFunction(String name, int line, 
                                     List<String> paramNames, 
                                     List<String> paramTypes, 
                                     String returnType) {
        Symbol s = new Symbol(name, "GLOBAL", "FUNCTION", line, 
                             paramNames, paramTypes, returnType);
        return add(s);
    }
    
    /**
     * Agrega un procedimiento (función sin retorno)
     */
    public static boolean addProcedure(String name, int line, 
                                      List<String> paramNames, 
                                      List<String> paramTypes) {
        Symbol s = new Symbol(name, "GLOBAL", "PROCEDURE", line, 
                             paramNames, paramTypes, null);
        return add(s);
    }
    
    // ========================================================================
    // MÉTODOS DE BÚSQUEDA
    // ========================================================================
    
    /**
     * Busca un símbolo siguiendo las reglas de scope:
     * 1. Busca en el scope actual
     * 2. Si no lo encuentra, busca en GLOBAL
     * 3. Si no existe, retorna null
     */
    public static Symbol lookup(String name) {
        // 1. Buscar en scope actual
        String key = makeKey(currentScope, name);
        Symbol s = table.get(key);
        
        // 2. Si no está y no estamos en GLOBAL, buscar en GLOBAL
        if (s == null && !currentScope.equals("GLOBAL")) {
            key = makeKey("GLOBAL", name);
            s = table.get(key);
        }
        
        return s;
    }
    
    /**
     * Busca un símbolo SOLO en el scope actual
     * (útil para detectar dobles definiciones)
     */
    public static Symbol lookupInCurrentScope(String name) {
        String key = makeKey(currentScope, name);
        return table.get(key);
    }
    
    /**
     * Busca un símbolo en un scope específico
     */
    public static Symbol lookupInScope(String name, String scope) {
        String key = makeKey(scope, name);
        return table.get(key);
    }
    
    /**
     * Verifica si un símbolo existe (en cualquier scope alcanzable)
     */
    public static boolean exists(String name) {
        return lookup(name) != null;
    }
    
    // ========================================================================
    // MÉTODOS DE CONSULTA
    // ========================================================================
    
    /**
     * Obtiene todos los símbolos de un scope específico
     */
    public static List<Symbol> getSymbolsInScope(String scope) {
        List<Symbol> result = new ArrayList<>();
        String prefix = scope + ".";
        
        for (Map.Entry<String, Symbol> entry : table.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Obtiene todas las variables globales
     */
    public static List<Symbol> getGlobalVariables() {
        List<Symbol> globals = new ArrayList<>();
        for (Symbol s : getSymbolsInScope("GLOBAL")) {
            if (s.category.equals("VAR")) {
                globals.add(s);
            }
        }
        return globals;
    }
    
    /**
     * Obtiene todas las funciones y procedimientos
     */
    public static List<Symbol> getFunctionsAndProcedures() {
        List<Symbol> funcs = new ArrayList<>();
        for (Symbol s : getSymbolsInScope("GLOBAL")) {
            if (s.category.equals("FUNCTION") || s.category.equals("PROCEDURE")) {
                funcs.add(s);
            }
        }
        return funcs;
    }
    
    /**
     * Cuenta cuántos símbolos hay en la tabla
     */
    public static int size() {
        return table.size();
    }
    
    /**
     * Verifica si la tabla está vacía
     */
    public static boolean isEmpty() {
        return table.isEmpty();
    }
    
    // ========================================================================
    // MÉTODOS DE IMPRESIÓN Y DEBUGGING
    // ========================================================================
    
    /**
     * Imprime toda la tabla de símbolos (para el reporte final)
     */
    public static void print() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        TABLA DE SÍMBOLOS");
        System.out.println("=".repeat(80));
        
        if (table.isEmpty()) {
            System.out.println("(vacía)");
            return;
        }
        
        // Cabecera
        System.out.printf("%-15s %-10s %-15s %-12s %-6s %s%n", 
            "Nombre", "Tipo", "Ámbito", "Categoría", "Línea", "Info Extra");
        System.out.println("-".repeat(80));
        
        // Ordenar por scope y luego por nombre
        List<Symbol> sorted = new ArrayList<>(table.values());
        sorted.sort((a, b) -> {
            int scopeCmp = a.scope.compareTo(b.scope);
            return (scopeCmp != 0) ? scopeCmp : a.name.compareTo(b.name);
        });
        
        // Imprimir agrupado por scope
        String lastScope = "";
        for (Symbol s : sorted) {
            if (!s.scope.equals(lastScope)) {
                if (!lastScope.isEmpty()) {
                    System.out.println();  // línea en blanco entre scopes
                }
                lastScope = s.scope;
            }
            System.out.println(s);
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Total de símbolos: " + table.size());
    }
    
    /**
     * Imprime solo las variables globales (útil para generación de código)
     */
    public static void printGlobalVariables() {
        System.out.println("\n=== VARIABLES GLOBALES ===");
        List<Symbol> globals = getGlobalVariables();
        
        if (globals.isEmpty()) {
            System.out.println("(ninguna)");
            return;
        }
        
        System.out.printf("%-15s %-10s %-6s%n", "Nombre", "Tipo", "Línea");
        System.out.println("-".repeat(35));
        for (Symbol s : globals) {
            System.out.printf("%-15s %-10s %-6d%n", s.name, s.type, s.line);
        }
    }
    
    /**
     * Imprime solo funciones y procedimientos
     */
    public static void printFunctions() {
        System.out.println("\n=== FUNCIONES Y PROCEDIMIENTOS ===");
        List<Symbol> funcs = getFunctionsAndProcedures();
        
        if (funcs.isEmpty()) {
            System.out.println("(ninguno)");
            return;
        }
        
        for (Symbol f : funcs) {
            String params = f.paramNames.isEmpty() ? "()" : 
                "(" + String.join(", ", 
                    java.util.stream.IntStream.range(0, f.paramNames.size())
                        .mapToObj(i -> f.paramTypes.get(i) + " " + f.paramNames.get(i))
                        .toArray(String[]::new)
                ) + ")";
            
            String ret = f.returnType != null ? " : " + f.returnType : "";
            System.out.printf("%-15s %s%s (línea %d)%n", 
                f.name, params, ret, f.line);
        }
    }
    
    /**
     * Exporta la tabla a un archivo de texto
     */
    public static void exportToFile(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("TABLA DE SÍMBOLOS");
            writer.println("=".repeat(80));
            writer.printf("%-15s %-10s %-15s %-12s %-6s%n", 
                "Nombre", "Tipo", "Ámbito", "Categoría", "Línea");
            writer.println("-".repeat(80));
            
            for (Symbol s : table.values()) {
                writer.println(s);
            }
            
            writer.println("\nTotal: " + table.size() + " símbolos");
            System.out.println("[INFO] Tabla exportada a: " + filename);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo exportar: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================================================
    
    /**
     * Genera la clave única "scope.nombre"
     */
    private static String makeKey(String scope, String name) {
        return scope + "." + name.toLowerCase();  // case-insensitive
    }
    
    // ========================================================================
    // INICIALIZACIÓN ESTÁTICA
    // ========================================================================
    
    static {
        // Inicializar la tabla con GLOBAL como scope inicial
        scopeStack.push("GLOBAL");
        currentScope = "GLOBAL";
    }
}
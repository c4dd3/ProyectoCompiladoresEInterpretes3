package parser;

import java.util.*;

/**
 * Pila Semántica para evaluación de expresiones
 * y generación de código intermedio.
 */
public class SemanticStack {
    
    // ========================================================================
    // CLASE INTERNA: StackEntry (Registro Semántico)
    // ========================================================================
    
    public static class StackEntry {
        public String type;        // "INT", "REAL", "CHAR", "STRING", "BOOL"
        public String value;       // Nombre de variable o temporal ("a", "t1", "5")
        public boolean isConst;    // true si es una constante conocida
        public Object constValue;  // Valor real si isConst=true (Integer, Double, etc.)
        
        /**
         * Constructor para valores constantes
         */
        public StackEntry(String type, Object constValue) {
            this.type = type;
            this.value = String.valueOf(constValue);
            this.isConst = true;
            this.constValue = constValue;
        }
        
        /**
         * Constructor para variables/temporales
         */
        public StackEntry(String type, String value) {
            this.type = type;
            this.value = value;
            this.isConst = false;
            this.constValue = null;
        }
        
        /**
         * Obtiene el valor como entero (si es constante INT)
         */
        public int getIntValue() {
            if (isConst && type.equals("INT")) {
                return (Integer) constValue;
            }
            throw new RuntimeException("No es un INT constante");
        }
        
        /**
         * Obtiene el valor como real (si es constante REAL)
         */
        public double getRealValue() {
            if (isConst && type.equals("REAL")) {
                return (Double) constValue;
            }
            throw new RuntimeException("No es un REAL constante");
        }
        
        @Override
        public String toString() {
            if (isConst) {
                return String.format("Const(%s, %s)", type, constValue);
            } else {
                return String.format("Var(%s, %s)", type, value);
            }
        }
    }
    
    // ========================================================================
    // PILA SEMÁNTICA
    // ========================================================================
    
    private static Stack<StackEntry> stack = new Stack<>();
    
    /**
     * Reinicia la pila
     */
    public static void reset() {
        stack.clear();
    }
    
    /**
     * Agrega un elemento a la pila
     */
    public static void push(StackEntry entry) {
        stack.push(entry);
        System.out.println("[DEBUG STACK] Push: " + entry);
    }
    
    /**
     * Saca el tope de la pila
     */
    public static StackEntry pop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Pila semántica vacía (pop)");
        }
        StackEntry entry = stack.pop();
        System.out.println("[DEBUG STACK] Pop: " + entry);
        return entry;
    }
    
    /**
     * Ve el tope sin sacarlo
     */
    public static StackEntry peek() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Pila semántica vacía (peek)");
        }
        return stack.peek();
    }
    
    /**
     * Verifica si está vacía
     */
    public static boolean isEmpty() {
        return stack.isEmpty();
    }
    
    /**
     * Obtiene el tamaño actual
     */
    public static int size() {
        return stack.size();
    }
    
    /**
     * Imprime el estado actual (debugging)
     */
    public static void printStack() {
        System.out.println("[STACK] Contenido actual (" + stack.size() + " elementos):");
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.println("  [" + i + "] " + stack.get(i));
        }
    }
    
    // ========================================================================
    // MÉTODOS HELPER PARA EXPRESIONES
    // ========================================================================
    
    /**
     * Procesa una operación binaria aritmética (+, -, *, /, MOD, DIV)
     * Aplica Constant Folding si ambos operandos son constantes.
     */
    public static void processBinaryOp(String operator) {
        StackEntry right = pop();
        StackEntry left = pop();
        
        // Constant Folding: si ambos son constantes
        if (left.isConst && right.isConst && left.type.equals("INT") && right.type.equals("INT")) {
            int leftVal = left.getIntValue();
            int rightVal = right.getIntValue();
            int result = 0;
            
            switch (operator) {
                case "+" -> result = leftVal + rightVal;
                case "-" -> result = leftVal - rightVal;
                case "*" -> result = leftVal * rightVal;
                case "/" -> result = leftVal / rightVal;
                case "MOD" -> result = leftVal % rightVal;
                case "DIV" -> result = leftVal / rightVal;
            }
            
            System.out.println("[CONSTANT FOLDING] " + leftVal + " " + operator + " " + rightVal + " = " + result);
            push(new StackEntry("INT", result));
            return;
        }
        
        // Si no son constantes, generar código temporal
        String temp = CodeGenerator.newTemp();
        switch (operator) {
            case "+" -> CodeGenerator.emitAdd(temp, left.value, right.value);
            case "-" -> CodeGenerator.emitSub(temp, left.value, right.value);
            case "*" -> CodeGenerator.emitMul(temp, left.value, right.value);
            case "/" -> CodeGenerator.emitDiv(temp, left.value, right.value);
            case "MOD" -> CodeGenerator.emitMod(temp, left.value, right.value);
            default -> CodeGenerator.emitCode(temp + " = " + left.value + " " + operator + " " + right.value);
        }
        push(new StackEntry(left.type, temp));
    }
    
    /**
     * Procesa una operación relacional (=, <>, <, >, <=, >=)
     * Devuelve un valor booleano.
     */
    public static void processRelationalOp(String operator) {
        StackEntry right = pop();
        StackEntry left = pop();
        
        String temp = CodeGenerator.newTemp();
        if ("=".equals(operator)) {
            CodeGenerator.emitEqual(temp, left.value, right.value);
            push(new StackEntry("BOOL", temp));
        } else {
            CodeGenerator.emitCode(temp + " = " + left.value + " " + operator + " " + right.value);
            push(new StackEntry("BOOL", temp));
        }
    }
    
    /**
     * Procesa operadores unarios (++, --, NOT, -)
     */
    public static void processUnaryOp(String operator) {
        StackEntry operand = pop();
        
        if (operator.equals("NOT")) {
            String temp = CodeGenerator.newTemp();
            CodeGenerator.emitCode(temp + " = NOT " + operand.value);
            push(new StackEntry("BOOL", temp));
        } else if (operator.equals("-")) {
            if (operand.isConst && operand.type.equals("INT")) {
                // Constant Folding para negación
                int val = -operand.getIntValue();
                push(new StackEntry("INT", val));
            } else {
                String temp = CodeGenerator.newTemp();
                CodeGenerator.emitCode(temp + " = -" + operand.value);
                push(new StackEntry(operand.type, temp));
            }
        }
    }
    
    /**
     * Carga una variable desde SymbolTable a la pila
     */
    public static void loadVariable(String name, int line) {
        SymbolTable.Symbol sym = SymbolTable.lookup(name);
        if (sym == null) {
            // mismo tipo de error que estás usando en el parser.cup
            SemanticAnalyzer.addError(
                line,
                "Variable '" + name + "' no esta definida en un ambito visible",
                "VAR_NO_DEFINIDA"
            );
            // seguimos, pero marcamos el tipo como ERROR
            push(new StackEntry("ERROR", name));
        } else {
            push(new StackEntry(sym.type, name));
        }
    }

    
    /**
     * Carga una constante literal a la pila
     */
    public static void loadConstant(String type, String literal) {
        switch (type) {
            case "INT" -> push(new StackEntry("INT", Integer.parseInt(literal)));
            case "REAL" -> push(new StackEntry("REAL", Double.parseDouble(literal)));
            case "STRING" -> push(new StackEntry("STRING", literal));
            case "CHAR" -> push(new StackEntry("CHAR", literal.charAt(0)));
        }
    }
}

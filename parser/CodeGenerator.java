
package parser;

import java.io.*;
import java.util.*;

/**
 * Generador de Código para el Proyecto 3
 * Genera código ensamblador (x86 NASM) a partir del análisis semántico.
 * 
 * Solo genera:
 * - Variables globales
 * - Código del MAIN
 * - Asignaciones
 * - Expresiones aritméticas (+, -, ++, --)
 * - Expresión booleana (=)
 * - IF-ELSE
 * - WRITE(x)
 */
public class CodeGenerator {
    
    // ========================================================================
    // SECCIONES DEL CÓDIGO ENSAMBLADOR
    // ========================================================================
    
    private static List<String> dataSection = new ArrayList<>();      // .data
    private static List<String> bssSection = new ArrayList<>();       // .bss
    private static List<String> textSection = new ArrayList<>();      // .text (código)
    
    // ========================================================================
    // CONTADORES PARA TEMPORALES Y LABELS
    // ========================================================================
    
    private static Set<String> declaredGlobals = new LinkedHashSet<>(); // seguimiento de globals
    private static int tempCounter = 0;
    private static int labelCounter = 0;
    
    // ========================================================================
    // CONFIGURACIÓN
    // ========================================================================
    
    private static boolean isLinux = true;  // true=Linux, false=Windows
    
    /**
     * Configura el sistema operativo objetivo
     */
    public static void setTargetOS(boolean linux) {
        isLinux = linux;
    }
    
    // ========================================================================
    // INICIALIZACIÓN
    // ========================================================================
    
    /**
     * Reinicia el generador (para nuevos análisis)
     */
    public static void reset() {
        dataSection.clear();
        bssSection.clear();
        textSection.clear();
        declaredGlobals.clear();
        tempCounter = 0;
        labelCounter = 0;
        
        // Agregar mensajes predefinidos para WRITE
        addData("newline", "DB 10, 0");  // Salto de línea
        addData("fmt_int", "DB '%d', 10, 0");  // Formato para enteros
        addData("fmt_str", "DB '%s', 10, 0");  // Formato para strings
    }
    
    // ========================================================================
    // GENERACIÓN DE TEMPORALES Y LABELS
    // ========================================================================
    
    /**
     * Genera un nuevo temporal (t0, t1, t2, ...)
     */
    public static String newTemp() {
        String temp = "t" + tempCounter;
        tempCounter++;
        
        // Declarar el temporal en .bss (sin inicializar)
        addBss(temp, "RESD 1");  // Reservar 4 bytes para INT
        
        return temp;
    }
    
    /**
     * Genera un nuevo label (L0, L1, L2, ...)
     */
    public static String newLabel() {
        String label = "L" + labelCounter;
        labelCounter++;
        return label;
    }
    
    // ========================================================================
    // EMISIÓN DE CÓDIGO
    // ========================================================================
    
    /**
     * Agrega una línea a la sección .data
     */
    public static void addData(String label, String declaration) {
        dataSection.add(label + " " + declaration);
    }
    
    /**
     * Agrega una línea a la sección .bss
     */
    public static void addBss(String label, String declaration) {
        bssSection.add(label + " " + declaration);
    }
    
    /**
     * Agrega una instrucción a la sección .text (con indentación)
     */
    public static void emitCode(String instruction) {
        textSection.add("    " + instruction);
    }
    
    /**
     * Agrega una instrucción sin indentación (para labels)
     */
    public static void emitLabel(String label) {
        textSection.add(label + ":");
    }
    
    /**
     * Agrega un comentario
     */
    public static void emitComment(String comment) {
        textSection.add("    ; " + comment);
    }
    
    // ========================================================================
    // 1. VARIABLES GLOBALES
    // ========================================================================
    
    /**
     * Declara una variable global en la sección .bss
     */
    public static void declareGlobalVariable(String name, String type) {
        String key = name.toLowerCase();
        if (declaredGlobals.contains(key)) {
            return;  // evitar doble emisión
        }
        declaredGlobals.add(key);

        String declaration = switch (type) {
            case "INT" -> "RESD 1";          // 4 bytes (entero)
            case "REAL" -> "RESQ 1";         // 8 bytes (double)
            case "CHAR" -> "RESB 1";         // 1 byte
            case "STRING" -> "RESB 256";     // 256 bytes
            default -> "RESD 1";
        };
        
        addBss(name, declaration);
        System.out.println("[CODE GEN] Variable global declarada: " + name + " (" + type + ")");
    }
    
    /**
     * Declara todas las variables globales de la tabla de símbolos
     */
    public static void declareAllGlobalVariables() {
        List<SymbolTable.Symbol> globals = SymbolTable.getGlobalVariables();
        
        if (globals.isEmpty()) {
            emitComment("No hay variables globales");
            return;
        }
        
        emitComment("=== Variables Globales ===");
        for (SymbolTable.Symbol var : globals) {
            declareGlobalVariable(var.name, var.type);
        }
    }
    
    // ========================================================================
    // 2. ASIGNACIONES
    // ========================================================================
    
    /**
     * Genera código para una asignación simple: var = valor
     */
    public static void emitAssignment(String varName, String value) {
        emitComment("Asignación: " + varName + " = " + value);
        emitCode("MOV EAX, [" + value + "]");
        emitCode("MOV [" + varName + "], EAX");
    }
    
    /**
     * Genera código para asignar una constante: var = 5
     */
    public static void emitAssignmentConst(String varName, int value) {
        emitComment("Asignación: " + varName + " = " + value);
        emitCode("MOV DWORD [" + varName + "], " + value);
    }
    
    /**
     * Genera código para asignar desde un temporal: var = t1
     */
    public static void emitAssignmentFromTemp(String varName, String tempName) {
        emitComment("Asignación: " + varName + " = " + tempName);
        emitCode("MOV EAX, [" + tempName + "]");
        emitCode("MOV [" + varName + "], EAX");
    }
    
    // ========================================================================
    // 3. EXPRESIONES ARITMÉTICAS
    // ========================================================================
    
    /**
     * Genera código para suma: temp = op1 + op2
     */
    public static void emitAdd(String result, String op1, String op2) {
        emitComment(result + " = " + op1 + " + " + op2);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("ADD EAX, [" + op2 + "]");
        emitCode("MOV [" + result + "], EAX");
    }
    
    /**
     * Genera código para resta: temp = op1 - op2
     */
    public static void emitSub(String result, String op1, String op2) {
        emitComment(result + " = " + op1 + " - " + op2);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("SUB EAX, [" + op2 + "]");
        emitCode("MOV [" + result + "], EAX");
    }
    
    /**
     * Genera código para multiplicación: temp = op1 * op2
     */
    public static void emitMul(String result, String op1, String op2) {
        emitComment(result + " = " + op1 + " * " + op2);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("IMUL EAX, [" + op2 + "]");
        emitCode("MOV [" + result + "], EAX");
    }
    
    /**
     * Genera código para división: temp = op1 / op2
     */
    public static void emitDiv(String result, String op1, String op2) {
        emitComment(result + " = " + op1 + " / " + op2);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("CDQ");  // Extender signo a EDX:EAX
        emitCode("IDIV DWORD [" + op2 + "]");
        emitCode("MOV [" + result + "], EAX");
    }
    
    /**
     * Genera código para módulo: temp = op1 MOD op2
     */
    public static void emitMod(String result, String op1, String op2) {
        emitComment(result + " = " + op1 + " MOD " + op2);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("CDQ");
        emitCode("IDIV DWORD [" + op2 + "]");
        emitCode("MOV [" + result + "], EDX");  // El residuo está en EDX
    }
    
    /**
     * Genera código para incremento: var++
     */
    public static void emitIncrement(String varName) {
        emitComment(varName + "++");
        emitCode("INC DWORD [" + varName + "]");
    }
    
    /**
     * Genera código para decremento: var--
     */
    public static void emitDecrement(String varName) {
        emitComment(varName + "--");
        emitCode("DEC DWORD [" + varName + "]");
    }
    
    // ========================================================================
    // 4. EXPRESIONES BOOLEANAS (solo =)
    // ========================================================================
    
    /**
     * Genera código para comparación de igualdad: temp = (op1 == op2)
     * Devuelve 1 si son iguales, 0 si no
     */
    public static void emitEqual(String result, String op1, String op2) {
        emitComment(result + " = (" + op1 + " == " + op2 + ")");
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("CMP EAX, [" + op2 + "]");
        emitCode("SETE AL");          // AL = 1 si igual, 0 si no
        emitCode("MOVZX EAX, AL");    // Extender AL a EAX
        emitCode("MOV [" + result + "], EAX");
    }
    
    /**
     * Genera código para salto condicional basado en comparación
     * if (op1 == op2) goto label
     */
    public static void emitIfEqual(String op1, String op2, String label) {
        emitComment("if (" + op1 + " == " + op2 + ") goto " + label);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("CMP EAX, [" + op2 + "]");
        emitCode("JE " + label);
    }
    
    /**
     * Genera código para salto condicional negado
     * if (op1 != op2) goto label
     */
    public static void emitIfNotEqual(String op1, String op2, String label) {
        emitComment("if (" + op1 + " != " + op2 + ") goto " + label);
        emitCode("MOV EAX, [" + op1 + "]");
        emitCode("CMP EAX, [" + op2 + "]");
        emitCode("JNE " + label);
    }
    
    /**
     * Evalúa una condición booleana y salta si es falsa
     * if (!condition) goto label
     */
    public static void emitIfFalse(String condition, String label) {
        emitComment("if (!" + condition + ") goto " + label);
        emitCode("CMP DWORD [" + condition + "], 0");
        emitCode("JE " + label);
    }
    
    // ========================================================================
    // 5. IF-ELSE
    // ========================================================================
    
    /**
     * Inicia un IF: evalúa condición y salta al else/end si es falsa
     * Retorna el label de salto
     */
    public static String emitIfStart(String condition) {
        String labelFalse = newLabel();
        emitComment("=== IF ===");
        emitIfFalse(condition, labelFalse);
        return labelFalse;
    }
    
    /**
     * Genera el código entre IF y ELSE
     */
    public static String emitElse(String labelFalse) {
        String labelEnd = newLabel();
        emitCode("JMP " + labelEnd);     // Saltar al final después del THEN
        emitLabel(labelFalse);           // Inicio del ELSE
        emitComment("=== ELSE ===");
        return labelEnd;
    }
    
    /**
     * Termina un IF (sin ELSE)
     */
    public static void emitIfEnd(String labelFalse) {
        emitLabel(labelFalse);
        emitComment("=== END IF ===");
    }
    
    /**
     * Termina un IF-ELSE
     */
    public static void emitIfElseEnd(String labelEnd) {
        emitLabel(labelEnd);
        emitComment("=== END IF-ELSE ===");
    }
    
    // ========================================================================
    // 6. WRITE (Imprimir en pantalla)
    // ========================================================================
    
    /**
     * Genera código para WRITE(variable_entera)
     */
    public static void emitWriteInt(String varName) {
        emitComment("WRITE(" + varName + ")");
        
        if (isLinux) {
            // Linux: usar printf de C
            emitCode("PUSH DWORD [" + varName + "]");
            emitCode("PUSH fmt_int");
            emitCode("CALL printf");
            emitCode("ADD ESP, 8");  // Limpiar pila (2 argumentos * 4 bytes)
        } else {
            // Windows: usar printf de msvcrt.dll
            emitCode("PUSH DWORD [" + varName + "]");
            emitCode("PUSH fmt_int");
            emitCode("CALL [printf]");
            emitCode("ADD ESP, 8");
        }
    }
    
    /**
     * Genera código para WRITE(string_literal)
     */
    public static void emitWriteString(String stringLabel) {
        emitComment("WRITE(string)");
        
        if (isLinux) {
            emitCode("PUSH " + stringLabel);
            emitCode("PUSH fmt_str");
            emitCode("CALL printf");
            emitCode("ADD ESP, 8");
        } else {
            emitCode("PUSH " + stringLabel);
            emitCode("PUSH fmt_str");
            emitCode("CALL [printf]");
            emitCode("ADD ESP, 8");
        }
    }
    
    /**
     * Agrega un string literal a .data y retorna su label
     */
    public static String addStringLiteral(String content) {
        String label = "str" + labelCounter;
        labelCounter++;
        
        // Escapar caracteres especiales
        content = content.replace("\\n", "\", 10, \"");
        content = content.replace("\\t", "\", 9, \"");
        
        addData(label, "DB \"" + content + "\", 0");
        return label;
    }
    
    // ========================================================================
    // 7. GENERACIÓN DEL ARCHIVO FINAL
    // ========================================================================
    
    /**
     * Genera el archivo .asm completo
     */
    public static void generateFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            
            // ===== CABECERA =====
            writer.println("; Código generado por el Compilador ABS");
            writer.println("; Proyecto 3 - Compiladores e Intérpretes");
            writer.println("; Archivo: " + filename);
            writer.println();
            
            // ===== DECLARACIONES EXTERNAS =====
            if (isLinux) {
                writer.println("extern printf");
                writer.println("extern exit");
            } else {
                writer.println("extern _printf");
                writer.println("extern _exit");
            }
            writer.println();
            
            // ===== SECCIÓN .data =====
            writer.println("section .data");
            if (dataSection.isEmpty()) {
                writer.println("    ; (vacía)");
            } else {
                for (String line : dataSection) {
                    writer.println("    " + line);
                }
            }
            writer.println();
            
            // ===== SECCIÓN .bss =====
            writer.println("section .bss");
            if (bssSection.isEmpty()) {
                writer.println("    ; (vacía)");
            } else {
                for (String line : bssSection) {
                    writer.println("    " + line);
                }
            }
            writer.println();
            
            // ===== SECCIÓN .text (código) =====
            writer.println("section .text");
            if (isLinux) {
                writer.println("global _start");
                writer.println();
                writer.println("_start:");
            } else {
                writer.println("global _main");
                writer.println();
                writer.println("_main:");
            }
            
            if (textSection.isEmpty()) {
                writer.println("    ; (vacío)");
            } else {
                for (String line : textSection) {
                    writer.println(line);
                }
            }
            
            // ===== SALIDA DEL PROGRAMA =====
            writer.println();
            writer.println("    ; Salir del programa");
            if (isLinux) {
                writer.println("    MOV EAX, 1");      // syscall: exit
                writer.println("    XOR EBX, EBX");    // código de salida: 0
                writer.println("    INT 0x80");        // llamada al sistema
            } else {
                writer.println("    PUSH 0");
                writer.println("    CALL [exit]");
            }
            
            System.out.println("\n[CODE GEN] ✓ Archivo generado exitosamente: " + filename);
            System.out.println("           - Variables globales: " + bssSection.size());
            System.out.println("           - Instrucciones: " + textSection.size());
            
        } catch (IOException e) {
            System.err.println("[CODE GEN] ✗ Error al escribir el archivo: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // 8. MÉTODOS DE DEBUGGING
    // ========================================================================
    
    /**
     * Imprime el código generado (para debugging)
     */
    public static void printGeneratedCode() {
        System.out.println("\n=== CÓDIGO GENERADO ===");
        
        System.out.println("\n--- .data ---");
        if (dataSection.isEmpty()) {
            System.out.println("(vacía)");
        } else {
            for (String line : dataSection) {
                System.out.println("  " + line);
            }
        }
        
        System.out.println("\n--- .bss ---");
        if (bssSection.isEmpty()) {
            System.out.println("(vacía)");
        } else {
            for (String line : bssSection) {
                System.out.println("  " + line);
            }
        }
        
        System.out.println("\n--- .text ---");
        if (textSection.isEmpty()) {
            System.out.println("(vacía)");
        } else {
            for (String line : textSection) {
                System.out.println(line);
            }
        }
    }
    
    /**
     * Obtiene estadísticas del código generado
     */
    public static void printStats() {
        System.out.println("\n=== ESTADÍSTICAS DE GENERACIÓN ===");
        System.out.println("Variables globales declaradas: " + bssSection.size());
        System.out.println("Constantes en .data: " + dataSection.size());
        System.out.println("Instrucciones generadas: " + textSection.size());
        System.out.println("Temporales usados: " + tempCounter);
        System.out.println("Labels generados: " + labelCounter);
    }
}

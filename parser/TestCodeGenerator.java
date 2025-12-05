package parser;

public class TestCodeGenerator {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEL GENERADOR DE CÓDIGO ===\n");
        
        // Reset
        CodeGenerator.reset();
        CodeGenerator.setTargetOS(true);  // Linux
        
        // ===== PRUEBA 1: Variables globales =====
        System.out.println("--- Generando variables globales ---");
        CodeGenerator.declareGlobalVariable("x", "INT");
        CodeGenerator.declareGlobalVariable("y", "INT");
        CodeGenerator.declareGlobalVariable("result", "INT");
        
        // ===== PRUEBA 2: Asignaciones =====
        System.out.println("\n--- Generando asignaciones ---");
        CodeGenerator.emitComment("=== MAIN ===");
        CodeGenerator.emitAssignmentConst("x", 5);
        CodeGenerator.emitAssignmentConst("y", 10);
        
        // ===== PRUEBA 3: Expresión aritmética =====
        System.out.println("\n--- Generando expresión: result = x + y * 2 ---");
        String t1 = CodeGenerator.newTemp();
        CodeGenerator.emitAssignmentConst(t1, 2);
        
        String t2 = CodeGenerator.newTemp();
        CodeGenerator.emitMul(t2, "y", t1);
        
        String t3 = CodeGenerator.newTemp();
        CodeGenerator.emitAdd(t3, "x", t2);
        
        CodeGenerator.emitAssignmentFromTemp("result", t3);
        
        // ===== PRUEBA 4: IF-ELSE =====
        System.out.println("\n--- Generando IF-ELSE ---");
        String condTemp = CodeGenerator.newTemp();
        CodeGenerator.emitEqual(condTemp, "x", "y");
        
        String labelFalse = CodeGenerator.emitIfStart(condTemp);
        CodeGenerator.emitWriteInt("x");
        
        String labelEnd = CodeGenerator.emitElse(labelFalse);
        CodeGenerator.emitWriteInt("y");
        
        CodeGenerator.emitIfElseEnd(labelEnd);
        
        // ===== PRUEBA 5: WRITE =====
        System.out.println("\n--- Generando WRITE ---");
        String strLabel = CodeGenerator.addStringLiteral("Resultado: ");
        CodeGenerator.emitWriteString(strLabel);
        CodeGenerator.emitWriteInt("result");
        
        // ===== PRUEBA 6: ++ y -- =====
        System.out.println("\n--- Generando incremento/decremento ---");
        CodeGenerator.emitIncrement("x");
        CodeGenerator.emitDecrement("y");
        
        // ===== Imprimir código generado =====
        CodeGenerator.printGeneratedCode();
        CodeGenerator.printStats();
        
        // ===== Generar archivo =====
        CodeGenerator.generateFile("output.asm");
        
        System.out.println("\n✓ Prueba completa");
    }
}
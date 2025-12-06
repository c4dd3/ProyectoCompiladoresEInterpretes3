/* ====== 1. Código de usuario ====== */
package scanner;
import java_cup.runtime.*;
import java.util.*;   // para ArrayList, HashMap, etc.
import java.io.*;     // para manejar archivos
import parser.sym;

%%

%class Scanner
%unicode
%public
%cup
%function next_token
%type java_cup.runtime.Symbol
%ignorecase
%line
%column

// Palabras reservadas ABS
RESERVADA = (ABSOLUTE|AND|ARRAY|ASM|BEGIN|CASE|CONST|CONSTRUCTOR|DESTRUCTOR|EXTERNAL|DIV|DO|DOWNTO|ELSE|END|FILE|FOR|FORWARD|FUNCTION|GOTO|IF|IMPLEMENTATION|IN|INLINE|INTERFACE|INTERRUPT|LABEL|MOD|NIL|NOT|OBJECT|OF|OR|PACKED|PRIVATE|PROCEDURE|RECORD|REPEAT|SET|SHL|SHR|STRING|THEN|TO|TYPE|UNIT|UNTIL|USES|VAR|VIRTUAL|WHILE|WITH|XOR|INT|CHAR|READ|REAL|WRITE|PROGRAM)

// Operadores
OPERADOR = \+|\-|\*|\/|DIV|MOD|NOT|AND|OR|=|<>|<|>|<=|>=|IN|,|;|\+\+|\-\-|\(|\)|\[|\]|:|\.|\^|\*\*

NumeroRealIncorrecto = \.[0-9]+|[0-9]+\.
// Comentarios
Comentario1 = \{[^}]*\}
Comentario1MalCerrado = \{[^}]*
Comentario2 = \(\*([^*]|\*[^)])*\*\)
Comentario2MalCerrado = \(\*([^*]|\*[^)])*

// Literales
Octal         = 0[0-7]+
Hexadecimal   = 0[xX][0-9a-fA-F]+
Decimal       = [1-9][0-9]*|0
Exponent      = [eE][+-]?[0-9]+
RealStrict    = [0-9]+\.[0-9]+({Exponent})?
String        = \"([^\"\n])*\" 
StringIncorrecto = \"([^\"\n\r]*[\n\r][^\"]*)\"
StringSinCerrar = \"[^\n\r\"]*
CharInvalido = \'([^\'\n][^\'\n]+)\'|\'\'
CharSinCierre = \'[^\'\n\r]*
Char          = \'([^\'\n]|\\.)\' 

// Identificadores (1-127 letras/dígitos, inicia con letra, no palabra reservada)
// Identificador inválido que comienza con número
IdentificadorNumero = [0-9][a-zA-Z0-9]*
// Identificador inválido que comienzan con caracteres no permitidos (EXCLUYENDO operadores válidos)
IdentificadorSimbolo = [^a-zA-Z0-9 \t\n\r\(\)\[\]\+\-\*\/=<>:;.,][a-zA-Z0-9]*
// Identificador inválido con caracteres no permitidos (pero SIN espacios ni paréntesis ni símbolos válidos)
IdentificadorInvalido = [a-zA-Z][a-zA-Z0-9]*[^a-zA-Z0-9 \t\n\r\(\)\[\]\+\-\*\/=<>:;.,]+[a-zA-Z0-9]+
// Identificador inválido: 128 o más caracteres
IdentificadorMuyLargo = [a-zA-Z][a-zA-Z0-9]{127}[a-zA-Z0-9]*
// Identificador válido: empieza con letra, sigue con letras/dígitos
Identificador = [a-zA-Z][a-zA-Z0-9]{0,126}


%{
// Lista para guardar errores léxicos
private ArrayList<String> errores = new ArrayList<>();

public ArrayList<String> getErrores() {
    return errores;
}

// Mapa para tokens aceptados
private HashMap<String, TokenInfo> tokensAceptados = new HashMap<>();

// Clase auxiliar para almacenar info de token
private static class TokenInfo {
    String tipo;
    HashMap<Integer, Integer> lineas = new HashMap<>();

    TokenInfo(String tipo) {
        this.tipo = tipo;
    } 

    void agregarLinea(int linea) {
        lineas.put(linea, lineas.getOrDefault(linea, 0) + 1);
    }
}

// Método para registrar tokens válidos
private void registrarToken(String token, String tipo) {
    token = token.toUpperCase(); 
    TokenInfo info = tokensAceptados.get(token);
    if(info == null) {
        info = new TokenInfo(tipo);
        tokensAceptados.put(token, info);
    }
    info.agregarLinea(yyline + 1); 
}

// Método para mostrar tabla al final
public void imprimirTokens() {
    System.out.printf("%-20s %-25s %s\n", "Token", "Tipo de Token", "Líneas");
    System.out.println("----------------------------------------------------------");
    List<String> claves = new ArrayList<>(tokensAceptados.keySet());
    Collections.sort(claves); // orden alfabético
    for(String token : claves) {
        TokenInfo info = tokensAceptados.get(token);
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer, Integer> e : info.lineas.entrySet()) {
            sb.append(e.getKey());
            if(e.getValue() > 1) sb.append("(").append(e.getValue()).append(")");
            sb.append(", ");
        }
        if(sb.length() >= 2) sb.setLength(sb.length() - 2); // quitar última coma
        System.out.printf("%-20s %-25s %s\n", token, info.tipo, sb.toString());
    }
}

// Método auxiliar para combinar registro y creación de símbolo
private Symbol crearSymbol(int symType, String token, String tipoToken) {
    registrarToken(token, tipoToken);
    return new Symbol(symType, yyline, yycolumn, token);
}
%}

%%

// Comentarios
{Comentario1MalCerrado} {
    errores.add("Error en línea " + (yyline+1) +
                ", comentario '{' sin cerrar. Texto: " + yytext());
}
{Comentario2MalCerrado} {
    errores.add("Error en línea " + (yyline+1) +
                ", comentario '(*' sin cerrar. Texto: " + yytext());
}
{Comentario1} {
    registrarToken(yytext(), "COMENTARIO");
}
{Comentario2} {
    registrarToken(yytext(), "COMENTARIO");
}

// Ignorar espacios y saltos de línea
[ \t\n\r]+           { /* Ignorar */ }

// ========== OPERADORES Y SÍMBOLOS ==========
// Símbolos aritméticos
"+"     { return crearSymbol(sym.MAS, yytext(), "OPERADOR"); }
"-"     { return crearSymbol(sym.MENOS, yytext(), "OPERADOR"); }
"*"     { return crearSymbol(sym.POR, yytext(), "OPERADOR"); }
"/"     { return crearSymbol(sym.DIVISION, yytext(), "OPERADOR"); }
"++"    { return crearSymbol(sym.INCREMENTO, yytext(), "OPERADOR"); }
"--"    { return crearSymbol(sym.DECREMENTO, yytext(), "OPERADOR"); }
"**"    { return crearSymbol(sym.POTENCIA, yytext(), "OPERADOR"); }
// Símbolos de comparación
"="     { return crearSymbol(sym.IGUAL, yytext(), "OPERADOR"); }
"<>"    { return crearSymbol(sym.DIFERENTE, yytext(), "OPERADOR"); }
"<"     { return crearSymbol(sym.MENOR, yytext(), "OPERADOR"); }
">"     { return crearSymbol(sym.MAYOR, yytext(), "OPERADOR"); }
"<="    { return crearSymbol(sym.MENOR_IGUAL, yytext(), "OPERADOR"); }
">="    { return crearSymbol(sym.MAYOR_IGUAL, yytext(), "OPERADOR"); }
// Asiginación y otros símbolos
":="    { return crearSymbol(sym.ASIGNACION, yytext(), "OPERADOR"); }
"."     { return crearSymbol(sym.PUNTO, yytext(), "OPERADOR"); }
","     { return crearSymbol(sym.COMA, yytext(), "OPERADOR"); }
":"     { return crearSymbol(sym.DOS_PUNTOS, yytext(), "OPERADOR"); }
";"     { return crearSymbol(sym.PUNTO_COMA, yytext(), "OPERADOR"); }
"("     { return crearSymbol(sym.PARENTESIS_IZQ, yytext(), "OPERADOR"); }
")"     { return crearSymbol(sym.PARENTESIS_DER, yytext(), "OPERADOR"); }
"["     { return crearSymbol(sym.CORCHETE_IZQ, yytext(), "OPERADOR"); }
"]"     { return crearSymbol(sym.CORCHETE_DER, yytext(), "OPERADOR"); }
"^"     { return crearSymbol(sym.PUNTERO, yytext(), "OPERADOR"); }


// ========== PALABRAS RESERVADAS ==========
"ABSOLUTE"      { return crearSymbol(sym.ABSOLUTE, yytext(), "PALABRA RESERVADA"); }
"AND"           { return crearSymbol(sym.AND, yytext(), "PALABRA RESERVADA"); }
"ARRAY"         { return crearSymbol(sym.ARRAY, yytext(), "PALABRA RESERVADA"); }
"ASM"           { return crearSymbol(sym.ASM, yytext(), "PALABRA RESERVADA"); }
"BEGIN"         { return crearSymbol(sym.BEGIN, yytext(), "PALABRA RESERVADA"); }
"CASE"          { return crearSymbol(sym.CASE, yytext(), "PALABRA RESERVADA"); }
"CONST"         { return crearSymbol(sym.CONST, yytext(), "PALABRA RESERVADA"); }
"CONSTRUCTOR"   { return crearSymbol(sym.CONSTRUCTOR, yytext(), "PALABRA RESERVADA"); }
"DESTRUCTOR"    { return crearSymbol(sym.DESTRUCTOR, yytext(), "PALABRA RESERVADA"); }
"DIV"           { return crearSymbol(sym.DIV, yytext(), "PALABRA RESERVADA"); }
"DO"            { return crearSymbol(sym.DO, yytext(), "PALABRA RESERVADA"); }
"DOWNTO"        { return crearSymbol(sym.DOWNTO, yytext(), "PALABRA RESERVADA"); }
"ELSE"          { return crearSymbol(sym.ELSE, yytext(), "PALABRA RESERVADA"); }
"END"           { return crearSymbol(sym.END, yytext(), "PALABRA RESERVADA"); }
"EXTERNAL"      { return crearSymbol(sym.EXTERNAL, yytext(), "PALABRA RESERVADA"); }
"FILE"          { return crearSymbol(sym.FILE, yytext(), "PALABRA RESERVADA"); }
"FOR"           { return crearSymbol(sym.FOR, yytext(), "PALABRA RESERVADA"); }
"FORWARD"       { return crearSymbol(sym.FORWARD, yytext(), "PALABRA RESERVADA"); }
"FUNCTION"      { return crearSymbol(sym.FUNCTION, yytext(), "PALABRA RESERVADA"); }
"GOTO"          { return crearSymbol(sym.GOTO, yytext(), "PALABRA RESERVADA"); }
"IF"            { return crearSymbol(sym.IF, yytext(), "PALABRA RESERVADA"); }
"IMPLEMENTATION" { return crearSymbol(sym.IMPLEMENTATION, yytext(), "PALABRA RESERVADA"); }
"IN"            { return crearSymbol(sym.IN, yytext(), "PALABRA RESERVADA"); }
"INLINE"        { return crearSymbol(sym.INLINE, yytext(), "PALABRA RESERVADA"); }
"INTERFACE"     { return crearSymbol(sym.INTERFACE, yytext(), "PALABRA RESERVADA"); }
"INTERRUPT"     { return crearSymbol(sym.INTERRUPT, yytext(), "PALABRA RESERVADA"); }
"LABEL"         { return crearSymbol(sym.LABEL, yytext(), "PALABRA RESERVADA"); }
"MOD"           { return crearSymbol(sym.MOD, yytext(), "PALABRA RESERVADA"); }
"NIL"           { return crearSymbol(sym.NIL, yytext(), "PALABRA RESERVADA"); }
"NOT"           { return crearSymbol(sym.NOT, yytext(), "PALABRA RESERVADA"); }
"OBJECT"        { return crearSymbol(sym.OBJECT, yytext(), "PALABRA RESERVADA"); }
"OF"            { return crearSymbol(sym.OF, yytext(), "PALABRA RESERVADA"); }
"OR"            { return crearSymbol(sym.OR, yytext(), "PALABRA RESERVADA"); }
"PACKED"        { return crearSymbol(sym.PACKED, yytext(), "PALABRA RESERVADA"); }
"PRIVATE"       { return crearSymbol(sym.PRIVATE, yytext(), "PALABRA RESERVADA"); }
"PROCEDURE"     { return crearSymbol(sym.PROCEDURE, yytext(), "PALABRA RESERVADA"); }
"PROGRAM"       { return crearSymbol(sym.PROGRAM, yytext(), "PALABRA RESERVADA"); }
"RECORD"        { return crearSymbol(sym.RECORD, yytext(), "PALABRA RESERVADA"); }
"REPEAT"        { return crearSymbol(sym.REPEAT, yytext(), "PALABRA RESERVADA"); }
"SET"           { return crearSymbol(sym.SET, yytext(), "PALABRA RESERVADA"); }
"SHL"           { return crearSymbol(sym.SHL, yytext(), "PALABRA RESERVADA"); }
"SHR"           { return crearSymbol(sym.SHR, yytext(), "PALABRA RESERVADA"); }
"STRING"        { return crearSymbol(sym.STRING, yytext(), "PALABRA RESERVADA"); }
"THEN"          { return crearSymbol(sym.THEN, yytext(), "PALABRA RESERVADA"); }
"TO"            { return crearSymbol(sym.TO, yytext(), "PALABRA RESERVADA"); }
"TYPE"          { return crearSymbol(sym.TYPE, yytext(), "PALABRA RESERVADA"); }
"UNIT"          { return crearSymbol(sym.UNIT, yytext(), "PALABRA RESERVADA"); }
"UNTIL"         { return crearSymbol(sym.UNTIL, yytext(), "PALABRA RESERVADA"); }
"USES"          { return crearSymbol(sym.USES, yytext(), "PALABRA RESERVADA"); }
"VAR"           { return crearSymbol(sym.VAR, yytext(), "PALABRA RESERVADA"); }
"VIRTUAL"       { return crearSymbol(sym.VIRTUAL, yytext(), "PALABRA RESERVADA"); }
"WHILE"         { return crearSymbol(sym.WHILE, yytext(), "PALABRA RESERVADA"); }
"WITH"          { return crearSymbol(sym.WITH, yytext(), "PALABRA RESERVADA"); }
"XOR"           { return crearSymbol(sym.XOR, yytext(), "PALABRA RESERVADA"); }
"INT"           { return crearSymbol(sym.INT, yytext(), "PALABRA RESERVADA"); }
"CHAR"          { return crearSymbol(sym.CHAR, yytext(), "PALABRA RESERVADA"); }
"READ"          { return crearSymbol(sym.READ, yytext(), "PALABRA RESERVADA"); }
"REAL"          { return crearSymbol(sym.REAL, yytext(), "PALABRA RESERVADA"); }
"WRITE"         { return crearSymbol(sym.WRITE, yytext(), "PALABRA RESERVADA"); }


// ========== LITERALES NUMERICOS Y CADENAS ==========
{NumeroRealIncorrecto} {
    errores.add("Error en línea " + (yyline+1) +
                ", columna " + (yycolumn+1) +
                ": número real incorrecto. Texto: " + yytext());
}

{Octal}         { return crearSymbol(sym.LIT_OCTAL, yytext(), "LITERAL OCTAL"); }
{Hexadecimal}   { return crearSymbol(sym.LIT_HEX, yytext(), "LITERAL HEXADECIMAL"); }
{Decimal}       { return crearSymbol(sym.LIT_ENTERO, yytext(), "LITERAL ENTERO"); }
{RealStrict}    { return crearSymbol(sym.LIT_REAL, yytext(), "LITERAL REAL"); }


{StringSinCerrar} {
    errores.add("Error en línea " + (yyline+1) +
                ", columna " + (yycolumn+1) +
                ": string sin cerrar. Texto: " + yytext());
}
{StringIncorrecto} {
    errores.add("Error en línea " + (yyline+1) +
                ", columna " + (yycolumn+1) +
                ": string incorrecto. Texto: " + yytext());
}
{String}        { return crearSymbol(sym.LIT_STRING, yytext(), "LITERAL STRING"); }
{CharInvalido} {
    errores.add("Error en línea " + (yyline+1) +
                ", columna " + (yycolumn+1) +
                ": carácter inválido. Texto: " + yytext());
}
{CharSinCierre} {
    errores.add("Error en línea " + (yyline+1) +
                ", columna " + (yycolumn+1) +
                ": carácter sin cerrar. Texto: " + yytext());
}
{Char}          { return crearSymbol(sym.LIT_CHAR, yytext(), "LITERAL CARACTER"); }


// ========== IDENTIFICADORES ==========
{IdentificadorNumero} {
    errores.add("Error en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": identificador inválido, no puede iniciar con un número. Texto: " + yytext());
}
{IdentificadorSimbolo} {
    errores.add("Error en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": identificador inválido, no puede iniciar con símbolo. Texto: " + yytext());
}
{IdentificadorInvalido} {
    errores.add("Error en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": identificador inválido, solo se permiten letras y dígitos. Texto: " + yytext());
}
{IdentificadorMuyLargo} {
    errores.add("Error en línea " + (yyline+1) + ", columna " + (yycolumn+1) + ": identificador inválido, excede el máximo de 127 caracteres. Texto: " + yytext());
}
{Identificador} { return crearSymbol(sym.IDENTIFICADOR, yytext(), "IDENTIFICADOR"); }
// ========== CUALQUIER OTRO ==========
. {
   errores.add("Error en línea " + (yyline+1) +
               ", columna " + (yycolumn+1) +
               ": " + yytext());
}


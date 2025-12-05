; Código generado por el Compilador ABS
; Proyecto 3 - Compiladores e Intérpretes
; Archivo: output.asm

extern printf
extern exit

section .data
    newline DB 10, 0
    fmt_int DB '%d', 10, 0
    fmt_str DB '%s', 10, 0
    str2 DB "Resultado: ", 0

section .bss
    x RESD 1
    y RESD 1
    result RESD 1
    t0 RESD 1
    t1 RESD 1
    t2 RESD 1
    t3 RESD 1

section .text
global _start

_start:
    ; === MAIN ===
    ; Asignación: x = 5
    MOV DWORD [x], 5
    ; Asignación: y = 10
    MOV DWORD [y], 10
    ; Asignación: t0 = 2
    MOV DWORD [t0], 2
    ; t1 = y * t0
    MOV EAX, [y]
    IMUL EAX, [t0]
    MOV [t1], EAX
    ; t2 = x + t1
    MOV EAX, [x]
    ADD EAX, [t1]
    MOV [t2], EAX
    ; Asignación: result = t2
    MOV EAX, [t2]
    MOV [result], EAX
    ; t3 = (x == y)
    MOV EAX, [x]
    CMP EAX, [y]
    SETE AL
    MOVZX EAX, AL
    MOV [t3], EAX
    ; === IF ===
    ; if (!t3) goto L0
    CMP DWORD [t3], 0
    JE L0
    ; WRITE(x)
    PUSH DWORD [x]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
    JMP L1
L0:
    ; === ELSE ===
    ; WRITE(y)
    PUSH DWORD [y]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
L1:
    ; === END IF-ELSE ===
    ; WRITE(string)
    PUSH str2
    PUSH fmt_str
    CALL printf
    ADD ESP, 8
    ; WRITE(result)
    PUSH DWORD [result]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
    ; x++
    INC DWORD [x]
    ; y--
    DEC DWORD [y]

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

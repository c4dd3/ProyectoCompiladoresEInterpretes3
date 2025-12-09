; Código generado por el Compilador ABS
; Proyecto 3 - Compiladores e Intérpretes
; Archivo: parser\testFile.asm

extern printf
extern exit

section .data
    newline DB 10, 0
    fmt_int DB '%d', 10, 0
    fmt_str DB '%s', 10, 0

section .bss
    x RESD 1
    y RESD 1
    t0 RESD 1
    t1 RESD 1
    t2 RESD 1
    t3 RESD 1

section .text
global _start

_start:
    ; Asignación: x = 10
    MOV DWORD [x], 10
    ; Asignación: y = 20
    MOV DWORD [y], 20
    ; Asignación: x = 9
    MOV DWORD [x], 9
    ; Asignación: t0 = 15
    MOV DWORD [t0], 15
    ; t1 = (y == t0)
    MOV EAX, [y]
    CMP EAX, [t0]
    SETE AL
    MOVZX EAX, AL
    MOV [t1], EAX
    ; t2 = x * y
    MOV EAX, [x]
    IMUL EAX, [y]
    MOV [t2], EAX
    ; Asignación: x = t2
    MOV EAX, [t2]
    MOV [x], EAX
    ; t3 = x - y
    MOV EAX, [x]
    SUB EAX, [y]
    MOV [t3], EAX
    ; Asignación: x = t3
    MOV EAX, [t3]
    MOV [x], EAX
    ; x++
    INC DWORD [x]
    ; y--
    DEC DWORD [y]
    ; WRITE(x)
    PUSH DWORD [x]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
    ; WRITE(y)
    PUSH DWORD [y]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
    ; === Variables Globales ===

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

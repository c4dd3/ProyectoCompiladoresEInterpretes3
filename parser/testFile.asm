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
    numero RESD 1
    limite RESD 1
    contador RESD 1
    nombre RESB 256
    apellido RESB 256
    a RESD 1
    i RESD 1
    t0 RESD 1
    t1 RESD 1
    t2 RESD 1

section .text
global _start

_start:
    ; Asignación: funcion4 = 0
    MOV DWORD [funcion4], 0
    ; t0 = a + 6
    MOV EAX, [a]
    ADD EAX, [6]
    MOV [t0], EAX
    ; Asignación: a = t0
    MOV EAX, [t0]
    MOV [a], EAX
    ; Asignación: funcion5 = 0
    MOV DWORD [funcion5], 0
    t1 = a > 8
    ; Asignación: i = 0
    MOV DWORD [i], 0
    ; t2 = x + 1
    MOV EAX, [x]
    ADD EAX, [1]
    MOV [t2], EAX
    ; Asignación: x = t2
    MOV EAX, [t2]
    MOV [x], EAX
    ; WRITE(string)
    PUSH "Fin"
    PUSH fmt_str
    CALL printf
    ADD ESP, 8
    ; === Variables Globales ===

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

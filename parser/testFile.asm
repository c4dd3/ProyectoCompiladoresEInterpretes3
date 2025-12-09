; Código generado por el Compilador ABS
; Proyecto 3 - Compiladores e Intérpretes
; Archivo: parser\testFile.asm

extern printf
extern exit

section .data
    newline DB 10, 0
    fmt_int DB '%d', 10, 0
    fmt_str DB '%s', 10, 0
    str0 DB "Fin", 0

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
    t3 RESD 1
    t4 RESD 1
    t5 RESD 1

section .text
global _start

_start:
    ; Asignación: funcion4 = 0
    MOV DWORD [funcion4], 0
    ; Asignación: t0 = 6
    MOV DWORD [t0], 6
    ; t1 = a + t0
    MOV EAX, [a]
    ADD EAX, [t0]
    MOV [t1], EAX
    ; Asignación: a = t1
    MOV EAX, [t1]
    MOV [a], EAX
    ; Asignación: funcion5 = 0
    MOV DWORD [funcion5], 0
    ; Asignación: t2 = 8
    MOV DWORD [t2], 8
    t3 = a > t2
    ; Asignación: i = 0
    MOV DWORD [i], 0
    ; Asignación: t4 = 1
    MOV DWORD [t4], 1
    ; t5 = x + t4
    MOV EAX, [x]
    ADD EAX, [t4]
    MOV [t5], EAX
    ; Asignación: x = t5
    MOV EAX, [t5]
    MOV [x], EAX
    ; WRITE(string)
    PUSH str0
    PUSH fmt_str
    CALL printf
    ADD ESP, 8
    ; === Variables Globales ===

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

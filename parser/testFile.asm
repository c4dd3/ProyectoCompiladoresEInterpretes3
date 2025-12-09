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
    a RESD 1
    i RESD 1
    nombre RESB 256
    apellido RESB 256
    t0 RESD 1
    t1 RESD 1
    t2 RESD 1
    t3 RESD 1
    t4 RESD 1

section .text
global _start

_start:
    ; Asignación: numero = 1
    MOV DWORD [numero], 1
    ; Asignación: limite = 2
    MOV DWORD [limite], 2
    ; t0 = numero + limite
    MOV EAX, [numero]
    ADD EAX, [limite]
    MOV [t0], EAX
    ; Asignación: contador = t0
    MOV EAX, [t0]
    MOV [contador], EAX
    ; Asignación: a = 5
    MOV DWORD [a], 5
    ; Asignación: i = 0
    MOV DWORD [i], 0
    ; Asignación: t1 = 1
    MOV DWORD [t1], 1
    ; t2 = a + t1
    MOV EAX, [a]
    ADD EAX, [t1]
    MOV [t2], EAX
    ; Asignación: a = t2
    MOV EAX, [t2]
    MOV [a], EAX
    ; Asignación: t3 = 1
    MOV DWORD [t3], 1
    ; t4 = a - t3
    MOV EAX, [a]
    SUB EAX, [t3]
    MOV [t4], EAX
    ; Asignación: a = t4
    MOV EAX, [t4]
    MOV [a], EAX
    ; WRITE(string)
    PUSH str0
    PUSH fmt_str
    CALL printf
    ADD ESP, 8
    ; WRITE(a)
    PUSH DWORD [a]
    PUSH fmt_int
    CALL printf
    ADD ESP, 8
    ; === Variables Globales ===

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

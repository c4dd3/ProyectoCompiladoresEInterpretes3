; Código generado por el Compilador ABS
; Proyecto 3 - Compiladores e Intérpretes
; Archivo: parser\testFiles.asm

extern printf
extern exit

section .data
    newline DB 10, 0
    fmt_int DB '%d', 10, 0
    fmt_str DB '%s', 10, 0

section .bss
    ; (vacía)

section .text
global _start

_start:
    ; No hay variables globales

    ; Salir del programa
    MOV EAX, 1
    XOR EBX, EBX
    INT 0x80

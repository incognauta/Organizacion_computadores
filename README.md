# Organización de Computadores - Proyecto 2

## Universidad EAFIT - Ingeniería de Sistemas
**Período:** 2026-1  
**Docente:** José Luis Montoya Pareja  
**Fecha Publicación:** 25 de marzo de 2026  
**Fecha Entrega:** 29 de abril de 2026

---

## 📋 Proyecto 2: ALU y Nuevos Comandos a Implementar en la ALU

### Objetivo
Practicar los conceptos vistos en clase durante todo el semestre desde el diseño de una tabla de verdad, ALUs, construcción de la CPU y programación en assembler hasta su implementación en el HDL Nand2Tetris con circuitos adicionales a los que la plataforma Nand2Tetris trae.

---

## 👥 Integrantes del Proyecto 2

| Nombre | Rol |
|--------|-----|
| [Nombre estudiante 1] | Desarrollador |
| [Nombre estudiante 2] | Desarrollador |

---

## 📂 Estructura del Proyecto

```
Proyecto #2/
├── README.md                 # Este archivo
├── CONTRIBUTORS.md           # Roles y contribuciones del equipo
├── CHANGELOG.md              # Historial de cambios
├── LICENSE                   # Licencia del proyecto
├── Shifter.hdl              # Circuito del Shifter (Punto 1)
├── Shifter.md5              # Checksum del Shifter
├── ALU.hdl                  # ALU con Shifter integrado (Punto 2)
├── ALU.md5                  # Checksum del ALU
├── Memory.hdl               # Memoria RAM (Punto 3)
├── Memory.md5               # Checksum de Memory
├── CPU.hdl                  # CPU con ALU (Punto 3)
├── CPU.md5                  # Checksum de CPU
├── Computer.hdl             # Computadora completa (Punto 3)
├── Computer.md5             # Checksum de Computer
├── design.txt               # Especificación de instrucciones Shift (Punto 4)
└── design.md5               # Checksum de design.txt
```

---

## 🎯 Puntos a Desarrollar

### Punto 1: Circuito Shifter (25%)
Implementación de un circuito que desplaza bits a izquierda o derecha en un valor de 16 bits.

**Entrada:**
- `in[16]`: Dato a desplazar
- `direction`: 0 = izquierda, 1 = derecha

**Salida:**
- `out[16]`: Dato desplazado
- `result`: Bit que sale del extremo

### Punto 2: ALU con Shifter (25%)
Integración del circuito Shifter en la ALU existente.

**Comando de Shifter:**
- `zx=0, nx=0, zy=0, ny=0, no=1`
- `f`: determina dirección (0 = izquierda, 1 = derecha)

### Punto 3: Implementación de la CPU (40%)
Implementación completa de la arquitectura del computador Nand2Tetris:
- **Memory.hdl**: Memoria RAM
- **CPU.hdl**: Unidad central de procesamiento
- **Computer.hdl**: Computadora completa

### Punto 4: Arquitectura de la Instrucción (10%)
Documentación del formato de las instrucciones Shift Left (`<<`) y Shift Right (`>>`).

---

## 🛠️ Herramientas Utilizadas

- **Nand2Tetris Web IDE**: https://nand2tetris.github.io/web-ide/
- **Lenguaje**: HDL (Hardware Description Language)
- **Validación**: Pruebas unitarias en plataforma Nand2Tetris

---

## ✅ Rúbrica de Evaluación

| Criterio | Peso |
|----------|------|
| Aplicación de conceptos | 20% |
| Conocimiento | 20% |
| Cumplimiento de requisitos | 20% |
| Funcionamiento | 20% |
| Optimización | 20% |

---

## 📝 Consideraciones Especiales

1. Proyecto en equipos de **dos personas**
2. Se espera **calidad profesional**
3. **Documentación exhaustiva** requerida
4. **Pruebas completas** de todos los componentes
5. **Código bien estructurado** y modular
6. **Presentación organizada** con commits claros
7. Evaluación de contribución individual (auto-evaluación, evaluación por pares)
8. Reportar conflictos al profesor antes del **17 de abril**

---

## 🚀 Estado del Proyecto

| Punto | Tarea | Estado |
|-------|-------|--------|
| 1 | Shifter.hdl | ✅ Completado |
| 2 | ALU.hdl | ✅ Completado |
| 3 | Memory.hdl, CPU.hdl, Computer.hdl | ✅ Completado |
| 4 | design.txt | ⏳ Pendiente |

---

## 📖 Referencias

- [Nand2Tetris Project 5](https://nand2tetris.github.io/web-ide/chip)
- [Libro Nand2Tetris - Capítulo 5](https://b1391bd6-da3d-477d-8c01-38cdf774495a.filesusr.com/ugd/44046b_b2cad2eea33847869b86c541683551a7.pdf)
- Clases 12, 13 y 14 del curso

---

**Última actualización:** 27 de abril de 2026

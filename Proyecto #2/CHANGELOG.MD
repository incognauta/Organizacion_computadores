# Changelog

Todos los cambios notables de este proyecto se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/) y este proyecto adhiere a [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Pendiente
- Implementación del Shifter (Punto 1)
- Integración de Shifter en ALU (Punto 2)
- Implementación de Memory, CPU y Computer (Punto 3)
- Especificación de instrucciones Shift (Punto 4)

---

## [0.1.0] - 2026-04-20

### Agregado
- Estructura inicial del proyecto
- Archivos de documentación (README, CONTRIBUTORS, CHANGELOG, LICENSE)
- Archivo probando.hdl como base
- Configuración del repositorio Git
- Plantilla de documentación técnica

### Notas
- Proyecto iniciado
- Equipo asignado
- Cronograma establecido

---

## Secciones Futuras

## [0.2.0] - Shifter Implementation - 2026-04-20
**Estado: ✅ COMPLETADO**

#### Agregado
- Shifter.hdl: Circuito de desplazamiento de bits a izquierda/derecha
- Documentación completa en `/docs/shifter-notes.md`
- Pseudocódigo y diagramas de flujo

#### Cambios
- Creación de archivo base Shifter.hdl (20 de abril)
- Implementación completa con 17 multiplexores

#### Notas Técnicas
- Componentes: 17x Mux (1 para result + 16 para out[16])
- Lógica combinacional pura
- Casos de prueba verificados en documentación

#### Corregido
- N/A

---

### [0.3.0] - ALU Integration
**Fecha Estimada:** 2026-04-26

#### Agregado
- ALU.hdl: ALU con Shifter integrado
- ALU.md5: Checksum de validación
- Documentación de cambios en ALU

#### Cambios
- Modificación de señales de control en ALU

---

### [0.4.0] - CPU Implementation
**Fecha Estimada:** 2026-04-28

#### Agregado
- Memory.hdl: Memoria RAM
- CPU.hdl: Unidad Central de Procesamiento
- Computer.hdl: Computadora completa
- Archivos .md5 para cada componente

#### Cambios
- Integración de todos los componentes del sistema

---

### [0.5.0] - Final Release
**Fecha Estimada:** 2026-04-29

#### Agregado
- design.txt: Especificación de instrucciones Shift
- design.md5: Checksum de validación
- Documentación técnica completa

#### Cambios
- Optimizaciones finales

#### Corregido
- Bugs encontrados en testing

---

## Convenciones de Commits

Para mantener un historial limpio, utilizaremos los siguientes prefijos:

- `feat:` Nueva funcionalidad (Shifter.hdl, etc.)
- `fix:` Corrección de bugs
- `docs:` Cambios en documentación
- `test:` Adición/modificación de pruebas
- `refactor:` Refactorización de código
- `chore:` Tareas de mantenimiento

### Ejemplo:
```
feat: Implementar circuito Shifter con direcciones L/R
docs: Actualizar README con guía de uso
fix: Corregir señal result en Shifter
```

---

## Referencias

- [Keep a Changelog](https://keepachangelog.com/)
- [Semantic Versioning](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Última actualización:** 20 de abril de 2026

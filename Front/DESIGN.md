---
version: 1.0
name: TipQR-design-system
description: Sistema de diseño para TipQR, plataforma de propinas digitales orientada al rubro gastronómico. Interfaz clara y de uso rápido para mozos y dueños de restaurante. Primario teal (confianza, dinero), acento ámbar (propinas, destacados), superficies blancas limpias. Tipografía Inter en todos los niveles — óptima para cifras y uso mobile.

colors:
  primary: "#0f766e"
  primary-hover: "#14b8a6"
  primary-active: "#0d5e57"
  primary-disabled: "#99d6d1"
  accent: "#f59e0b"
  accent-soft: "#fef3c7"
  ink: "#111827"
  body: "#374151"
  muted: "#6b7280"
  muted-soft: "#9ca3af"
  hairline: "#e5e7eb"
  canvas: "#f9fafb"
  surface-card: "#ffffff"
  surface-soft: "#f3f4f6"
  on-primary: "#ffffff"
  on-accent: "#111827"
  success: "#22c55e"
  success-soft: "#dcfce7"
  error: "#ef4444"
  error-soft: "#fee2e2"
  warning: "#f59e0b"
  warning-soft: "#fef3c7"

typography:
  display-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.5px
  display-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: -0.3px
  display-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 20px
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: -0.2px
  title-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 18px
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: 0
  title-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: 0
  title-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: 0
  body-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: 0
  body-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: 0
  body-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 13px
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: 0
  amount:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 36px
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: -1px
  amount-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.5px
  label:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 13px
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: 0
  caption:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: 0.2px
  button:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1
    letterSpacing: 0
  button-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 13px
    fontWeight: 600
    lineHeight: 1
    letterSpacing: 0
  nav-link:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: 0

rounded:
  xs: 4px
  sm: 6px
  md: 8px
  lg: 12px
  xl: 16px
  2xl: 20px
  pill: 9999px
  full: 9999px

spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
  section: 64px

shadows:
  card: "0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.04)"
  card-hover: "0 4px 12px rgba(0,0,0,0.10), 0 2px 4px rgba(0,0,0,0.06)"
  modal: "0 20px 60px rgba(0,0,0,0.15)"

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button}"
    rounded: "{rounded.lg}"
    padding: 10px 20px
    height: 42px
    hover: "{colors.primary-hover}"
    active: "{colors.primary-active}"
  button-primary-disabled:
    backgroundColor: "{colors.primary-disabled}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.lg}"
    cursor: not-allowed
  button-secondary:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink}"
    border: "1px solid {colors.hairline}"
    typography: "{typography.button}"
    rounded: "{rounded.lg}"
    padding: 10px 20px
    height: 42px
  button-ghost:
    backgroundColor: transparent
    textColor: "{colors.primary}"
    typography: "{typography.button}"
    rounded: "{rounded.lg}"
    padding: 10px 20px
    height: 42px
  button-danger:
    backgroundColor: "{colors.error}"
    textColor: "#ffffff"
    typography: "{typography.button}"
    rounded: "{rounded.lg}"
    padding: 10px 20px
    height: 42px
  button-sm:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.button-sm}"
    rounded: "{rounded.md}"
    padding: 6px 14px
    height: 32px
  button-accent:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.on-accent}"
    typography: "{typography.button}"
    rounded: "{rounded.lg}"
    padding: 10px 20px
    height: 42px
  text-input:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink}"
    placeholderColor: "{colors.muted-soft}"
    border: "1px solid {colors.hairline}"
    typography: "{typography.body-lg}"
    rounded: "{rounded.lg}"
    padding: 10px 14px
    height: 42px
  text-input-focused:
    border: "2px solid {colors.primary}"
    ring: "3px solid rgba(15,118,110,0.12)"
  text-input-error:
    border: "1px solid {colors.error}"
    ring: "3px solid rgba(239,68,68,0.12)"
  label-input:
    textColor: "{colors.body}"
    typography: "{typography.label}"
    marginBottom: 6px
  error-message:
    textColor: "{colors.error}"
    typography: "{typography.body-sm}"
    marginTop: 4px
  stat-card:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink}"
    rounded: "{rounded.xl}"
    padding: 24px
    shadow: "{shadows.card}"
  stat-card-accent:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.xl}"
    padding: 24px
  amount-display:
    textColor: "{colors.ink}"
    typography: "{typography.amount}"
  tip-badge:
    backgroundColor: "{colors.accent-soft}"
    textColor: "{colors.warning}"
    typography: "{typography.caption}"
    rounded: "{rounded.pill}"
    padding: 3px 10px
  status-badge-success:
    backgroundColor: "{colors.success-soft}"
    textColor: "{colors.success}"
    typography: "{typography.caption}"
    rounded: "{rounded.pill}"
    padding: 3px 10px
  status-badge-error:
    backgroundColor: "{colors.error-soft}"
    textColor: "{colors.error}"
    typography: "{typography.caption}"
    rounded: "{rounded.pill}"
    padding: 3px 10px
  status-badge-warning:
    backgroundColor: "{colors.warning-soft}"
    textColor: "{colors.warning}"
    typography: "{typography.caption}"
    rounded: "{rounded.pill}"
    padding: 3px 10px
  data-table:
    headerBackground: "{colors.surface-soft}"
    headerTextColor: "{colors.muted}"
    headerTypography: "{typography.label}"
    rowBackground: "{colors.surface-card}"
    rowTextColor: "{colors.body}"
    rowTypography: "{typography.body-md}"
    borderColor: "{colors.hairline}"
    rounded: "{rounded.xl}"
  sidebar:
    backgroundColor: "{colors.primary-active}"
    textColor: "#ffffff"
    width: 256px
    navLinkTypography: "{typography.nav-link}"
    navLinkActiveBackground: "rgba(255,255,255,0.12)"
    navLinkHoverBackground: "rgba(255,255,255,0.08)"
  top-nav:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink}"
    height: 64px
    borderBottom: "1px solid {colors.hairline}"
  modal:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.ink}"
    rounded: "{rounded.2xl}"
    padding: 32px
    shadow: "{shadows.modal}"
    overlay: "rgba(0,0,0,0.4)"
  alert-success:
    backgroundColor: "{colors.success-soft}"
    textColor: "#15803d"
    border: "1px solid #bbf7d0"
    rounded: "{rounded.lg}"
    padding: 12px 16px
  alert-error:
    backgroundColor: "{colors.error-soft}"
    textColor: "#b91c1c"
    border: "1px solid #fecaca"
    rounded: "{rounded.lg}"
    padding: 12px 16px
  qr-card:
    backgroundColor: "{colors.surface-card}"
    rounded: "{rounded.2xl}"
    padding: 32px
    shadow: "{shadows.card-hover}"
    borderColor: "{colors.hairline}"
---

## Overview

TipQR es una plataforma de propinas digitales para el rubro gastronómico. La interfaz está pensada para dos tipos de usuario:
- **Empleados (mozos/bartenders)**: acceso rápido, claro, sin fricción. Ven sus propinas en tiempo real.
- **Dueños/encargados**: panel de administración con métricas, ABM de empleados, sucursales y mesas.

El sistema ancla en un **fondo canvas gris muy suave** (`{colors.canvas}` — #f9fafb) con tarjetas blancas puras (`{colors.surface-card}` — #ffffff). El color primario es un **teal profundo** (`{colors.primary}` — #0f766e) que evoca confianza y está asociado al dinero sin ser el verde chillón típico. El ámbar (`{colors.accent}` — #f59e0b) es el acento para propinas recibidas, montos destacados y badges.

**Tipografía única: Inter** — humanista, legible en cualquier tamaño, excelente para cifras monetarias. Token especial `{typography.amount}` para mostrar montos de propinas en 36px bold.

## Colors

### Primarios
- **Primary** (`{colors.primary}` — #0f766e): Teal oscuro. Todos los botones CTA primarios, sidebar, focus rings. Color más representativo de la marca.
- **Primary Hover** (`{colors.primary-hover}` — #14b8a6): Estado hover/light del primario.
- **Primary Active** (`{colors.primary-active}` — #0d5e57): Press state. También es el color del sidebar.
- **Primary Disabled** (`{colors.primary-disabled}` — #99d6d1): Estado deshabilitado del botón primario.

### Acento
- **Accent** (`{colors.accent}` — #f59e0b): Ámbar. Propinas recibidas, montos destacados, badges de "nueva propina".
- **Accent Soft** (`{colors.accent-soft}` — #fef3c7): Fondo suave del badge ámbar.

### Superficies
- **Canvas** (`{colors.canvas}` — #f9fafb): Fondo de página. Gris casi blanco — más tranquilo que el blanco puro.
- **Surface Card** (`{colors.surface-card}` — #ffffff): Fondo de tarjetas, modales, inputs.
- **Surface Soft** (`{colors.surface-soft}` — #f3f4f6): Encabezados de tabla, fondos de sección secundaria.
- **Hairline** (`{colors.hairline}` — #e5e7eb): Bordes y divisores.

### Texto
- **Ink** (`{colors.ink}` — #111827): Títulos, valores numéricos, texto de máximo peso visual.
- **Body** (`{colors.body}` — #374151): Texto de párrafo y contenido general.
- **Muted** (`{colors.muted}` — #6b7280): Labels secundarios, texto de ayuda.
- **Muted Soft** (`{colors.muted-soft}` — #9ca3af): Placeholders, texto terciario.

### Semánticos
- **Success** (`{colors.success}` — #22c55e) / **Success Soft** (#dcfce7): Propinas cobradas, pagos confirmados.
- **Error** (`{colors.error}` — #ef4444) / **Error Soft** (#fee2e2): Errores de validación, acciones destructivas.
- **Warning** (`{colors.warning}` — #f59e0b) / **Warning Soft** (#fef3c7): Alertas, estados pendientes.

## Typography

Una única familia: **Inter**. Nunca usar serif. La app maneja números y cantidades — Inter tiene la mejor legibilidad numérica del mundo sans-serif.

| Token | Tamaño | Peso | Uso |
|---|---|---|---|
| `{typography.display-lg}` | 32px / 700 | Bold | Título principal de página (h1) |
| `{typography.display-md}` | 24px / 700 | Bold | Secciones destacadas, totales grandes |
| `{typography.display-sm}` | 20px / 600 | Semibold | Subtítulos de sección |
| `{typography.title-lg}` | 18px / 600 | Semibold | Título de card, nombre de empleado destacado |
| `{typography.title-md}` | 16px / 600 | Semibold | Encabezados de formulario, columnas de tabla |
| `{typography.title-sm}` | 14px / 600 | Semibold | Labels de datos en card |
| `{typography.body-lg}` | 16px / 400 | Regular | Texto de inputs, contenido principal |
| `{typography.body-md}` | 14px / 400 | Regular | Cuerpo general, celdas de tabla |
| `{typography.body-sm}` | 13px / 400 | Regular | Texto de ayuda, mensajes de error |
| `{typography.amount}` | 36px / 700 | Bold | Monto total de propina (token especial) |
| `{typography.amount-sm}` | 24px / 700 | Bold | Monto secundario o en card pequeña |
| `{typography.label}` | 13px / 500 | Medium | Label de input, encabezado de columna |
| `{typography.caption}` | 12px / 500 | Medium | Badges, chips de estado |
| `{typography.button}` | 14px / 600 | Semibold | Todos los botones tamaño normal |
| `{typography.button-sm}` | 13px / 600 | Semibold | Botones pequeños inline |
| `{typography.nav-link}` | 14px / 500 | Medium | Links del sidebar y top nav |

## Buttons

### Primary — Acción principal
```
bg: #0f766e  |  text: #ffffff  |  rounded: 12px  |  height: 42px  |  padding: 10px 20px
hover → bg: #14b8a6
active → bg: #0d5e57
disabled → bg: #99d6d1, cursor: not-allowed
```

### Secondary — Acción alternativa
```
bg: #ffffff  |  text: #111827  |  border: 1px solid #e5e7eb  |  rounded: 12px  |  height: 42px
hover → border: #9ca3af
```

### Ghost — Acción terciaria / link
```
bg: transparent  |  text: #0f766e  |  rounded: 12px  |  height: 42px
hover → bg: rgba(15,118,110,0.06)
```

### Danger — Acciones destructivas (eliminar)
```
bg: #ef4444  |  text: #ffffff  |  rounded: 12px  |  height: 42px
hover → bg: #dc2626
```

### Accent — Destacar propina / acción especial
```
bg: #f59e0b  |  text: #111827  |  rounded: 12px  |  height: 42px
hover → bg: #d97706
```

### Small — Botones inline en tablas o cards
```
height: 32px  |  padding: 6px 14px  |  rounded: 8px  |  font: 13px/600
```

## Inputs & Forms

```
height: 42px  |  padding: 10px 14px  |  rounded: 12px
border: 1px solid #e5e7eb  |  bg: #ffffff  |  text: #111827
placeholder: #9ca3af
focus → border: 2px solid #0f766e, ring: 3px solid rgba(15,118,110,0.12)
error  → border: 1px solid #ef4444, ring: 3px solid rgba(239,68,68,0.12)
```

Labels: 13px / 500 / color #374151, margin-bottom 6px.
Mensajes de error: 12px / 400 / color #ef4444, margin-top 4px.

## Cards

### Stat Card (métricas del dashboard)
```
bg: #ffffff  |  rounded: 16px  |  padding: 24px
shadow: 0 1px 3px rgba(0,0,0,0.08)
```
Variante primaria: `bg: #0f766e`, texto en blanco — para la métrica más importante.

### QR Card (código QR del empleado/mesa)
```
bg: #ffffff  |  rounded: 20px  |  padding: 32px
shadow: 0 4px 12px rgba(0,0,0,0.10)  |  border: 1px solid #e5e7eb
```

### Data Table
```
header: bg #f3f4f6, text #6b7280, font 13px/500
rows: bg #ffffff, text #374151, font 14px/400
border entre filas: 1px solid #e5e7eb
rounded del contenedor: 12px
```

## Badges & Status

### Tip Badge (propina ámbar)
```
bg: #fef3c7  |  text: #f59e0b  |  rounded: pill  |  padding: 3px 10px  |  font: 12px/500
```

### Status badges
```
success → bg: #dcfce7, text: #16a34a
error   → bg: #fee2e2, text: #dc2626
warning → bg: #fef3c7, text: #d97706
```

## Layout

### Sidebar
```
bg: #0d5e57  |  width: 256px  |  text: #ffffff
nav-link activo: bg rgba(255,255,255,0.12)
nav-link hover:  bg rgba(255,255,255,0.08)
```

### Spacing
- Entre secciones de página: 64px
- Padding interno de cards: 24px (normal) / 32px (grande)
- Gap entre elementos de formulario: 16px
- Gap entre cards de dashboard: 24px

### Breakpoints
| Nombre | Ancho | Cambios clave |
|---|---|---|
| Mobile | < 768px | Sidebar collapsa, layout 1 columna, amount token reduce a 28px |
| Tablet | 768–1024px | Sidebar fija, grids 2 columnas |
| Desktop | > 1024px | Full sidebar, grids 3-4 columnas |

## Do's and Don'ts

### Do
- Usar `{colors.primary}` (#0f766e) para todos los botones CTA principales y el sidebar.
- Usar `{typography.amount}` para montos de propina — el token de 36px bold es la pieza más visible de cada pantalla.
- Usar `{colors.accent}` (#f59e0b) para resaltar propinas recibidas, no como color primario.
- Cards blancas sobre canvas gris suave — el contraste suave descansa la vista en uso prolongado.
- Siempre mostrar feedback visual en los inputs (focus ring teal, error ring rojo).

### Don't
- No mezclar colores primarios — no usar indigo, violeta ni azul. El teal es el único color de marca.
- No usar Inter bold para cuerpo de texto — reservar bold/semibold para jerarquía clara.
- No omitir el estado disabled en botones — los mozos usan la app rápido y el feedback es clave.
- No usar sombras fuertes — el sistema es flat con sombras sutiles, no profundidad exagerada.
- No usar el acento ámbar como color de fondo de sección — solo para badges y highlights puntuales.

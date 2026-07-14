# TipQR - Plataforma de propinas digitales

Plataforma web orientada a comercios de atención al público (bares, restaurantes, cafeterías, hoteles) que permite gestionar propinas digitales mediante códigos QR, integrando una pasarela de pago externa y brindando herramientas administrativas para el seguimiento de las operaciones.

> Trabajo Final Integrador — Tecnicatura Universitaria en Programación — UTN FRC  
> Alumno: Almada Joaquín | Legajo: 412180

---

## Problema

Muchos clientes ya no utilizan efectivo, lo que hace que los empleados de comercios de atención al público pierdan oportunidades de recibir propinas. Además, los comercios no cuentan con una herramienta ordenada para administrar propinas individuales, propinas grupales, mesas atendidas, sucursales, turnos y reportes generales.

## Solución

TipQR permite que el cliente escanee un QR y deje una propina digital de forma rápida, sin necesidad de tener efectivo ni registrarse en la plataforma. El sistema gestiona el ciclo completo: creación de la orden, procesamiento del pago a través de Mercado Pago, conciliación automática y distribución de propinas grupales.

El sistema **no** funciona como billetera virtual ni custodia dinero. El procesamiento del pago es delegado a Mercado Pago en ambiente de prueba.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 3.4.3 |
| Frontend | Angular 21 + Tailwind CSS 4 (SSR con @angular/ssr) |
| Base de datos | PostgreSQL 16 |
| Autenticación | Spring Security 6 + JWT (jjwt, HS256, stateless) |
| ORM | Spring Data JPA + Hibernate |
| Email | Spring Mail (SMTP) — verificación de cuenta y notificaciones |
| Captcha | Google reCAPTCHA v2 (registro) |
| Configuración | Variables de entorno vía `.env` (spring-dotenv, gitignored) |
| Documentación API | Swagger / OpenAPI (springdoc) |
| Pagos | Mercado Pago Checkout Pro (ambiente de prueba) + Webhooks (integración vía REST con `RestClient`) |
| Testing | JUnit 5 + Mockito (163 tests unitarios), H2 en memoria |
| Diseño | Design system propio (tipografía Fraunces + Hanken Grotesk, paleta bordó/rosa/navy) |
| Utilidades | Lombok, ZXing (generación de QR) |

---

## Estado del proyecto

Funcionalidades efectivamente implementadas hasta la fecha:

### ✅ Autenticación y estructura base (Sprint 1)
- Login con JWT stateless (HS256) y BCrypt para el hash de contraseñas.
- Control de acceso por rol (`SUPERADMIN`, `DUENO`, `ENCARGADO`, `EMPLEADO`) con `@PreAuthorize` en el backend y *guards* + interceptor en el frontend.
- Manejo centralizado de errores con cuerpo uniforme (`ErrorResponse`): 400, 401, 403, 404, 409.
- Frontend base: layout con sidebar, dashboards diferenciados por rol.
- Las 16 entidades JPA del dominio modeladas y mapeadas.

### ✅ Registro self-service con validación (onboarding)
- **Stepper de 3 pasos**: datos del usuario (con reCAPTCHA y verificación de email real por SMTP), datos del comercio, y carga de documentos (DNI frente/dorso y selfie como imagen, constancia AFIP como PDF).
- Estados de cuenta: `CREADA → VERIFICADA → PENDIENTE_VALIDACION → APROBADA / RECHAZADA`. El login pleno requiere `APROBADA`.
- Validación de unicidad de email, CUIT y DNI.

### ✅ Panel del superadmin
- Rol `SUPERADMIN` único (sembrado). Revisa las solicitudes de alta pendientes con todos los datos y **previsualiza los documentos** (imágenes y PDF) sin descargarlos.
- Aprueba o rechaza (con motivo); en ambos casos se notifica al dueño por email.

### ✅ Gestión operativa — ABM completo (Sprint 2)
Todo con **aislamiento multi-tenant** (cada dueño accede solo a los datos de su empresa) y verificado de punta a punta:
- **Empresa** (Mi empresa): ver / editar / activar-desactivar, CUIT único.
- **Sucursales**: ABM, nombre único por empresa, no se desactiva con empleados activos.
- **Empleados**: ABM con alta automática de su usuario (rol EMPLEADO + contraseña temporal por email), filtrable por sucursal.
- **Mesas**: ABM, número único por sucursal.
- **Grupos de propina**: ABM, nombre único por sucursal.
- **Asignación de empleados a grupos**: relación N:N (solo empleados de la misma sucursal, sin duplicar).
- **Panel del encargado**: un empleado puede marcarse como encargado; ve en solo lectura los empleados, mesas y grupos de su sucursal.

### ✅ Del QR a la propina pagada (Sprint 3)

#### Órdenes de propina (modelo, estados y eventos)
- Cada propina es una **orden** con código único, tipo, monto, estado y vencimiento.
- Ciclo de estados: `CREADA → PENDIENTE_PAGO → PAGADA / RECHAZADA / CANCELADA / EXPIRADA`. Cada cambio registra un **EventoOrden** (trazabilidad completa).
- **Expiración automática**: una tarea programada (`@Scheduled`) expira las órdenes sin pagar vencidas. El plazo es configurable.
- Endpoint público de consulta de estado, usado por la pantalla pública mientras espera el pago.

#### Generación de códigos QR
- Se genera **automáticamente** un QR único por mesa y por empleado al darlos de alta (idempotente).
- Cada QR apunta a la pantalla pública de propina. **Descarga e previsualización del PNG** en el panel de administración (con regeneración del código).

#### Pantalla pública de propina (sin login)
- El cliente escanea el QR, ve el destino (mesa/empleado) y el comercio, elige un **monto preestablecido o libre** y continúa al pago. Pantalla mobile-first.

#### Integración con Mercado Pago (Checkout Pro)
- Al confirmar el monto se crea la **preferencia de pago** y se redirige al Checkout Pro; la orden pasa a `PENDIENTE_PAGO`.
- **Webhook** que recibe la notificación de Mercado Pago, consulta el pago y **concilia** la orden automáticamente (`approved → PAGADA`, `rejected → RECHAZADA`), registrando el `Pago` y el `WebhookPago`.
- **Validación de firma** (`x-signature`, HMAC-SHA256) implementada y configurable: se valida en producción y se desactiva en el sandbox (los pagos de usuarios de prueba se firman con un secreto no expuesto).
- Pantalla de resultado del pago con consulta del estado (aprobado / rechazado / pendiente).

### ✅ Diseño
- Rediseño editorial completo de toda la interfaz (tipografía Fraunces + Hanken Grotesk, paleta bordó/rosa/navy, superficies crema) con un design system propio en `styles.css`.

### ✅ Calidad
- **163 tests unitarios** (JUnit 5 + Mockito) cubriendo seguridad, servicios (incluidos órdenes, QR y pagos), controladores, entidades y DTOs.

### 🚧 En curso / próximos
- Historial de propinas del empleado y dashboard de resumen del dueño (resto del Sprint 3).
- Distribución de propinas grupales, dashboards y reportes (Sprint 4).
- Comunicaciones internas y agentes de IA (Sprint 5).

---

## Módulos del sistema

- **Autenticación y roles:** dueño/administrador, encargado y empleado. El cliente que escanea el QR no requiere login.
- **Gestión operativa:** empresa, sucursales, empleados, mesas, turnos y grupos de propina.
- **QR y pantalla pública:** generación de QR o links de propina por empleado, mesa, sucursal o grupo. Pantalla pública para que el cliente elija tipo de propina, monto y continúe al pago.
- **Órdenes de propina:** cada propina genera una orden con identificador único, tipo, monto, estado, vencimiento e historial de eventos.
- **Propinas individuales:** asociadas a un empleado específico.
- **Propinas grupales:** asociadas a un equipo, mesa, turno o sucursal, con distribución equitativa entre los empleados asignados al grupo.
- **Integración de pagos:** preferencias de pago con Mercado Pago, recepción de webhooks y conciliación automática del estado de la orden.
- **Dashboard del empleado:** propinas recibidas, mesas, QR personal y notificaciones.
- **Dashboard administrativo:** indicadores generales, KPIs y reportes sin exponer detalle sensible de cada empleado.
- **Comunicaciones internas:** envío manual de avisos a empleados, sucursales, turnos o roles.
- **Agente de notificaciones internas:** transforma instrucciones informales en comunicados claros y estructurados.
- **Agente de reportes automáticos:** genera resúmenes operativos de propinas, actividad, ranking y alertas.

### Fuera del alcance

Billetera virtual propia, custodia de dinero, liquidaciones bancarias reales, transferencias entre usuarios, facturación electrónica, aplicación mobile nativa, chat en tiempo real y decisiones autónomas de IA sin confirmación humana.

---

## Modelo de base de datos

### Diagrama general

```
EMPRESA
  ├── SUCURSAL
  │     ├── EMPLEADO
  │     │      └── USUARIO
  │     │
  │     ├── MESA
  │     │
  │     ├── GRUPO_PROPINA
  │     │      └── GRUPO_PROPINA_EMPLEADO
  │     │
  │     ├── CODIGO_QR
  │     │
  │     ├── ORDEN_PROPINA
  │     │      ├── PAGO
  │     │      │    └── WEBHOOK_PAGO
  │     │      ├── EVENTO_ORDEN
  │     │      └── DISTRIBUCION_PROPINA
  │     │
  │     └── NOTIFICACION
  │            └── NOTIFICACION_DESTINATARIO
  │
  └── REPORTE_AUTOMATICO

USUARIO
  └── NOTIFICACION_DESTINATARIO
```

### Entidades

#### Usuario
Persona que accede al sistema (superadmin, dueño, encargado o empleado).

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| nombre | String | Nombre del usuario |
| apellido | String | Apellido del usuario |
| email | String | Email único |
| password | String | Contraseña encriptada (BCrypt) |
| telefono | String | Teléfono |
| cuit | String | CUIT (único) |
| dni | String | DNI (único) |
| rol | Enum | SUPERADMIN, DUENO, ENCARGADO, EMPLEADO |
| estadoCuenta | Enum | CREADA, VERIFICADA, PENDIENTE_VALIDACION, APROBADA, RECHAZADA |
| emailVerificado | Boolean | Si verificó su email |
| emailToken | String | Token de verificación / registro (nullable) |
| empresa_id | Long | FK → Empresa (nullable; el superadmin no tiene) |
| estado | Boolean | Activo/Inactivo |
| fechaCreacion | LocalDateTime | Fecha de alta |

**Relaciones:** `Usuario N → 1 Empresa`, `Usuario 1 → 1 Empleado`

#### DocumentoRegistro
Documento adjunto por el dueño en el registro (DNI frente/dorso, selfie, constancia AFIP). El binario se guarda en la base.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| usuario_id | Long | FK → Usuario |
| tipo | Enum | DNI_FRENTE, DNI_DORSO, SELFIE, CONSTANCIA_AFIP |
| nombreArchivo | String | Nombre original del archivo |
| contentType | String | Tipo MIME (image/* o application/pdf) |
| datos | byte[] | Contenido binario |
| fechaCarga | LocalDateTime | Fecha de carga |

---

#### Empresa
Comercio principal registrado en la plataforma.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| nombre | String | Razón social / nombre del comercio |
| nombreFantasia | String | Nombre de fantasía (opcional) |
| rubro | String | Rubro del negocio |
| cuit | String | CUIT del comercio (único) |
| provincia | String | Provincia |
| calle | String | Calle |
| numeracion | String | Numeración |
| emailContacto | String | Email de contacto |
| telefono | String | Teléfono |
| estado | Boolean | Activo/Inactivo |
| fechaCreacion | LocalDateTime | Fecha de alta |

**Relaciones:** `Empresa 1 → N Sucursal`, `Empresa 1 → N Usuario`

---

#### Sucursal
Local físico de una empresa.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| empresa_id | Long | FK → Empresa |
| nombre | String | Nombre de la sucursal |
| direccion | String | Dirección física |
| telefono | String | Teléfono |
| estado | Boolean | Activo/Inactivo |
| fechaCreacion | LocalDateTime | Fecha de alta |

**Relaciones:** `Sucursal N → 1 Empresa`, `Sucursal 1 → N Empleado`, `Sucursal 1 → N Mesa`, `Sucursal 1 → N GrupoPropina`, `Sucursal 1 → N OrdenPropina`

---

#### Empleado
Persona que trabaja en el comercio y puede recibir propinas.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| usuario_id | Long | FK → Usuario |
| sucursal_id | Long | FK → Sucursal |
| nombreVisible | String | Nombre que ve el cliente en el QR |
| puesto | String | Puesto o cargo |
| estado | Boolean | Activo/Inactivo |
| fechaAlta | LocalDateTime | Fecha de alta |

**Relaciones:** `Empleado N → 1 Sucursal`, `Empleado 1 → 1 Usuario`, `Empleado 1 → N OrdenPropina`, `Empleado N → N GrupoPropina`

---

#### Mesa
Mesa física de un local.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| sucursal_id | Long | FK → Sucursal |
| numero | Integer | Número de mesa |
| descripcion | String | Descripción opcional |
| estado | Boolean | Activo/Inactivo |

**Relaciones:** `Mesa N → 1 Sucursal`, `Mesa 1 → N OrdenPropina`

---

#### GrupoPropina
Equipo o grupo que recibe propinas de forma conjunta (ej: "Equipo Turno Noche", "Barra").

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| sucursal_id | Long | FK → Sucursal |
| nombre | String | Nombre del grupo |
| descripcion | String | Descripción |
| tipoGrupo | String | Tipo de agrupación |
| estado | Boolean | Activo/Inactivo |

**Relaciones:** `GrupoPropina N → 1 Sucursal`, `GrupoPropina N → N Empleado`

---

#### GrupoPropinaEmpleado
Tabla intermedia para la relación Empleado ↔ GrupoPropina.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| grupo_propina_id | Long | FK → GrupoPropina |
| empleado_id | Long | FK → Empleado |
| porcentajeDistribucion | Double | Porcentaje (opcional, por defecto equitativo) |
| activo | Boolean | Activo/Inactivo |

---

#### CodigoQR
QR o link asociado a un empleado, mesa, grupo o sucursal.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| codigo | String | Código único |
| tipoDestino | Enum | EMPLEADO, MESA, GRUPO, SUCURSAL |
| empleado_id | Long | FK → Empleado (nullable) |
| mesa_id | Long | FK → Mesa (nullable) |
| grupo_propina_id | Long | FK → GrupoPropina (nullable) |
| sucursal_id | Long | FK → Sucursal |
| url | String | URL a la que apunta el QR |
| activo | Boolean | Activo/Inactivo |
| fechaCreacion | LocalDateTime | Fecha de creación |

---

#### OrdenPropina
Entidad central del sistema. Representa una intención de propina desde su creación hasta el pago.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| codigo | String | Identificador único de la orden |
| sucursal_id | Long | FK → Sucursal |
| mesa_id | Long | FK → Mesa (nullable) |
| empleado_id | Long | FK → Empleado (nullable si es grupal) |
| grupo_propina_id | Long | FK → GrupoPropina (nullable si es individual) |
| tipoPropina | Enum | INDIVIDUAL, GRUPAL |
| monto | BigDecimal | Monto de la propina |
| estado | Enum | CREADA, PENDIENTE_PAGO, PAGADA, RECHAZADA, CANCELADA, EXPIRADA |
| fechaCreacion | LocalDateTime | Fecha de creación |
| fechaExpiracion | LocalDateTime | Vencimiento de la orden |
| fechaPago | LocalDateTime | Fecha en que se confirmó el pago |

**Regla:** Si `tipoPropina = INDIVIDUAL` → tiene `empleado_id`. Si `tipoPropina = GRUPAL` → tiene `grupo_propina_id`.

**Relaciones:** `OrdenPropina 1 → 1 Pago`, `OrdenPropina 1 → N EventoOrden`, `OrdenPropina 1 → N DistribucionPropina`

---

#### Pago
Información del pago externo procesado con Mercado Pago.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| orden_propina_id | Long | FK → OrdenPropina |
| proveedor | Enum | MERCADO_PAGO |
| external_payment_id | String | ID devuelto por Mercado Pago |
| preference_id | String | ID de preferencia MP |
| init_point | String | URL de pago de MP |
| estadoProveedor | String | approved / pending / rejected |
| monto | BigDecimal | Monto del pago |
| moneda | String | ARS |
| fechaCreacion | LocalDateTime | Fecha de creación |
| fechaActualizacion | LocalDateTime | Última actualización |

**Relaciones:** `Pago 1 → 1 OrdenPropina`, `Pago 1 → N WebhookPago`

---

#### WebhookPago
Notificaciones recibidas desde Mercado Pago. Permite trazabilidad completa.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| pago_id | Long | FK → Pago |
| proveedor | String | Proveedor del webhook |
| tipoEvento | String | Tipo de evento recibido |
| external_id | String | ID externo del evento |
| payload | JSONB | Cuerpo completo del webhook |
| fechaRecepcion | LocalDateTime | Fecha de recepción |
| procesado | Boolean | Si fue procesado |

---

#### DistribucionPropina
Registro de cómo se distribuyó una propina grupal entre los empleados del grupo.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| orden_propina_id | Long | FK → OrdenPropina |
| empleado_id | Long | FK → Empleado |
| montoAsignado | BigDecimal | Monto asignado al empleado |
| porcentaje | Double | Porcentaje asignado |
| criterio | String | Criterio de distribución (equitativo, etc.) |
| fechaCreacion | LocalDateTime | Fecha de distribución |

---

#### EventoOrden
Historial de cambios de estado de una orden de propina.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| orden_propina_id | Long | FK → OrdenPropina |
| tipoEvento | Enum | ORDEN_CREADA, PREFERENCIA_MP_GENERADA, PAGO_CONFIRMADO, ORDEN_PAGADA, ORDEN_RECHAZADA, ORDEN_CANCELADA, ORDEN_EXPIRADA, DISTRIBUCION_GENERADA |
| descripcion | String | Descripción del evento |
| fecha | LocalDateTime | Fecha del evento |

---

#### Notificacion
Aviso interno enviado a empleados, sucursales, turnos o roles.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| empresa_id | Long | FK → Empresa |
| sucursal_id | Long | FK → Sucursal (nullable) |
| creado_por_usuario_id | Long | FK → Usuario |
| titulo | String | Título del aviso |
| mensaje | String | Cuerpo del mensaje |
| categoria | Enum | OPERATIVA, STOCK, HORARIO, PAGOS, GENERAL |
| prioridad | Enum | BAJA, MEDIA, ALTA |
| origen | Enum | MANUAL, AGENTE |
| fechaCreacion | LocalDateTime | Fecha de creación |

**Relaciones:** `Notificacion 1 → N NotificacionDestinatario`

---

#### NotificacionDestinatario
Destinatarios de una notificación y su estado de lectura.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| notificacion_id | Long | FK → Notificacion |
| usuario_id | Long | FK → Usuario |
| leida | Boolean | Si fue leída |
| fechaLectura | LocalDateTime | Fecha en que la leyó |

---

#### ReporteAutomatico
Almacena los reportes generados por el agente externo (n8n / script) para mostrarlos en el dashboard administrativo.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| empresa_id | Long | FK → Empresa |
| sucursal_id | Long | FK → Sucursal (nullable) |
| tipoReporte | Enum | DIARIO, SEMANAL, MENSUAL, PERSONALIZADO |
| periodoDesde | LocalDate | Inicio del período |
| periodoHasta | LocalDate | Fin del período |
| resumenGenerado | String | Texto del reporte generado por el agente |
| fechaGeneracion | LocalDateTime | Fecha de generación |

---

## Diagrama Entidad-Relación

```mermaid
erDiagram
    EMPRESA {
        bigint id PK
        string nombre
        string rubro
        string cuit
        string emailContacto
        string telefono
        boolean estado
        datetime fechaCreacion
    }

    SUCURSAL {
        bigint id PK
        bigint empresa_id FK
        string nombre
        string direccion
        string telefono
        boolean estado
        datetime fechaCreacion
    }

    USUARIO {
        bigint id PK
        bigint empresa_id FK
        string nombre
        string apellido
        string email
        string password
        string rol
        boolean estado
        datetime fechaCreacion
    }

    EMPLEADO {
        bigint id PK
        bigint usuario_id FK
        bigint sucursal_id FK
        string nombreVisible
        string puesto
        boolean estado
        datetime fechaAlta
    }

    MESA {
        bigint id PK
        bigint sucursal_id FK
        int numero
        string descripcion
        boolean estado
    }

    GRUPO_PROPINA {
        bigint id PK
        bigint sucursal_id FK
        string nombre
        string descripcion
        string tipoGrupo
        boolean estado
    }

    GRUPO_PROPINA_EMPLEADO {
        bigint id PK
        bigint grupo_propina_id FK
        bigint empleado_id FK
        double porcentajeDistribucion
        boolean activo
    }

    CODIGO_QR {
        bigint id PK
        bigint sucursal_id FK
        bigint empleado_id FK
        bigint mesa_id FK
        bigint grupo_propina_id FK
        string codigo
        string tipoDestino
        string url
        boolean activo
        datetime fechaCreacion
    }

    ORDEN_PROPINA {
        bigint id PK
        bigint sucursal_id FK
        bigint mesa_id FK
        bigint empleado_id FK
        bigint grupo_propina_id FK
        string codigo
        string tipoPropina
        decimal monto
        string estado
        datetime fechaCreacion
        datetime fechaExpiracion
        datetime fechaPago
    }

    PAGO {
        bigint id PK
        bigint orden_propina_id FK
        string proveedor
        string external_payment_id
        string preference_id
        string init_point
        string estadoProveedor
        decimal monto
        string moneda
        datetime fechaCreacion
        datetime fechaActualizacion
    }

    WEBHOOK_PAGO {
        bigint id PK
        bigint pago_id FK
        string proveedor
        string tipoEvento
        string external_id
        text payload
        datetime fechaRecepcion
        boolean procesado
    }

    DISTRIBUCION_PROPINA {
        bigint id PK
        bigint orden_propina_id FK
        bigint empleado_id FK
        decimal montoAsignado
        double porcentaje
        string criterio
        datetime fechaCreacion
    }

    EVENTO_ORDEN {
        bigint id PK
        bigint orden_propina_id FK
        string tipoEvento
        string descripcion
        datetime fecha
    }

    NOTIFICACION {
        bigint id PK
        bigint empresa_id FK
        bigint sucursal_id FK
        bigint creado_por_usuario_id FK
        string titulo
        string mensaje
        string categoria
        string prioridad
        string origen
        datetime fechaCreacion
    }

    NOTIFICACION_DESTINATARIO {
        bigint id PK
        bigint notificacion_id FK
        bigint usuario_id FK
        boolean leida
        datetime fechaLectura
    }

    REPORTE_AUTOMATICO {
        bigint id PK
        bigint empresa_id FK
        bigint sucursal_id FK
        string tipoReporte
        date periodoDesde
        date periodoHasta
        string resumenGenerado
        datetime fechaGeneracion
    }

    EMPRESA ||--o{ SUCURSAL : "tiene"
    EMPRESA ||--o{ USUARIO : "tiene"
    EMPRESA ||--o{ NOTIFICACION : "genera"
    EMPRESA ||--o{ REPORTE_AUTOMATICO : "tiene"

    SUCURSAL ||--o{ EMPLEADO : "tiene"
    SUCURSAL ||--o{ MESA : "tiene"
    SUCURSAL ||--o{ GRUPO_PROPINA : "tiene"
    SUCURSAL ||--o{ CODIGO_QR : "tiene"
    SUCURSAL ||--o{ ORDEN_PROPINA : "registra"
    SUCURSAL }o--|| NOTIFICACION : "recibe"
    SUCURSAL }o--|| REPORTE_AUTOMATICO : "tiene"

    USUARIO ||--o| EMPLEADO : "es"
    USUARIO ||--o{ NOTIFICACION_DESTINATARIO : "recibe"
    USUARIO ||--o{ NOTIFICACION : "crea"

    EMPLEADO ||--o{ CODIGO_QR : "tiene"
    EMPLEADO ||--o{ ORDEN_PROPINA : "recibe"
    EMPLEADO ||--o{ GRUPO_PROPINA_EMPLEADO : "integra"
    EMPLEADO ||--o{ DISTRIBUCION_PROPINA : "recibe"

    MESA ||--o{ CODIGO_QR : "tiene"
    MESA ||--o{ ORDEN_PROPINA : "genera"

    GRUPO_PROPINA ||--o{ GRUPO_PROPINA_EMPLEADO : "contiene"
    GRUPO_PROPINA ||--o{ CODIGO_QR : "tiene"
    GRUPO_PROPINA ||--o{ ORDEN_PROPINA : "recibe"

    ORDEN_PROPINA ||--|| PAGO : "genera"
    ORDEN_PROPINA ||--o{ EVENTO_ORDEN : "registra"
    ORDEN_PROPINA ||--o{ DISTRIBUCION_PROPINA : "distribuye"

    PAGO ||--o{ WEBHOOK_PAGO : "recibe"

    NOTIFICACION ||--o{ NOTIFICACION_DESTINATARIO : "tiene"
```

---

## API REST

Base URL: `http://localhost:8080`. Todos los endpoints (excepto los públicos) requieren el header `Authorization: Bearer <token>`.

### Endpoints públicos

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/health` | Health check del servicio |
| `POST` | `/api/auth/login` | Login. Devuelve token JWT + datos del usuario |
| `POST` | `/api/registro/paso1` | Alta del usuario + captcha + envío de email de verificación |
| `GET` | `/api/registro/verificar?token=` | Verifica el email (link del correo) |
| `GET` | `/api/registro/estado?token=` | Estado del registro (polling del wizard) |
| `POST` | `/api/registro/paso2?token=` | Datos del comercio |
| `POST` | `/api/registro/documentos?token=&tipo=` | Subida de documento (multipart) |
| `POST` | `/api/registro/finalizar?token=` | Finaliza → cuenta en PENDIENTE_VALIDACION |
| `GET` | `/api/public/qr/{codigo}` | Resuelve el QR escaneado (destino + comercio) |
| `POST` | `/api/public/qr/{codigo}/ordenes` | Crea la orden de propina con el monto elegido |
| `POST` | `/api/public/ordenes/{codigo}/pago` | Inicia el pago: crea la preferencia de Mercado Pago |
| `GET` | `/api/public/pagos/retorno?orden=` | Retorno del Checkout Pro (redirige al frontend) |
| `POST` | `/api/public/pagos/webhook` | Webhook de Mercado Pago (conciliación de la orden) |
| `GET` | `/api/ordenes/{codigo}/estado` | Estado de una orden (polling de la pantalla pública) |
| `GET` | `/swagger-ui.html` | Documentación interactiva OpenAPI |

### Perfil (usuario autenticado)

| Método | Endpoint | Rol requerido |
|---|---|---|
| `GET` | `/api/perfil` | Cualquier autenticado |
| `GET` | `/api/admin/perfil` | `DUENO`, `ENCARGADO` |
| `GET` | `/api/empleado/perfil` | `EMPLEADO` |

> Todas las entidades operativas están **acotadas a la empresa del usuario autenticado** (multi-tenant): un recurso de otra empresa responde `404`. La lectura es para `DUENO` y `ENCARGADO`; la escritura, solo `DUENO`.

### Mi empresa

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/empresas/mia` | Empresa del usuario |
| `GET` / `PUT` | `/api/empresas/{id}` | Ver / editar (propia) |
| `PATCH` | `/api/empresas/{id}/estado?estado=` | Activar / desactivar |

### Sucursales · Mesas · Grupos de propina

Mismo patrón ABM (`GET` lista con filtro `?sucursalId=`, `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}/estado`):

| Recurso | Base |
|---|---|
| Sucursales | `/api/sucursales` |
| Mesas | `/api/mesas` |
| Grupos de propina | `/api/grupos-propina` |

### Empleados

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/empleados?sucursalId=` | Listar (filtrable por sucursal) |
| `POST` | `/api/empleados` | Crear (alta automática de usuario + contraseña temporal) |
| `PUT` | `/api/empleados/{id}` | Editar |
| `PATCH` | `/api/empleados/{id}/estado?estado=` | Activar / desactivar |
| `PATCH` | `/api/empleados/{id}/encargado?valor=` | Marcar / quitar encargado |
| `GET` | `/api/empleados/mi-sucursal` | Sucursal del usuario logueado (panel del encargado) |

### Miembros de un grupo de propina

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/grupos-propina/{id}/empleados` | Listar miembros |
| `POST` | `/api/grupos-propina/{id}/empleados` | Agregar empleado (de la misma sucursal) |
| `DELETE` | `/api/grupos-propina/{id}/empleados/{empleadoId}` | Remover empleado |

### Códigos QR (`DUENO`, `ENCARGADO`)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/qr?sucursalId=` | Listar QR (filtrable por sucursal) |
| `GET` | `/api/qr/{id}/imagen` | Imagen PNG del QR (previsualizar / descargar) |
| `POST` | `/api/qr/{id}/regenerar` | Regenerar el código del QR (`DUENO`) |

### Superadmin (`SUPERADMIN`)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/superadmin/solicitudes` | Solicitudes de alta pendientes |
| `GET` | `/api/superadmin/solicitudes/{id}` | Detalle (datos + documentos) |
| `GET` | `/api/superadmin/documentos/{docId}` | Descargar / previsualizar documento |
| `POST` | `/api/superadmin/solicitudes/{id}/aprobar` | Aprobar (+ email) |
| `POST` | `/api/superadmin/solicitudes/{id}/rechazar?motivo=` | Rechazar (+ email) |

Los errores se devuelven con un cuerpo uniforme: `{ "status": 409, "error": "...", "timestamp": "..." }` (404 inexistente/ajeno, 409 duplicado, 400 validación, 401 sin token, 403 sin permiso).

### Roles

| Rol | Acceso |
|---|---|
| `SUPERADMIN` | Dueño del producto: valida las altas de comercios. Único, sembrado. |
| `DUENO` | Acceso total a su empresa: administración y escritura sobre todas sus entidades |
| `ENCARGADO` | Panel de solo lectura de su sucursal (empleados, mesas, grupos) |
| `EMPLEADO` | Panel propio: sus propinas, mesas y notificaciones |

---

## Planificación

| Sprint | Período | Objetivo | Estado |
|---|---|---|---|
| Sprint 0 | 14/05 – 18/05 | Entorno, repositorio, BD y estructura inicial | ✅ Completado |
| Sprint 1 | 19/05 – 01/06 | Login, roles y estructura base frontend/backend | ✅ Completado |
| Sprint 2 | 02/06 – 15/06 | ABM empresa, sucursales, empleados, mesas y grupos + onboarding, superadmin y panel encargado | ✅ Completado |
| Sprint 3 | 16/06 – 29/06 | QR, pantalla pública, órdenes de propina e integración Mercado Pago | 🚧 En curso (QR, pantalla pública, órdenes y pago con MP ✅) |
| Sprint 4 | 30/06 – 13/07 | Conciliación, distribución grupal, dashboards y reportes | ⏳ Pendiente |
| Sprint 5 | 14/07 – 20/07 | Comunicaciones internas, agentes IA, testing y demo final | ⏳ Pendiente |

---

## Estructura del repositorio

```
TipQR/
├── Back/                       # Java Spring Boot — API REST
│   └── src/main/java/tipqr/back/
│       ├── config/             # Seguridad, OpenAPI, datos iniciales
│       ├── controller/         # Endpoints REST + manejo global de errores
│       ├── dto/                # Objetos de entrada/salida
│       ├── entity/             # 16 entidades JPA + enums
│       ├── exception/          # Excepciones de negocio (404, 409)
│       ├── repository/         # Spring Data JPA
│       ├── security/           # JWT, filtros, entry points
│       └── service/            # Lógica de negocio
│
├── Front/                      # Angular 21 — Interfaz web
│   └── src/app/
│       ├── core/               # Servicios, guards, interceptores, modelos
│       ├── features/           # auth (login/registro), dashboards, empresa,
│       │                       #   sucursal, empleado, mesa, grupo, qr,
│       │                       #   encargado, superadmin, landing,
│       │                       #   public (pantalla de propina + resultado de pago)
│       └── shared/             # Layout y componentes reutilizables
│
└── docker-compose.yml          # PostgreSQL + pgAdmin
```

> El design system (tokens de color, tipografía y clases reutilizables `.btn`/`.card`/`.input`/`.badge`/`.eyebrow`) vive en `Front/src/styles.css`.

---

## Cómo levantar el proyecto localmente

### Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Java 21
- Node.js 20+

### 1. Base de datos (Docker)

```bash
docker compose up -d
```

Levanta PostgreSQL en `localhost:5432` y pgAdmin en `http://localhost:5050`.

| pgAdmin | Valor |
|---|---|
| Email | admin@tipqr.com |
| Password | admin |
| Host BD | tipqr-postgres |
| Puerto BD | 5432 |
| Usuario BD | postgres |
| Password BD | postgres |

### 2. Backend

```bash
cd Back
cp .env.example .env
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Health check: `GET http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

> Las credenciales (DB, SMTP, reCAPTCHA, JWT, Mercado Pago) se configuran con variables de entorno en el archivo `Back/.env` (excluido del repositorio). Ver `.env.example` como referencia. El registro real necesita credenciales SMTP y claves de reCAPTCHA v2.

#### Variables de Mercado Pago (`Back/.env`)

> Solo nombres de variables — los valores reales nunca se versionan.

| Variable | Descripción |
|---|---|
| `MP_ACCESS_TOKEN` | Access token del vendedor (usuario de prueba en sandbox) |
| `MP_PUBLIC_KEY` | Public key de la aplicación |
| `MP_WEBHOOK_URL` | URL pública base que recibe el webhook y el retorno (en local, la URL de ngrok) |
| `MP_WEBHOOK_SECRET` | Secreto para validar la firma del webhook |
| `MP_VALIDATE_SIGNATURE` | `true` en producción; `false` en sandbox (los webhooks de usuarios de prueba se firman con un secreto no expuesto) |

#### Webhook en local (ngrok)

Mercado Pago necesita una URL pública para enviar el webhook; `localhost` no es accesible. Para probar el pago de punta a punta se expone el backend con un túnel:

```bash
ngrok http 8080
```

La URL `https://...ngrok-free.app` que devuelve se usa como `MP_WEBHOOK_URL`. Para correr los tests no hace falta ngrok.

Al iniciar por primera vez se crean automáticamente usuarios de prueba:

| Email | Password | Rol |
|---|---|---|
| `superadmin@tipqr.com` | `superadmin2026` | `SUPERADMIN` |
| `admin@tipqr.com` | `tipqr2026` | `DUENO` |

### 3. Frontend

```bash
cd Front
npm install
npm start
```

La app corre en `http://localhost:4200`.

### Tests del backend

```bash
cd Back
./mvnw test
```

Ejecuta los 163 tests unitarios (JUnit 5 + Mockito) sobre una base H2 en memoria.

### Bajar los servicios

```bash
docker compose down
```

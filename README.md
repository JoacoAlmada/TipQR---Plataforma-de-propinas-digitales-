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
| Backend | Java 21 + Spring Boot 4 |
| Frontend | Angular 21 + Tailwind CSS 4 |
| Base de datos | PostgreSQL |
| Autenticación | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Documentación API | Swagger / OpenAPI |
| Pagos | Mercado Pago Checkout Pro (ambiente de prueba) + Webhooks |
| Testing | JUnit 5, Mockito, Postman |
| Utilidades | Lombok, librería QR |

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
Persona que accede al sistema (dueño, encargado o empleado).

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| nombre | String | Nombre del usuario |
| apellido | String | Apellido del usuario |
| email | String | Email único |
| password | String | Contraseña encriptada |
| rol | Enum | DUENO, ENCARGADO, EMPLEADO |
| estado | Boolean | Activo/Inactivo |
| fechaCreacion | LocalDateTime | Fecha de alta |

---

#### Empresa
Comercio principal registrado en la plataforma.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | PK |
| nombre | String | Nombre del comercio |
| rubro | String | Rubro del negocio |
| cuit | String | CUIT del comercio |
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
| tipoEvento | Enum | ORDEN_CREADA, PREFERENCIA_MP_GENERADA, PAGO_CONFIRMADO, ORDEN_PAGADA, ORDEN_EXPIRADA, DISTRIBUCION_GENERADA |
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

## Planificación

| Sprint | Período | Objetivo |
|---|---|---|
| Sprint 0 | 14/05 – 18/05 | Entorno, repositorio, BD y estructura inicial |
| Sprint 1 | 19/05 – 01/06 | ABM empresa, sucursales, empleados, mesas y grupos de propina |
| Sprint 2 | 02/06 – 15/06 | Login, roles y estructura base frontend/backend |
| Sprint 3 | 16/06 – 29/06 | QR, pantalla pública, órdenes de propina e integración Mercado Pago |
| Sprint 4 | 30/06 – 13/07 | Conciliación, distribución grupal, dashboards y reportes |
| Sprint 5 | 14/07 – 20/07 | Comunicaciones internas, agentes IA, testing y demo final |

---

## Estructura del repositorio

```
TipQR/
├── Back/         # Java Spring Boot — API REST
└── Front/        # Angular 21 — Interfaz web
```



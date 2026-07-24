import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { InfoPageComponent } from '../../shared/components/info-page/info-page.component';

interface Pregunta { q: string; a: string; }

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [InfoPageComponent, RouterLink],
  templateUrl: './faq.component.html'
})
export class FaqComponent {
  abierta = signal<number | null>(0);

  readonly preguntas: Pregunta[] = [
    { q: '¿Qué es TipQR?',
      a: 'Una plataforma web para que comercios gastronómicos (bares, restaurantes, cafeterías, hoteles) reciban propinas digitales por QR. El cliente escanea, elige un monto y paga con Mercado Pago, sin efectivo ni registro.' },
    { q: '¿Para qué tipo de comercios sirve?',
      a: 'Para cualquier comercio de atención al público con personal que reciba propinas: bares, restaurantes, cafeterías, hoteles, food trucks y servicios similares. Se puede organizar por empresa, sucursales, mesas y grupos.' },
    { q: '¿El cliente necesita descargar una app o registrarse?',
      a: 'No. El cliente solo escanea el código QR con la cámara de su celular, elige el monto y paga. No hace falta instalar nada ni crear una cuenta.' },
    { q: '¿TipQR guarda mi dinero?',
      a: 'No. TipQR no es una billetera ni custodia fondos, tampoco hace intermediación financiera. El pago lo procesa Mercado Pago; TipQR solo registra, concilia y refleja la distribución de la propina entre el personal.' },
    { q: '¿Qué medios de pago acepta?',
      a: 'Los pagos se procesan con Mercado Pago (Checkout Pro), que admite tarjetas de crédito, débito y dinero en cuenta de Mercado Pago.' },
    { q: '¿Cómo se reparte una propina de mesa?',
      a: 'Al escanear el QR de una mesa, el cliente puede dejarle la propina a un mozo puntual o al equipo. Si elige el equipo, se reparte entre los integrantes del grupo que está en el turno activo.' },
    { q: '¿Se puede repartir por porcentaje y no solo en partes iguales?',
      a: 'Sí. Cada grupo puede repartir de forma equitativa (partes iguales) o por porcentaje configurable por integrante. El reparto siempre es exacto al centavo.' },
    { q: '¿Qué es el "turno activo"?',
      a: 'Es el grupo de empleados que está trabajando en ese momento en la sucursal. El encargado lo abre y lo cambia al rotar el turno; las propinas de mesa se reparten entre ese grupo.' },
    { q: '¿Cómo se generan los códigos QR?',
      a: 'Se generan automáticamente al crear una mesa o un empleado. Desde el panel se pueden previsualizar y descargar en PNG para imprimirlos y colocarlos en el local.' },
    { q: '¿Cómo veo mis propinas si soy empleado?',
      a: 'Cada empleado tiene su panel con el historial de propinas recibidas (las individuales y su parte de las grupales) y su código QR personal.' },
    { q: '¿Qué ve el dueño en su panel?',
      a: 'El dueño ve indicadores (total recaudado, ticket promedio, cantidad), el ranking de empleados, reportes por período y un resumen ejecutivo generado con asistencia de IA.' },
    { q: '¿Un mismo dueño puede administrar varias empresas?',
      a: 'Sí. Un dueño puede tener varias empresas y cambiar cuál está gestionando; toda la información (sucursales, empleados, propinas) corresponde a la empresa activa en ese momento.' },
    { q: '¿Qué son las notificaciones internas?',
      a: 'El dueño o encargado puede enviar avisos al personal, segmentando por empresa, sucursal, turno o rol. Además, un agente de IA ayuda a redactar el aviso a partir de una idea informal, siempre con revisión humana antes de enviarlo.' },
    { q: '¿Es seguro?',
      a: 'Trabajamos con contraseñas cifradas, autenticación por token, control de acceso por rol y aislamiento de los datos de cada empresa. Los datos de tarjetas los maneja Mercado Pago, no TipQR.' },
    { q: '¿Tiene costo?',
      a: 'TipQR es un proyecto académico (Trabajo Final Integrador). El uso de Mercado Pago se rige por las comisiones y condiciones del propio procesador de pagos.' },
    { q: '¿Puedo pedir la baja de mi cuenta o de mis datos?',
      a: 'Sí. Podés solicitar la baja de la cuenta y el acceso, rectificación o eliminación de tus datos escribiéndonos. Más detalles en la Política de Privacidad.' },
  ];

  toggle(i: number): void {
    this.abierta.set(this.abierta() === i ? null : i);
  }
}

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
      a: 'Una plataforma web para que comercios gastronómicos reciban propinas digitales por QR. El cliente escanea, elige un monto y paga con Mercado Pago, sin efectivo ni registro.' },
    { q: '¿El cliente necesita descargar una app o registrarse?',
      a: 'No. El cliente solo escanea el código QR con la cámara de su celular, elige el monto y paga. No hace falta instalar nada ni crear una cuenta.' },
    { q: '¿Cómo se reparte una propina de mesa?',
      a: 'Al escanear el QR de una mesa, el cliente puede dejarle la propina a un mozo puntual o al equipo. Si elige el equipo, se reparte de forma equitativa entre los integrantes del grupo que está en el turno activo.' },
    { q: '¿Qué es el "turno activo"?',
      a: 'Es el grupo de empleados que está trabajando en ese momento en la sucursal. El encargado lo abre y lo cambia al rotar el turno; las propinas de mesa se reparten entre ese grupo.' },
    { q: '¿TipQR guarda mi dinero?',
      a: 'No. TipQR no es una billetera ni custodia fondos. El pago lo procesa Mercado Pago; TipQR solo registra, concilia y distribuye la propina entre el personal.' },
    { q: '¿Cómo veo mis propinas?',
      a: 'Cada empleado tiene su panel con el historial de propinas recibidas (individuales y su parte de las grupales) y su código QR personal. El dueño ve reportes, ranking y totales por sucursal.' },
    { q: '¿Cómo se generan los códigos QR?',
      a: 'Se generan automáticamente al crear una mesa o un empleado. Desde el panel se pueden previsualizar y descargar en PNG para imprimirlos y colocarlos en el local.' },
    { q: '¿Qué medios de pago acepta?',
      a: 'Los pagos se procesan con Mercado Pago (Checkout Pro), que admite tarjetas de crédito, débito y dinero en cuenta.' },
  ];

  toggle(i: number): void {
    this.abierta.set(this.abierta() === i ? null : i);
  }
}

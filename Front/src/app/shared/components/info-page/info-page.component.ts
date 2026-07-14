import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Layout compartido de las páginas informativas/legales (términos, privacidad, FAQ). */
@Component({
  selector: 'app-info-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './info-page.component.html'
})
export class InfoPageComponent {
  titulo = input.required<string>();
  eyebrow = input('Información');
  actualizado = input<string | null>(null);
}

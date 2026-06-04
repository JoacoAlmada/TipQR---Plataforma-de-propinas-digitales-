import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-verificar-email',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './verificar-email.component.html',
  styleUrl: './verificar-email.component.css'
})
export class VerificarEmailComponent {
  private readonly route = inject(ActivatedRoute);
  readonly ok = signal(this.route.snapshot.queryParamMap.get('ok') === 'true');
}

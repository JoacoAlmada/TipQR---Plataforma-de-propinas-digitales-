import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  private readonly auth = inject(AuthService);

  readonly logueado = this.auth.isLoggedIn();
  readonly destinoPanel =
    this.auth.getUsuario()?.rol === 'EMPLEADO' ? '/app/empleado' : '/app/dashboard';
}

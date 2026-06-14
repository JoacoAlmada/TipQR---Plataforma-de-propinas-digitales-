import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  private readonly auth = inject(AuthService);
  usuario = this.auth.getUsuario();

  readonly esDueno = this.usuario?.rol === 'DUENO';
  readonly inicio =
    this.usuario?.rol === 'ENCARGADO' ? '/app/encargado'
    : this.usuario?.rol === 'EMPLEADO' ? '/app/empleado'
    : '/app/dashboard';

  logout(): void {
    this.auth.logout();
  }
}

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

  readonly esAdmin = this.usuario?.rol === 'DUENO' || this.usuario?.rol === 'ENCARGADO';
  readonly inicio = this.usuario?.rol === 'EMPLEADO' ? '/app/empleado' : '/app/dashboard';

  logout(): void {
    this.auth.logout();
  }
}

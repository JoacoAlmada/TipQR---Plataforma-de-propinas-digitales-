import { Component, EventEmitter, Output, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionService } from '../../../core/services/notificacion.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly notificacionService = inject(NotificacionService);
  usuario = this.auth.getUsuario();

  noLeidas = signal(0);

  /** Se emite al navegar o cerrar sesión, para que el layout cierre el drawer en mobile. */
  @Output() navegar = new EventEmitter<void>();

  ngOnInit(): void {
    this.notificacionService.noLeidas().subscribe({
      next: (r) => this.noLeidas.set(r.noLeidas)
    });
  }

  readonly esDueno = this.usuario?.rol === 'DUENO';
  readonly inicio =
    this.usuario?.rol === 'ENCARGADO' ? '/app/encargado'
    : this.usuario?.rol === 'EMPLEADO' ? '/app/empleado'
    : '/app/dashboard';

  logout(): void {
    this.auth.logout();
  }
}

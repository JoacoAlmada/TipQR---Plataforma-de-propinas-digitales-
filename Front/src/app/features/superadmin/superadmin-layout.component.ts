import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-superadmin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './superadmin-layout.component.html'
})
export class SuperadminLayoutComponent {
  private readonly auth = inject(AuthService);
  usuario = this.auth.getUsuario();

  logout(): void {
    this.auth.logout();
  }
}

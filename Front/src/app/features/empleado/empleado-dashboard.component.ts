import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-empleado-dashboard',
  standalone: true,
  templateUrl: './empleado-dashboard.component.html',
  styleUrl: './empleado-dashboard.component.css'
})
export class EmpleadoDashboardComponent {
  usuario = inject(AuthService).getUsuario();
}

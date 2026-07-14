import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { QrService } from '../../core/services/qr.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { Qr } from '../../core/models/qr.model';
import { HistorialPropinas } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-empleado-dashboard',
  standalone: true,
  templateUrl: './empleado-dashboard.component.html',
  styleUrl: './empleado-dashboard.component.css'
})
export class EmpleadoDashboardComponent implements OnInit {
  private readonly qrService = inject(QrService);
  private readonly dashboardService = inject(DashboardService);

  usuario = inject(AuthService).getUsuario();

  qr = signal<Qr | null>(null);
  qrImagen = signal<string | null>(null);
  qrError = signal('');

  historial = signal<HistorialPropinas | null>(null);
  cargandoHistorial = signal(true);

  ngOnInit(): void {
    this.qrService.miQr().subscribe({
      next: (q) => this.qr.set(q),
      error: () => this.qrError.set('No se pudo cargar tu código QR.')
    });
    this.qrService.miQrImagen().subscribe({
      next: (blob) => this.qrImagen.set(URL.createObjectURL(blob))
    });
    this.dashboardService.historialEmpleado().subscribe({
      next: (h) => { this.historial.set(h); this.cargandoHistorial.set(false); },
      error: () => this.cargandoHistorial.set(false)
    });
  }

  descargar(): void {
    this.qrService.miQrImagen().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'mi-qr-tipqr.png';
        a.click();
        URL.revokeObjectURL(url);
      }
    });
  }
}

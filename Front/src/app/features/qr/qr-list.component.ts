import { Component, OnInit, inject, signal } from '@angular/core';
import { QrService } from '../../core/services/qr.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { Qr } from '../../core/models/qr.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-qr-list',
  standalone: true,
  imports: [],
  templateUrl: './qr-list.component.html'
})
export class QrListComponent implements OnInit {
  private readonly qrService = inject(QrService);
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  qrs = signal<Qr[]>([]);
  sucursales = signal<Sucursal[]>([]);
  filtroSucursal = signal<number | null>(null);
  /** Object URLs de las imágenes PNG, por id de QR. */
  imagenes = signal<Record<number, string>>({});
  loading = signal(true);
  errorMsg = signal('');

  readonly esDueno = this.auth.hasRole('DUENO');

  ngOnInit(): void {
    this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.liberarImagenes();
    this.qrService.listar(this.filtroSucursal()).subscribe({
      next: (data) => {
        this.qrs.set(data);
        this.loading.set(false);
        data.forEach((q) => this.cargarImagen(q.id));
      },
      error: () => { this.errorMsg.set('No se pudieron cargar los códigos QR'); this.loading.set(false); }
    });
  }

  onFiltro(value: string): void {
    this.filtroSucursal.set(value ? Number(value) : null);
    this.cargar();
  }

  private cargarImagen(id: number): void {
    this.qrService.imagenBlob(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.imagenes.update((m) => ({ ...m, [id]: url }));
      }
    });
  }

  descargar(q: Qr): void {
    this.qrService.imagenBlob(q.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `qr-${(q.destinoNombre || q.codigo).replace(/\s+/g, '-')}.png`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.errorMsg.set('No se pudo descargar la imagen')
    });
  }

  regenerar(q: Qr): void {
    this.errorMsg.set('');
    this.qrService.regenerar(q.id).subscribe({
      next: (act) => {
        this.qrs.update((list) => list.map((x) => x.id === act.id ? act : x));
        this.cargarImagen(act.id);
      },
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo regenerar el QR')
    });
  }

  iconoTipo(tipo: string): string {
    return tipo === 'MESA' ? '🍽️' : tipo === 'EMPLEADO' ? '👤' : tipo === 'GRUPO' ? '👥' : '🏬';
  }

  private liberarImagenes(): void {
    const actuales = this.imagenes();
    Object.values(actuales).forEach((url) => URL.revokeObjectURL(url));
    this.imagenes.set({});
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { SuperadminService, EmpresaValidacion } from '../../core/services/superadmin.service';

@Component({
  selector: 'app-empresa-detalle',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './empresa-detalle.component.html'
})
export class EmpresaDetalleComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly superadmin = inject(SuperadminService);
  private readonly sanitizer = inject(DomSanitizer);

  private id!: number;
  private previewRaw: string | null = null;

  empresa = signal<EmpresaValidacion | null>(null);
  loading = signal(true);
  errorMsg = signal('');
  procesando = signal(false);

  previewUrl = signal<SafeResourceUrl | null>(null);
  cargandoDoc = signal(false);

  mostrarRechazo = signal(false);
  motivoRechazo = signal('');

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.superadmin.empresa(this.id).subscribe({
      next: (e) => { this.empresa.set(e); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudo cargar la empresa'); this.loading.set(false); }
    });
  }

  // ── Visor de la constancia (PDF) ──
  verConstancia(): void {
    this.cargandoDoc.set(true);
    this.superadmin.constanciaBlob(this.id).subscribe({
      next: (blob) => {
        this.cargandoDoc.set(false);
        if (this.previewRaw) URL.revokeObjectURL(this.previewRaw);
        this.previewRaw = URL.createObjectURL(blob);
        this.previewUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.previewRaw));
      },
      error: () => { this.cargandoDoc.set(false); this.errorMsg.set('No se pudo abrir la constancia'); }
    });
  }

  cerrarPreview(): void {
    if (this.previewRaw) { URL.revokeObjectURL(this.previewRaw); this.previewRaw = null; }
    this.previewUrl.set(null);
  }

  // ── Acciones ──
  aprobar(): void {
    this.procesando.set(true);
    this.superadmin.aprobarEmpresa(this.id).subscribe({
      next: () => this.router.navigate(['/superadmin/empresas']),
      error: () => { this.procesando.set(false); this.errorMsg.set('No se pudo aprobar'); }
    });
  }

  confirmarRechazo(): void {
    this.procesando.set(true);
    this.superadmin.rechazarEmpresa(this.id, this.motivoRechazo()).subscribe({
      next: () => this.router.navigate(['/superadmin/empresas']),
      error: () => { this.procesando.set(false); this.errorMsg.set('No se pudo rechazar'); }
    });
  }
}

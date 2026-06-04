import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { SuperadminService, SolicitudDetalle, DocumentoMeta } from '../../core/services/superadmin.service';

interface Preview {
  nombre: string;
  esImagen: boolean;
  raw: string;
  safe: SafeUrl;
}

@Component({
  selector: 'app-solicitud-detalle',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './solicitud-detalle.component.html'
})
export class SolicitudDetalleComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly superadmin = inject(SuperadminService);
  private readonly sanitizer = inject(DomSanitizer);

  private id!: number;

  solicitud = signal<SolicitudDetalle | null>(null);
  loading = signal(true);
  errorMsg = signal('');
  procesando = signal(false);

  preview = signal<Preview | null>(null);
  cargandoDoc = signal<number | null>(null);

  mostrarRechazo = signal(false);
  motivoRechazo = signal('');
  mostrarAprobacion = signal(false);

  readonly etiquetasDoc: Record<string, string> = {
    DNI_FRENTE: 'DNI — Frente',
    DNI_DORSO: 'DNI — Dorso',
    SELFIE: 'Selfie con DNI',
    CONSTANCIA_AFIP: 'Constancia de inscripción AFIP'
  };

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.superadmin.detalle(this.id).subscribe({
      next: (d) => { this.solicitud.set(d); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudo cargar la solicitud'); this.loading.set(false); }
    });
  }

  etiqueta(tipo: string): string {
    return this.etiquetasDoc[tipo] ?? tipo;
  }

  // ── Visor de documentos ──
  verDocumento(doc: DocumentoMeta): void {
    this.cargandoDoc.set(doc.id);
    this.superadmin.documentoBlob(doc.id).subscribe({
      next: (blob) => {
        this.cargandoDoc.set(null);
        const url = URL.createObjectURL(blob);
        const esImagen = doc.contentType.startsWith('image/');
        this.preview.set({
          nombre: doc.nombreArchivo,
          esImagen,
          raw: url,
          safe: esImagen
            ? this.sanitizer.bypassSecurityTrustUrl(url)
            : this.sanitizer.bypassSecurityTrustResourceUrl(url)
        });
      },
      error: () => { this.cargandoDoc.set(null); this.errorMsg.set('No se pudo abrir el documento'); }
    });
  }

  cerrarPreview(): void {
    const p = this.preview();
    if (p) URL.revokeObjectURL(p.raw);
    this.preview.set(null);
  }

  // ── Acciones ──
  confirmarAprobacion(): void {
    this.procesando.set(true);
    this.superadmin.aprobar(this.id).subscribe({
      next: () => this.router.navigate(['/superadmin/solicitudes']),
      error: () => { this.procesando.set(false); this.errorMsg.set('No se pudo aprobar'); }
    });
  }

  confirmarRechazo(): void {
    this.procesando.set(true);
    this.superadmin.rechazar(this.id, this.motivoRechazo()).subscribe({
      next: () => this.router.navigate(['/superadmin/solicitudes']),
      error: () => { this.procesando.set(false); this.errorMsg.set('No se pudo rechazar'); }
    });
  }
}

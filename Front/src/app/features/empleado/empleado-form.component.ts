import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NgTemplateOutlet } from '@angular/common';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { EmpleadoService } from '../../core/services/empleado.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { EmpleadoRequest } from '../../core/models/empleado.model';
import { MiDocumento, TipoDocumento } from '../../core/models/empresa.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet],
  templateUrl: './empleado-form.component.html'
})
export class EmpleadoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);

  private empleadoId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');
  sucursales = signal<Sucursal[]>([]);

  // Tras crear: contraseña temporal a mostrar
  creado = signal<{ nombre: string; password: string } | null>(null);

  // Documentación del empleado (DNI frente/dorso, selfie)
  readonly tiposDoc: { tipo: TipoDocumento; label: string }[] = [
    { tipo: 'DNI_FRENTE', label: 'DNI (frente)' },
    { tipo: 'DNI_DORSO', label: 'DNI (dorso)' },
    { tipo: 'SELFIE', label: 'Selfie' }
  ];
  documentos = signal<MiDocumento[]>([]);
  previews = signal<Record<string, SafeUrl>>({});
  subiendoDoc = signal<string | null>(null);
  docMsg = signal('');

  form = this.fb.group({
    nombreVisible: ['', [Validators.required, Validators.maxLength(80)]],
    apellido:      ['', [Validators.required, Validators.maxLength(80)]],
    email:         ['', [Validators.required, Validators.email]],
    puesto:        ['', Validators.maxLength(60)],
    sucursalId:    [null as number | null, Validators.required]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.empleadoId = Number(idParam);
      this.esEdicion.set(true);
      this.loading.set(true);
      this.empleadoService.obtener(this.empleadoId).subscribe({
        next: (e) => {
          this.form.patchValue({
            nombreVisible: e.nombreVisible, apellido: e.apellido, email: e.email,
            puesto: e.puesto ?? '', sucursalId: e.sucursalId
          });
          this.loading.set(false);
        },
        error: () => { this.errorMsg.set('No se pudo cargar el empleado'); this.loading.set(false); }
      });
      this.cargarDocumentos(this.empleadoId);
    }
  }

  // ── Documentación ──

  private cargarDocumentos(id: number): void {
    this.empleadoService.documentos(id).subscribe({
      next: (docs) => {
        this.documentos.set(docs);
        docs.filter(d => d.cargado).forEach(d => this.cargarPreview(id, d));
      }
    });
  }

  private cargarPreview(id: number, d: MiDocumento): void {
    this.empleadoService.documentoBlob(id, d.tipo).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.previews.update(m => ({ ...m, [d.tipo]: this.sanitizer.bypassSecurityTrustUrl(url) }));
      }
    });
  }

  subirDoc(tipo: TipoDocumento, ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || this.empleadoId == null) return;
    this.subiendoDoc.set(tipo);
    this.docMsg.set('');
    this.empleadoService.subirDocumento(this.empleadoId, tipo, file).subscribe({
      next: (doc) => {
        this.subiendoDoc.set(null);
        this.documentos.update(list => list.map(d => d.tipo === tipo ? doc : d));
        this.cargarPreview(this.empleadoId!, doc);
        this.docMsg.set('Foto actualizada.');
        input.value = '';
      },
      error: (err) => {
        this.subiendoDoc.set(null);
        this.docMsg.set(err?.error?.error ?? 'No se pudo subir la foto.');
        input.value = '';
      }
    });
  }

  docLabel(tipo: string): string {
    return this.tiposDoc.find(t => t.tipo === tipo)?.label ?? tipo;
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form.value;
    const request: EmpleadoRequest = {
      nombreVisible: v.nombreVisible!,
      apellido: v.apellido!,
      email: v.email!,
      puesto: v.puesto || null,
      sucursalId: Number(v.sucursalId)
    };

    if (this.esEdicion()) {
      this.empleadoService.actualizar(this.empleadoId!, request).subscribe({
        next: () => this.router.navigate(['/app/empleados']),
        error: (err) => { this.loading.set(false); this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar'); }
      });
    } else {
      this.empleadoService.crear(request).subscribe({
        next: (emp) => {
          this.loading.set(false);
          this.empleadoId = emp.id;
          this.creado.set({ nombre: emp.nombreVisible, password: emp.passwordTemporal ?? '' });
          this.cargarDocumentos(emp.id); // slots vacíos para subir las fotos
        },
        error: (err) => { this.loading.set(false); this.errorMsg.set(err?.error?.error ?? 'No se pudo crear el empleado'); }
      });
    }
  }
}

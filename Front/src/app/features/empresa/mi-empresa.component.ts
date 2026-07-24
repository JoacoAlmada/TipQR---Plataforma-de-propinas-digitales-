import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { EmpresaService } from '../../core/services/empresa.service';
import { AuthService } from '../../core/services/auth.service';
import { Empresa, EmpresaRequest, MiDocumento, TipoDocumento } from '../../core/models/empresa.model';

@Component({
  selector: 'app-mi-empresa',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './mi-empresa.component.html',
  styleUrl: './mi-empresa.component.css'
})
export class MiEmpresaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empresaService = inject(EmpresaService);
  private readonly auth = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);

  private empresaId: number | null = null;

  // Documentación del dueño
  documentos = signal<MiDocumento[]>([]);
  previews = signal<Record<string, SafeUrl>>({});
  pdfUrls = signal<Record<string, string>>({});
  subiendoDoc = signal<string | null>(null);
  docMsg = signal('');

  empresa = signal<Empresa | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  errorMsg = signal('');
  okMsg = signal('');

  // Multi-empresa
  misEmpresas = signal<Empresa[]>([]);
  cambiandoA = signal<number | null>(null);

  // Alta de empresa nueva (modal con stepper: datos → constancia → validación)
  mostrarNueva = signal(false);
  nuevaPaso = signal(1);
  creando = signal(false);
  nuevaMsg = signal('');
  empresaNuevaId = signal<number | null>(null);
  subiendoConstancia = signal(false);
  constanciaLista = signal(false);
  // Corrección/reenvío de una empresa rechazada (reusa el mismo modal)
  modoCorreccion = signal(false);
  private corrigiendo: Empresa | null = null;

  readonly esDueno = this.auth.hasRole('DUENO');

  nuevaForm = this.fb.group({
    nombre:         ['', [Validators.required, Validators.maxLength(120)]],
    nombreFantasia: ['', Validators.maxLength(120)],
    rubro:          ['', Validators.required],
    cuit:           ['', Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    provincia:      ['', Validators.required],
    calle:          ['', Validators.required],
    numeracion:     ['', Validators.required]
  });

  get nf() { return this.nuevaForm.controls; }

  form = this.fb.group({
    nombre:         [{ value: '', disabled: !this.esDueno }, [Validators.required, Validators.maxLength(120)]],
    nombreFantasia: [{ value: '', disabled: !this.esDueno }, Validators.maxLength(120)],
    rubro:          [{ value: '', disabled: !this.esDueno }, Validators.maxLength(80)],
    cuit:           [{ value: '', disabled: !this.esDueno }, Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    provincia:      [{ value: '', disabled: !this.esDueno }, Validators.maxLength(80)],
    calle:          [{ value: '', disabled: !this.esDueno }, Validators.maxLength(120)],
    numeracion:     [{ value: '', disabled: !this.esDueno }, Validators.maxLength(20)],
    emailContacto:  [{ value: '', disabled: !this.esDueno }, Validators.email],
    telefono:       [{ value: '', disabled: !this.esDueno }, Validators.maxLength(30)]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    this.empresaService.miEmpresa().subscribe({
      next: (empresa) => {
        this.empresaId = empresa.id;
        this.empresa.set(empresa);
        this.form.patchValue({
          nombre: empresa.nombre,
          nombreFantasia: empresa.nombreFantasia ?? '',
          rubro: empresa.rubro ?? '',
          cuit: empresa.cuit ?? '',
          provincia: empresa.provincia ?? '',
          calle: empresa.calle ?? '',
          numeracion: empresa.numeracion ?? '',
          emailContacto: empresa.emailContacto ?? '',
          telefono: empresa.telefono ?? ''
        });
        this.cargando.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudo cargar tu empresa');
        this.cargando.set(false);
      }
    });
    if (this.esDueno) {
      this.cargarMisEmpresas();
      this.cargarDocumentos();
    }
  }

  // ── Documentación ──

  cargarDocumentos(): void {
    this.empresaService.documentos().subscribe({
      next: (docs) => {
        this.documentos.set(docs);
        docs.filter(d => d.cargado).forEach(d => this.cargarPreview(d));
      }
    });
  }

  private cargarPreview(d: MiDocumento): void {
    this.empresaService.documentoBlob(d.tipo).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        if (d.esPdf) {
          this.pdfUrls.update(m => ({ ...m, [d.tipo]: url }));
        } else {
          this.previews.update(m => ({ ...m, [d.tipo]: this.sanitizer.bypassSecurityTrustUrl(url) }));
        }
      }
    });
  }

  subirDoc(tipo: TipoDocumento, ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.subiendoDoc.set(tipo);
    this.docMsg.set('');
    this.empresaService.subirDocumento(tipo, file).subscribe({
      next: (doc) => {
        this.subiendoDoc.set(null);
        this.documentos.update(list => list.map(d => d.tipo === tipo ? doc : d));
        this.cargarPreview(doc);
        this.docMsg.set('Documento actualizado.');
        input.value = '';
      },
      error: (err) => {
        this.subiendoDoc.set(null);
        this.docMsg.set(err?.error?.error ?? 'No se pudo subir el documento.');
        input.value = '';
      }
    });
  }

  abrirPdf(tipo: string): void {
    const u = this.pdfUrls()[tipo];
    if (u) window.open(u, '_blank');
  }

  docLabel(tipo: string): string {
    return tipo === 'DNI_FRENTE' ? 'DNI (frente)'
      : tipo === 'DNI_DORSO' ? 'DNI (dorso)'
      : tipo === 'SELFIE' ? 'Selfie'
      : 'Constancia de AFIP';
  }

  cargarMisEmpresas(): void {
    this.empresaService.misEmpresas().subscribe({
      next: (e) => this.misEmpresas.set(e)
    });
  }

  /** Cambia la empresa activa y recarga para que toda la app refleje la nueva. */
  activarEmpresa(e: Empresa): void {
    if (e.activa) return;
    this.cambiandoA.set(e.id);
    this.empresaService.activar(e.id).subscribe({
      next: () => window.location.reload(),
      error: (err) => { this.cambiandoA.set(null); this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar de empresa'); }
    });
  }

  // ── Alta de empresa nueva (stepper en modal) ──

  abrirNueva(): void {
    this.modoCorreccion.set(false);
    this.corrigiendo = null;
    this.nuevaForm.reset({ nombre: '', nombreFantasia: '', rubro: '', cuit: '', provincia: '', calle: '', numeracion: '' });
    this.nuevaPaso.set(1);
    this.empresaNuevaId.set(null);
    this.constanciaLista.set(false);
    this.nuevaMsg.set('');
    this.mostrarNueva.set(true);
  }

  /** Abre el mismo modal en modo corrección, precargado con los datos de la empresa rechazada. */
  abrirCorregir(e: Empresa): void {
    this.modoCorreccion.set(true);
    this.corrigiendo = e;
    this.nuevaForm.reset({
      nombre: e.nombre, nombreFantasia: e.nombreFantasia ?? '', rubro: e.rubro ?? '',
      cuit: e.cuit ?? '', provincia: e.provincia ?? '', calle: e.calle ?? '', numeracion: e.numeracion ?? ''
    });
    this.nuevaPaso.set(1);
    this.empresaNuevaId.set(e.id);
    this.constanciaLista.set(!!e.constanciaCargada);
    this.nuevaMsg.set('');
    this.mostrarNueva.set(true);
  }

  cerrarNueva(recargar = false): void {
    this.mostrarNueva.set(false);
    if (recargar) this.cargarMisEmpresas();
  }

  /** Paso 1 → crea (alta) o guarda los datos corregidos (corrección) y avanza a la constancia. */
  crearEmpresa(): void {
    if (this.nuevaForm.invalid) { this.nuevaForm.markAllAsTouched(); return; }
    this.nuevaMsg.set('');
    this.creando.set(true);
    const v = this.nuevaForm.getRawValue();
    const request: EmpresaRequest = {
      nombre: v.nombre!,
      nombreFantasia: v.nombreFantasia || null,
      rubro: v.rubro || null,
      cuit: v.cuit || null,
      provincia: v.provincia || null,
      calle: v.calle || null,
      numeracion: v.numeracion || null,
      emailContacto: this.corrigiendo?.emailContacto ?? null,
      telefono: this.corrigiendo?.telefono ?? null
    };

    const obs = this.modoCorreccion() && this.empresaNuevaId() != null
      ? this.empresaService.actualizar(this.empresaNuevaId()!, request)
      : this.empresaService.crear(request);

    obs.subscribe({
      next: (emp) => { this.creando.set(false); this.empresaNuevaId.set(emp.id); this.nuevaPaso.set(2); },
      error: (err) => {
        this.creando.set(false);
        this.nuevaMsg.set(err?.error?.error ?? 'No se pudieron guardar los datos');
      }
    });
  }

  /** Paso 2 → sube la constancia de AFIP a la empresa recién creada. */
  subirConstanciaNueva(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    const id = this.empresaNuevaId();
    if (!file || id == null) return;
    this.nuevaMsg.set('');
    this.subiendoConstancia.set(true);
    this.empresaService.subirConstancia(id, file).subscribe({
      next: () => { this.subiendoConstancia.set(false); this.constanciaLista.set(true); },
      error: (err) => {
        this.subiendoConstancia.set(false);
        input.value = '';
        this.nuevaMsg.set(err?.error?.error ?? 'No se pudo subir la constancia');
      }
    });
  }

  /** Paso 2 → 3. En alta ya quedó pendiente; en corrección, reenvía a validación. */
  finalizarNueva(): void {
    if (this.modoCorreccion() && this.empresaNuevaId() != null) {
      this.nuevaMsg.set('');
      this.creando.set(true);
      this.empresaService.reenviar(this.empresaNuevaId()!).subscribe({
        next: () => { this.creando.set(false); this.nuevaPaso.set(3); },
        error: (err) => { this.creando.set(false); this.nuevaMsg.set(err?.error?.error ?? 'No se pudo reenviar'); }
      });
    } else {
      this.nuevaPaso.set(3);
    }
  }

  onSubmit(): void {
    if (this.form.invalid || this.empresaId === null) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.errorMsg.set('');
    this.okMsg.set('');

    const v = this.form.getRawValue();
    const request: EmpresaRequest = {
      nombre: v.nombre!,
      nombreFantasia: v.nombreFantasia || null,
      rubro: v.rubro || null,
      cuit: v.cuit || null,
      provincia: v.provincia || null,
      calle: v.calle || null,
      numeracion: v.numeracion || null,
      emailContacto: v.emailContacto || null,
      telefono: v.telefono || null
    };

    this.empresaService.actualizar(this.empresaId, request).subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.guardando.set(false);
        this.okMsg.set('Cambios guardados');
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudieron guardar los cambios');
      }
    });
  }

  toggleEstado(): void {
    const actual = this.empresa();
    if (!actual) return;
    this.empresaService.cambiarEstado(actual.id, !actual.estado).subscribe({
      next: (empresa) => this.empresa.set(empresa),
      error: () => this.errorMsg.set('No se pudo cambiar el estado')
    });
  }
}

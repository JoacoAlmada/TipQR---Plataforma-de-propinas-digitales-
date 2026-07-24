import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { RegistroService, TipoDocumento } from '../../../core/services/registro.service';

@Component({
  selector: 'app-registro-correccion',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro-correccion.component.html'
})
export class RegistroCorreccionComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly registroService = inject(RegistroService);
  private readonly route = inject(ActivatedRoute);

  private token = '';

  cargando = signal(true);
  errorCarga = signal('');
  estadoCuenta = signal('');
  motivoRechazo = signal<string | null>(null);
  emailUsuario = signal('');

  docsSubidos = signal<TipoDocumento[]>([]);
  subiendoDoc = signal<string | null>(null);
  enviando = signal(false);
  enviado = signal(false);
  errorMsg = signal('');

  readonly tiposDoc: { tipo: TipoDocumento; label: string; accept: string; hint: string }[] = [
    { tipo: 'DNI_FRENTE', label: 'DNI — Frente', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'DNI_DORSO', label: 'DNI — Dorso', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'SELFIE', label: 'Selfie con tu DNI', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'CONSTANCIA_AFIP', label: 'Constancia de AFIP', accept: 'application/pdf', hint: 'Archivo PDF' }
  ];

  form1 = this.fb.group({
    nombre:   ['', [Validators.required, Validators.maxLength(60)]],
    apellido: ['', [Validators.required, Validators.maxLength(60)]],
    telefono: ['', [Validators.required, Validators.maxLength(30)]],
    cuit:     ['', [Validators.required, Validators.pattern(/^\d{2}-?\d{8}-?\d{1}$/)]],
    dni:      ['', [Validators.required, Validators.pattern(/^\d{7,9}$/)]]
  });

  form2 = this.fb.group({
    nombreEmpresa:  ['', [Validators.required, Validators.maxLength(120)]],
    nombreFantasia: ['', Validators.maxLength(120)],
    provincia:      ['', Validators.required],
    calle:          ['', Validators.required],
    numeracion:     ['', Validators.required],
    cuit:           ['', Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    rubro:          ['', Validators.required]
  });

  get f1() { return this.form1.controls; }
  get f2() { return this.form2.controls; }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.cargando.set(false);
      this.errorCarga.set('Link inválido: falta el token de la solicitud.');
      return;
    }
    this.registroService.resumen(this.token).subscribe({
      next: (r) => {
        this.estadoCuenta.set(r.estadoCuenta);
        this.motivoRechazo.set(r.motivoRechazo);
        this.emailUsuario.set(r.email);
        this.docsSubidos.set(r.documentosCargados ?? []);
        this.form1.patchValue({
          nombre: r.nombre, apellido: r.apellido, telefono: r.telefono ?? '',
          cuit: r.cuit ?? '', dni: r.dni ?? ''
        });
        this.form2.patchValue({
          nombreEmpresa: r.nombreEmpresa ?? '', nombreFantasia: r.nombreFantasia ?? '',
          provincia: r.provincia ?? '', calle: r.calle ?? '', numeracion: r.numeracion ?? '',
          cuit: r.empresaCuit ?? '', rubro: r.rubro ?? ''
        });
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.errorCarga.set('No encontramos la solicitud. El link puede haber vencido.');
      }
    });
  }

  estaSubido(tipo: TipoDocumento): boolean {
    return this.docsSubidos().includes(tipo);
  }

  get todosLosDocs(): boolean {
    return this.docsSubidos().length === this.tiposDoc.length;
  }

  onArchivo(tipo: TipoDocumento, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.subiendoDoc.set(tipo);
    this.errorMsg.set('');
    this.registroService.subirDocumento(this.token, tipo, file).subscribe({
      next: () => {
        this.subiendoDoc.set(null);
        if (!this.docsSubidos().includes(tipo)) {
          this.docsSubidos.update(list => [...list, tipo]);
        }
      },
      error: (err) => {
        this.subiendoDoc.set(null);
        input.value = '';
        this.errorMsg.set(err?.error?.error ?? 'No se pudo subir el archivo.');
      }
    });
  }

  reenviar(): void {
    if (this.form1.invalid || this.form2.invalid) {
      this.form1.markAllAsTouched();
      this.form2.markAllAsTouched();
      this.errorMsg.set('Revisá los datos marcados en rojo.');
      return;
    }
    if (!this.todosLosDocs) {
      this.errorMsg.set('Tenés que adjuntar los cuatro documentos.');
      return;
    }

    this.errorMsg.set('');
    this.enviando.set(true);
    const p = this.form1.value;
    const e = this.form2.value;

    this.registroService.actualizarDatos(this.token, {
      nombre: p.nombre!, apellido: p.apellido!, telefono: p.telefono!, cuit: p.cuit!, dni: p.dni!
    }).pipe(
      switchMap(() => this.registroService.paso2(this.token, {
        nombreEmpresa: e.nombreEmpresa!, nombreFantasia: e.nombreFantasia || null,
        provincia: e.provincia!, calle: e.calle!, numeracion: e.numeracion!,
        cuit: e.cuit || null, rubro: e.rubro!
      })),
      switchMap(() => this.registroService.finalizar(this.token))
    ).subscribe({
      next: () => { this.enviando.set(false); this.enviado.set(true); },
      error: (err) => {
        this.enviando.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo reenviar la solicitud.');
      }
    });
  }
}

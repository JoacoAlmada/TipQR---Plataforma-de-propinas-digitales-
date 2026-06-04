import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { EmpresaService } from '../../core/services/empresa.service';
import { AuthService } from '../../core/services/auth.service';
import { Empresa, EmpresaRequest } from '../../core/models/empresa.model';

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

  private empresaId: number | null = null;

  empresa = signal<Empresa | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  errorMsg = signal('');
  okMsg = signal('');

  readonly esDueno = this.auth.hasRole('DUENO');

  form = this.fb.group({
    nombre:        [{ value: '', disabled: !this.esDueno }, [Validators.required, Validators.maxLength(120)]],
    rubro:         [{ value: '', disabled: !this.esDueno }, Validators.maxLength(80)],
    cuit:          [{ value: '', disabled: !this.esDueno }, Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    emailContacto: [{ value: '', disabled: !this.esDueno }, Validators.email],
    telefono:      [{ value: '', disabled: !this.esDueno }, Validators.maxLength(30)]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    this.empresaService.miEmpresa().subscribe({
      next: (empresa) => {
        this.empresaId = empresa.id;
        this.empresa.set(empresa);
        this.form.patchValue({
          nombre: empresa.nombre,
          rubro: empresa.rubro ?? '',
          cuit: empresa.cuit ?? '',
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
      rubro: v.rubro || null,
      cuit: v.cuit || null,
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

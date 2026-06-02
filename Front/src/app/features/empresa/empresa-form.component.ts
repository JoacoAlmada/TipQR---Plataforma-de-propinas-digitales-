import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmpresaService } from '../../core/services/empresa.service';
import { EmpresaRequest } from '../../core/models/empresa.model';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './empresa-form.component.html',
  styleUrl: './empresa-form.component.css'
})
export class EmpresaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empresaService = inject(EmpresaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private empresaId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');

  form = this.fb.group({
    nombre:        ['', [Validators.required, Validators.maxLength(120)]],
    rubro:         ['', Validators.maxLength(80)],
    cuit:          ['', Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    emailContacto: ['', Validators.email],
    telefono:      ['', Validators.maxLength(30)]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.empresaId = Number(idParam);
      this.esEdicion.set(true);
      this.cargarEmpresa(this.empresaId);
    }
  }

  private cargarEmpresa(id: number): void {
    this.loading.set(true);
    this.empresaService.obtener(id).subscribe({
      next: (empresa) => {
        this.form.patchValue({
          nombre: empresa.nombre,
          rubro: empresa.rubro ?? '',
          cuit: empresa.cuit ?? '',
          emailContacto: empresa.emailContacto ?? '',
          telefono: empresa.telefono ?? ''
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudo cargar la empresa');
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    const v = this.form.value;
    const request: EmpresaRequest = {
      nombre: v.nombre!,
      rubro: v.rubro || null,
      cuit: v.cuit || null,
      emailContacto: v.emailContacto || null,
      telefono: v.telefono || null
    };

    const peticion = this.esEdicion()
      ? this.empresaService.actualizar(this.empresaId!, request)
      : this.empresaService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/empresas']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar la empresa');
      }
    });
  }
}

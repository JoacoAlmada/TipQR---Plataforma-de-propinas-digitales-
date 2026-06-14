import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SucursalService } from '../../core/services/sucursal.service';
import { SucursalRequest } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-sucursal-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './sucursal-form.component.html'
})
export class SucursalFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private sucursalId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');

  form = this.fb.group({
    nombre:    ['', [Validators.required, Validators.maxLength(120)]],
    direccion: ['', Validators.maxLength(160)],
    telefono:  ['', Validators.maxLength(30)]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.sucursalId = Number(idParam);
      this.esEdicion.set(true);
      this.loading.set(true);
      this.sucursalService.obtener(this.sucursalId).subscribe({
        next: (s) => {
          this.form.patchValue({ nombre: s.nombre, direccion: s.direccion ?? '', telefono: s.telefono ?? '' });
          this.loading.set(false);
        },
        error: () => { this.errorMsg.set('No se pudo cargar la sucursal'); this.loading.set(false); }
      });
    }
  }

  soloNumeros(): void {
    const c = this.form.controls.telefono;
    const limpio = (c.value ?? '').replace(/\D/g, '');
    if (limpio !== c.value) c.setValue(limpio, { emitEvent: false });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form.value;
    const request: SucursalRequest = {
      nombre: v.nombre!,
      direccion: v.direccion || null,
      telefono: v.telefono || null
    };

    const peticion = this.esEdicion()
      ? this.sucursalService.actualizar(this.sucursalId!, request)
      : this.sucursalService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/app/sucursales']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar la sucursal');
      }
    });
  }
}

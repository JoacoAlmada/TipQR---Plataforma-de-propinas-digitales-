import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmpleadoService } from '../../core/services/empleado.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { EmpleadoRequest } from '../../core/models/empleado.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './empleado-form.component.html'
})
export class EmpleadoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private empleadoId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');
  sucursales = signal<Sucursal[]>([]);

  // Tras crear: contraseña temporal a mostrar
  creado = signal<{ nombre: string; password: string } | null>(null);

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
    }
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
          this.creado.set({ nombre: emp.nombreVisible, password: emp.passwordTemporal ?? '' });
        },
        error: (err) => { this.loading.set(false); this.errorMsg.set(err?.error?.error ?? 'No se pudo crear el empleado'); }
      });
    }
  }
}

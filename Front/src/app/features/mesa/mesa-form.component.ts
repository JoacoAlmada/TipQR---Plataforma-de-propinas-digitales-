import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MesaService } from '../../core/services/mesa.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { MesaRequest } from '../../core/models/mesa.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-mesa-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mesa-form.component.html'
})
export class MesaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly mesaService = inject(MesaService);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private mesaId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');
  sucursales = signal<Sucursal[]>([]);

  form = this.fb.group({
    numero:     [null as number | null, [Validators.required, Validators.min(1)]],
    descripcion:['', Validators.maxLength(120)],
    sucursalId: [null as number | null, Validators.required]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.mesaId = Number(idParam);
      this.esEdicion.set(true);
      this.loading.set(true);
      this.mesaService.obtener(this.mesaId).subscribe({
        next: (m) => {
          this.form.patchValue({ numero: m.numero, descripcion: m.descripcion ?? '', sucursalId: m.sucursalId });
          this.loading.set(false);
        },
        error: () => { this.errorMsg.set('No se pudo cargar la mesa'); this.loading.set(false); }
      });
    }
  }

  soloNumeros(): void {
    const c = this.form.controls.numero;
    const limpio = String(c.value ?? '').replace(/\D/g, '');
    const val = limpio ? Number(limpio) : null;
    if (val !== c.value) c.setValue(val, { emitEvent: false });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form.value;
    const request: MesaRequest = {
      numero: Number(v.numero),
      descripcion: v.descripcion || null,
      sucursalId: Number(v.sucursalId)
    };

    const peticion = this.esEdicion()
      ? this.mesaService.actualizar(this.mesaId!, request)
      : this.mesaService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/app/mesas']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar la mesa');
      }
    });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GrupoPropinaService } from '../../core/services/grupo-propina.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { GrupoPropinaRequest } from '../../core/models/grupo-propina.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-grupo-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './grupo-form.component.html'
})
export class GrupoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly grupoService = inject(GrupoPropinaService);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private grupoId: number | null = null;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');
  sucursales = signal<Sucursal[]>([]);

  readonly tiposSugeridos = ['Turno', 'Barra', 'Salón', 'Cocina', 'General'];

  form = this.fb.group({
    nombre:      ['', [Validators.required, Validators.maxLength(120)]],
    tipoGrupo:   ['', Validators.maxLength(60)],
    descripcion: ['', Validators.maxLength(160)],
    sucursalId:  [null as number | null, Validators.required]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.grupoId = Number(idParam);
      this.esEdicion.set(true);
      this.loading.set(true);
      this.grupoService.obtener(this.grupoId).subscribe({
        next: (g) => {
          this.form.patchValue({
            nombre: g.nombre, tipoGrupo: g.tipoGrupo ?? '',
            descripcion: g.descripcion ?? '', sucursalId: g.sucursalId
          });
          this.loading.set(false);
        },
        error: () => { this.errorMsg.set('No se pudo cargar el grupo'); this.loading.set(false); }
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form.value;
    const request: GrupoPropinaRequest = {
      nombre: v.nombre!,
      tipoGrupo: v.tipoGrupo || null,
      descripcion: v.descripcion || null,
      sucursalId: Number(v.sucursalId)
    };

    const peticion = this.esEdicion()
      ? this.grupoService.actualizar(this.grupoId!, request)
      : this.grupoService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/app/grupos']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar el grupo');
      }
    });
  }
}

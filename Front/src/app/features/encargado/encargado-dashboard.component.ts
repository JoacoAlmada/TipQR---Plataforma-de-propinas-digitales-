import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { EmpleadoService } from '../../core/services/empleado.service';
import { MesaService } from '../../core/services/mesa.service';
import { GrupoPropinaService } from '../../core/services/grupo-propina.service';
import { Sucursal } from '../../core/models/sucursal.model';
import { Empleado } from '../../core/models/empleado.model';
import { Mesa } from '../../core/models/mesa.model';
import { GrupoPropina } from '../../core/models/grupo-propina.model';

@Component({
  selector: 'app-encargado-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './encargado-dashboard.component.html'
})
export class EncargadoDashboardComponent implements OnInit {
  private readonly empleadoService = inject(EmpleadoService);
  private readonly mesaService = inject(MesaService);
  private readonly grupoService = inject(GrupoPropinaService);

  usuario = inject(AuthService).getUsuario();

  sucursal = signal<Sucursal | null>(null);
  empleados = signal<Empleado[]>([]);
  mesas = signal<Mesa[]>([]);
  grupos = signal<GrupoPropina[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  ngOnInit(): void {
    this.empleadoService.miSucursal().subscribe({
      next: (suc) => {
        this.sucursal.set(suc);
        this.empleadoService.listar(suc.id).subscribe({ next: (e) => this.empleados.set(e) });
        this.mesaService.listar(suc.id).subscribe({ next: (m) => this.mesas.set(m) });
        this.grupoService.listar(suc.id).subscribe({
          next: (g) => { this.grupos.set(g); this.loading.set(false); },
          error: () => this.loading.set(false)
        });
      },
      error: () => {
        this.errorMsg.set('No tenés una sucursal asignada como encargado.');
        this.loading.set(false);
      }
    });
  }
}

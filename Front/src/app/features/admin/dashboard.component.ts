import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { EmpresaService } from '../../core/services/empresa.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);

  usuario = inject(AuthService).getUsuario();
  readonly esDueno = this.usuario?.rol === 'DUENO';

  totalEmpresas = signal<number | null>(null);
  empresasActivas = signal<number | null>(null);

  ngOnInit(): void {
    this.empresaService.listar().subscribe({
      next: (empresas) => {
        this.totalEmpresas.set(empresas.length);
        this.empresasActivas.set(empresas.filter(e => e.estado).length);
      },
      error: () => {
        this.totalEmpresas.set(0);
        this.empresasActivas.set(0);
      }
    });
  }
}

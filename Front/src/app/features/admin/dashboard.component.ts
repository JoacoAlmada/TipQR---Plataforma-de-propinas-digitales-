import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { EmpresaService } from '../../core/services/empresa.service';
import { Empresa } from '../../core/models/empresa.model';

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

  empresa = signal<Empresa | null>(null);
  cargandoEmpresa = signal(true);

  ngOnInit(): void {
    this.empresaService.miEmpresa().subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.cargandoEmpresa.set(false);
      },
      error: () => this.cargandoEmpresa.set(false)
    });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmpresaService } from '../../core/services/empresa.service';
import { AuthService } from '../../core/services/auth.service';
import { Empresa } from '../../core/models/empresa.model';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './empresa-list.component.html',
  styleUrl: './empresa-list.component.css'
})
export class EmpresaListComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);
  private readonly auth = inject(AuthService);

  empresas = signal<Empresa[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  readonly esDueno = this.auth.hasRole('DUENO');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.empresaService.listar().subscribe({
      next: (data) => {
        this.empresas.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar las empresas');
        this.loading.set(false);
      }
    });
  }

  toggleEstado(empresa: Empresa): void {
    this.empresaService.cambiarEstado(empresa.id, !empresa.estado).subscribe({
      next: (actualizada) => {
        this.empresas.update(list =>
          list.map(e => e.id === actualizada.id ? actualizada : e));
      },
      error: () => this.errorMsg.set('No se pudo cambiar el estado de la empresa')
    });
  }
}

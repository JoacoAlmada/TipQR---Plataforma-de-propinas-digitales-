import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  /** Estado del drawer del sidebar en mobile. */
  abierto = signal(false);

  toggle(): void { this.abierto.update(v => !v); }
  cerrar(): void { this.abierto.set(false); }
}

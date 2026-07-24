import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { InfoPageComponent } from '../../shared/components/info-page/info-page.component';

@Component({
  selector: 'app-terminos',
  standalone: true,
  imports: [InfoPageComponent, RouterLink],
  templateUrl: './terminos.component.html'
})
export class TerminosComponent {}

import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegistroRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  loading = signal(false);
  errorMsg = signal('');

  form = this.fb.group({
    nombreEmpresa: ['', [Validators.required, Validators.maxLength(120)]],
    rubro:         ['', Validators.maxLength(80)],
    cuit:          ['', Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    nombre:        ['', [Validators.required, Validators.maxLength(60)]],
    apellido:      ['', [Validators.required, Validators.maxLength(60)]],
    email:         ['', [Validators.required, Validators.email]],
    password:      ['', [Validators.required, Validators.minLength(6)]]
  });

  get f() { return this.form.controls; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    const v = this.form.value;
    const request: RegistroRequest = {
      nombreEmpresa: v.nombreEmpresa!,
      rubro: v.rubro || null,
      cuit: v.cuit || null,
      nombre: v.nombre!,
      apellido: v.apellido!,
      email: v.email!,
      password: v.password!
    };

    this.auth.registrar(request).subscribe({
      next: () => this.router.navigate(['/app/dashboard']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo crear la cuenta. Intentá de nuevo.');
      }
    });
  }
}

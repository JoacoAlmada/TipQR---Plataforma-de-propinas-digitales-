import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  loading = false;
  errorMsg = '';

  get f() { return this.form.controls; }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMsg = '';

    const { email, password } = this.form.value;

    this.auth.login({ email: email!, password: password! }).subscribe({
      next: (res) => {
        if (res.rol === 'SUPERADMIN') {
          this.router.navigate(['/superadmin']);
        } else {
          this.router.navigate(res.rol === 'EMPLEADO' ? ['/app/empleado'] : ['/app/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        // 403 = cuenta pendiente/rechazada → mostramos el mensaje del backend
        this.errorMsg = err?.status === 403 && err?.error?.error
          ? err.error.error
          : 'Email o contraseña incorrectos';
      }
    });
  }
}

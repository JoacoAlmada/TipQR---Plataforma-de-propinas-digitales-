import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { redirectIfLoggedGuard } from './core/guards/redirect-if-logged.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'login',
    canActivate: [redirectIfLoggedGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'registro',
    canActivate: [redirectIfLoggedGuard],
    loadComponent: () =>
      import('./features/auth/registro/registro.component').then(m => m.RegistroComponent)
  },
  {
    path: 'app',
    loadComponent: () =>
      import('./shared/components/layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'empleado',
        loadComponent: () =>
          import('./features/empleado/empleado-dashboard.component').then(m => m.EmpleadoDashboardComponent)
      },
      {
        path: 'empresa',
        loadComponent: () =>
          import('./features/empresa/mi-empresa.component').then(m => m.MiEmpresaComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];

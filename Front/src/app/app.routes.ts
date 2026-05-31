import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { redirectIfLoggedGuard } from './core/guards/redirect-if-logged.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [redirectIfLoggedGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
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
        path: 'empleado/dashboard',
        loadComponent: () =>
          import('./features/empleado/empleado-dashboard.component').then(m => m.EmpleadoDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];

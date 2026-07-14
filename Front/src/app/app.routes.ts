import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { redirectIfLoggedGuard } from './core/guards/redirect-if-logged.guard';
import { superadminGuard } from './core/guards/superadmin.guard';

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
    path: 'verificar-email',
    loadComponent: () =>
      import('./features/auth/verificar-email/verificar-email.component').then(m => m.VerificarEmailComponent)
  },
  {
    path: 'terminos',
    loadComponent: () =>
      import('./features/legal/terminos.component').then(m => m.TerminosComponent)
  },
  {
    path: 'privacidad',
    loadComponent: () =>
      import('./features/legal/privacidad.component').then(m => m.PrivacidadComponent)
  },
  {
    path: 'faq',
    loadComponent: () =>
      import('./features/legal/faq.component').then(m => m.FaqComponent)
  },
  {
    path: 'propina/:codigo',
    loadComponent: () =>
      import('./features/public/propina.component').then(m => m.PropinaPublicaComponent)
  },
  {
    path: 'pago/resultado',
    loadComponent: () =>
      import('./features/public/pago-resultado.component').then(m => m.PagoResultadoComponent)
  },
  {
    path: 'superadmin',
    loadComponent: () =>
      import('./features/superadmin/superadmin-layout.component').then(m => m.SuperadminLayoutComponent),
    canActivate: [superadminGuard],
    children: [
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./features/superadmin/solicitudes-list.component').then(m => m.SolicitudesListComponent)
      },
      {
        path: 'solicitudes/:id',
        loadComponent: () =>
          import('./features/superadmin/solicitud-detalle.component').then(m => m.SolicitudDetalleComponent)
      },
      { path: '', redirectTo: 'solicitudes', pathMatch: 'full' }
    ]
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
        path: 'encargado',
        loadComponent: () =>
          import('./features/encargado/encargado-dashboard.component').then(m => m.EncargadoDashboardComponent)
      },
      {
        path: 'empresa',
        loadComponent: () =>
          import('./features/empresa/mi-empresa.component').then(m => m.MiEmpresaComponent)
      },
      {
        path: 'sucursales',
        loadComponent: () =>
          import('./features/sucursal/sucursal-list.component').then(m => m.SucursalListComponent)
      },
      {
        path: 'sucursales/nueva',
        loadComponent: () =>
          import('./features/sucursal/sucursal-form.component').then(m => m.SucursalFormComponent)
      },
      {
        path: 'sucursales/:id',
        loadComponent: () =>
          import('./features/sucursal/sucursal-form.component').then(m => m.SucursalFormComponent)
      },
      {
        path: 'empleados',
        loadComponent: () =>
          import('./features/empleado/empleado-list.component').then(m => m.EmpleadoListComponent)
      },
      {
        path: 'empleados/nuevo',
        loadComponent: () =>
          import('./features/empleado/empleado-form.component').then(m => m.EmpleadoFormComponent)
      },
      {
        path: 'empleados/:id',
        loadComponent: () =>
          import('./features/empleado/empleado-form.component').then(m => m.EmpleadoFormComponent)
      },
      {
        path: 'mesas',
        loadComponent: () =>
          import('./features/mesa/mesa-list.component').then(m => m.MesaListComponent)
      },
      {
        path: 'mesas/nueva',
        loadComponent: () =>
          import('./features/mesa/mesa-form.component').then(m => m.MesaFormComponent)
      },
      {
        path: 'mesas/:id',
        loadComponent: () =>
          import('./features/mesa/mesa-form.component').then(m => m.MesaFormComponent)
      },
      {
        path: 'grupos',
        loadComponent: () =>
          import('./features/grupo/grupo-list.component').then(m => m.GrupoListComponent)
      },
      {
        path: 'grupos/nuevo',
        loadComponent: () =>
          import('./features/grupo/grupo-form.component').then(m => m.GrupoFormComponent)
      },
      {
        path: 'grupos/:id',
        loadComponent: () =>
          import('./features/grupo/grupo-form.component').then(m => m.GrupoFormComponent)
      },
      {
        path: 'grupos/:id/miembros',
        loadComponent: () =>
          import('./features/grupo/grupo-miembros.component').then(m => m.GrupoMiembrosComponent)
      },
      {
        path: 'qr',
        loadComponent: () =>
          import('./features/qr/qr-list.component').then(m => m.QrListComponent)
      },
      {
        path: 'notificaciones',
        loadComponent: () =>
          import('./features/notificaciones/notificaciones.component').then(m => m.NotificacionesComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const redirectIfLoggedGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) return true;

  const rol = auth.getUsuario()?.rol;
  const destino = rol === 'SUPERADMIN' ? '/superadmin'
    : rol === 'ENCARGADO' ? '/app/encargado'
    : rol === 'EMPLEADO' ? '/app/empleado'
    : '/app/dashboard';
  return router.createUrlTree([destino]);
};

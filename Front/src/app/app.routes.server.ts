import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    // Páginas con captcha / query params que dependen del navegador.
    path: 'registro',
    renderMode: RenderMode.Client
  },
  {
    path: 'verificar-email',
    renderMode: RenderMode.Client
  },
  {
    path: 'superadmin/solicitudes/:id',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];

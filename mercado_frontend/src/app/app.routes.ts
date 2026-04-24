import { Routes } from '@angular/router';
import { authChildGuard, authGuard } from './core/guards/auth.guard';
import { AsignacionesPageComponent } from './features/asignaciones/pages/asignaciones-page/asignaciones-page.component';
import { LoginComponent } from './features/auth/login/login.component';
import { ConceptosPageComponent } from './features/conceptos/pages/conceptos-page/conceptos-page.component';
import { DeudasPageComponent } from './features/deudas/pages/deudas-page/deudas-page.component';
import { PagosPageComponent } from './features/pagos/pages/pagos-page/pagos-page.component';
import { PuestosPageComponent } from './features/puestos/pages/puestos-page/puestos-page.component';
import { ReportesPageComponent } from './features/reportes/pages/reportes-page/reportes-page.component';
import { SociosPageComponent } from './features/socios/pages/socios-page/socios-page.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { SectionPlaceholderComponent } from './shared/pages/section-placeholder/section-placeholder.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Iniciar sesion'
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    canActivateChild: [authChildGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'socios'
      },
      {
        path: 'socios',
        component: SociosPageComponent,
        title: 'Socios'
      },
      {
        path: 'puestos',
        component: PuestosPageComponent,
        title: 'Puestos'
      },
      {
        path: 'asignaciones',
        component: AsignacionesPageComponent,
        title: 'Asignaciones'
      },
      {
        path: 'conceptos',
        component: ConceptosPageComponent,
        title: 'Conceptos'
      },
      {
        path: 'deudas',
        component: DeudasPageComponent,
        title: 'Deudas'
      },
      {
        path: 'pagos',
        component: PagosPageComponent,
        title: 'Pagos'
      },
      {
        path: 'reportes',
        component: ReportesPageComponent,
        title: 'Reportes'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];

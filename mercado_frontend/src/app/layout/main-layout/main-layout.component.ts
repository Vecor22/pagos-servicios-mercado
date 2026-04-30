import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  route: string;
  marker: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  private readonly authService = inject(AuthService);

  readonly session = this.authService.getCurrentSession();
  readonly navItems: NavItem[] = [
    { label: 'Socios', route: '/socios', marker: 'SO' },
    { label: 'Puestos', route: '/puestos', marker: 'PU' },
    { label: 'Asignaciones', route: '/asignaciones', marker: 'AS' },
    { label: 'Conceptos', route: '/conceptos', marker: 'CO' },
    { label: 'Deudas', route: '/deudas', marker: 'DE' },
    { label: 'Pagos', route: '/pagos', marker: 'PA' },
    { label: 'Reportes', route: '/reportes', marker: 'RE' }
  ];

  logout(): void {
    this.authService.logout();
  }
}

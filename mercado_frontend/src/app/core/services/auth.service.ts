import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthSession, LoginRequest } from '../models/auth.models';

const SESSION_KEY = 'mercado.auth.session';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionSubject = new BehaviorSubject<AuthSession | null>(this.readSession());

  readonly session$ = this.sessionSubject.asObservable();

  login(credentials: LoginRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      map((response) => this.toSession(response)),
      tap((session) => this.saveSession(session))
    );
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.sessionSubject.next(null);
    void this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return Boolean(this.sessionSubject.value?.token);
  }

  getToken(): string | null {
    return this.sessionSubject.value?.token ?? null;
  }

  getAuthorizationHeader(): string | null {
    const session = this.sessionSubject.value;
    if (!session?.token) {
      return null;
    }

    return `${session.tokenType || 'Bearer'} ${session.token}`;
  }

  getCurrentSession(): AuthSession | null {
    return this.sessionSubject.value;
  }

  private saveSession(session: AuthSession): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.sessionSubject.next(session);
  }

  private readSession(): AuthSession | null {
    const stored = localStorage.getItem(SESSION_KEY);
    if (!stored) {
      return null;
    }

    try {
      return JSON.parse(stored) as AuthSession;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private toSession(response: AuthResponse): AuthSession {
    return {
      token: response.token,
      tokenType: response.tipoToken || 'Bearer',
      username: response.username,
      fullName: response.nombreCompleto
    };
  }
}

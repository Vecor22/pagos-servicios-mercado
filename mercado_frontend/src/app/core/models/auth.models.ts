export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tipoToken: string;
  username: string;
  nombreCompleto: string;
}

export interface AuthSession {
  token: string;
  tokenType: string;
  username: string;
  fullName: string;
}

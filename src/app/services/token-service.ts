import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private ACCESS = 'access_token';
  private REFRESH = 'refresh_token';

  setTokens(access: string, refresh: string): void {
    localStorage.setItem(this.ACCESS, access);
    localStorage.setItem(this.REFRESH, refresh);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH);
  }

  clear(): void {
    localStorage.removeItem(this.ACCESS);
    localStorage.removeItem(this.REFRESH);
  }

  decodeAccessToken(): any {
    const token = this.getAccessToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch (e) {
      console.error('Failed to decode token:', e);
      return null;
    }
  }
}

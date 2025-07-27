import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TokenService } from './token-service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:1234/Event'; // fixed realm

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  login(credentials: { userName: string, password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials).pipe(
      catchError(error => {
        console.error("❌ Login error:", error);
        return throwError(() => error);
      })
    );
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http.post(`${this.baseUrl}/refresh-token`, { refresh_token: refreshToken }).pipe(
      catchError(error => {
        console.error("❌ Refresh token error:", error);
        return throwError(() => error);
      })
    );
  }

  isLoggedIn(): boolean {
    return !!this.tokenService.getAccessToken();
  }

  hasRole(role: string): boolean {
    const token = this.tokenService.decodeAccessToken();
    return token?.roles?.includes(role);
  }

  logout(): void {
    this.tokenService.clear();
  }

  // auth-service.ts
  logoutFromServer(userId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout/${userId}`, {});
  }

  registerUser(user: any, role: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/create-user/${role}`, user);
  }
}

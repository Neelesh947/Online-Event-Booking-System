import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TokenService } from './token-service';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private baseUrl = 'http://localhost:1236/Event'; // fixed realm

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  getListOfAllCategory() {
    return this.http.get(`${this.baseUrl}/category`);
  }

  createCategory(category: { name: string }) {
    const accessToken = this.tokenService.getAccessToken();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    });
    return this.http.post(`${this.baseUrl}/category`, category, { headers });
  }
}

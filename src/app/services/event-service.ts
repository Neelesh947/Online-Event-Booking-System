import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TokenService } from './token-service';
import { Observable } from 'rxjs';

export interface EventRequest {
  title: string;
  description: string;
  location: string;
  startTime: string; // ISO string
  endTime: string;   // ISO string
  totalTickets: number;
  ticketPrice: number;
  imageUrl: string;
  categoryId: string;
}

export interface EventResponse {
  id: string;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  capacity: number;
  categoryName: string;
  createdBy: string;
  createdById: string;
}

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private baseUrl = 'http://localhost:1236/Event'; // fixed realm

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenService.getAccessToken();

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  createEvent(eventRequest: EventRequest): Observable<EventResponse> {
    const url = `${this.baseUrl}/events`;
    return this.http.post<EventResponse>(url, eventRequest, { headers: this.getAuthHeaders() });
  }

  getEventByOrganizer(): Observable<EventResponse[]>{
    const url = `${this.baseUrl}/events/organizer`;
    return this.http.get<EventResponse[]>(url, { headers: this.getAuthHeaders() })
  }

}

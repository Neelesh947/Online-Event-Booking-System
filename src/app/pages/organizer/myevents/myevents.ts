import { Component, OnInit } from '@angular/core';
import { EventResponse, EventService } from '../../../services/event-service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-myevents',
  standalone: false,
  templateUrl: './myevents.html',
  styleUrl: './myevents.css'
})
export class Myevents implements OnInit {

  events: EventResponse[] = [];
  isLoading: boolean = false;
  hasError: boolean = false;
  errorMessage: string = '';

  constructor(private eventService: EventService, private location:Location) { }

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.isLoading = true;
    this.hasError = false;

    this.eventService.getEventByOrganizer().subscribe({
      next: (data) => {
        this.events = data;  
        console.log(this.events)
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching events:', err);
        this.hasError = true;
        this.errorMessage = 'Failed to load events.';
        this.isLoading = false;
      }
    });
  }

  onUpdate(event: EventResponse): void{}
  onDelete(event: EventResponse): void{}

  onBack(): void {
  this.location.back();
}
}

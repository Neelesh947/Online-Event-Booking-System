import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CategoryService } from '../../../services/category-service';
import { Location } from '@angular/common';
import { EventRequest, EventService } from '../../../services/event-service';
import { Router } from '@angular/router';

interface Category {
  id: string;
  name: string;
}

@Component({
  selector: 'app-create-events',
  standalone: false,
  templateUrl: './create-events.html',
  styleUrl: './create-events.css'
})
export class CreateEvents implements OnInit {

  eventForm!: FormGroup;
  categories: Category[] = [];

  constructor(private fb: FormBuilder, private categoryService: CategoryService, private location: Location, 
    private eventService: EventService, private router:Router) { }

  ngOnInit(): void {
    this.initForm();
    this.fetchCategories();
  }

  initForm(): void {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      location: ['', Validators.required],
      startTime: ['', Validators.required],
      startDate: ['', Validators.required],
      endTime: ['', Validators.required],
      endDate: ['', Validators.required],
      totalTickets: [1, [Validators.required, Validators.min(1)]],
      ticketPrice: [0.01, [Validators.required, Validators.min(30.00)]],
      imageUrl: ['', Validators.required],
      category: ['', Validators.required],
      organizerId: ['', Validators.required]
    });
  }

  fetchCategories(): void {
    this.categoryService.getListOfAllCategory().subscribe({
      next: (data: any) => {
        console.log(data);
        this.categories = data;
      },
      error: (err) => {
        console.log("Error loading categories:", err)
      }
    })
  }

  onSubmit() {
    // if (this.eventForm.invalid) return;
    console.log("Clicked")
    const formValue = this.eventForm.value;
    const startDateTime = this.combineDateTime(formValue.startDate, formValue.startTime);
    const endDateTime = this.combineDateTime(formValue.endDate, formValue.endTime);

    const eventRequest: EventRequest = {
      title: formValue.title,
      description: formValue.description,
      location: formValue.location,
      startTime: startDateTime,
      endTime: endDateTime,
      totalTickets: formValue.totalTickets,
      ticketPrice: formValue.ticketPrice,
      imageUrl: formValue.imageUrl,
      categoryId: formValue.category
    };

    this.eventService.createEvent(eventRequest).subscribe({
      next: (res) => {
        alert('Event created successfully!');
        this.router.navigate(['organizer/my-event'])
      },
      error: (err) => {
        console.error('Event creation failed:', err);
        alert('Failed to create event.');
      }
    });
  }

  combineDateTime(date: any, time: any): string {
    if (!date || !time) return '';

    const dateObj = new Date(date);

    let hours = 0;
    let minutes = 0;

    if (typeof time === 'string') {
      const [h, m] = time.split(':');
      hours = parseInt(h, 10);
      minutes = parseInt(m, 10);
    } else if (time instanceof Date) {
      hours = time.getHours();
      minutes = time.getMinutes();
    }

    dateObj.setHours(hours, minutes, 0, 0);

    // Format to: YYYY-MM-DDTHH:mm:ss (without Z or ms)
    const isoString = dateObj.toISOString(); // e.g., 2025-08-10T10:00:00.000Z
    return isoString.slice(0, 19); // => "2025-08-10T10:00:00"
  }


  onBack(): void {
    this.location.back();
  }
}

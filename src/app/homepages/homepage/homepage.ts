import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-homepage',
  standalone: false,
  templateUrl: './homepage.html',
  styleUrl: './homepage.css'
})
export class Homepage implements OnInit{

  upcomingEvents = [
    { title: 'Standup Night - Delhi', date: 'Aug 10', location: 'Delhi NCR' },
    { title: 'Music Festival 2025', date: 'Sep 2', location: 'Bangalore' },
    { title: 'Startup Meetup', date: 'Aug 30', location: 'Pune' }
  ];

  benefits = [
    '✅ Easy online bookings',
    '✅ Save your event history',
    '✅ Organize your own events',
    '✅ Secure payments via Razorpay'
  ];

  howItWorks = [
    'Browse public events',
    'Login to book your seat',
    'Pay securely with Razorpay',
    'Attend the event with QR code'
  ];


  constructor(){}

  ngOnInit(): void {
    
  }
}

import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {

  metrics = {
    totalEvents: 0,
    totalBookings: 0,
    totalUsers: 0,
    totalRevenue: 0
  };
}

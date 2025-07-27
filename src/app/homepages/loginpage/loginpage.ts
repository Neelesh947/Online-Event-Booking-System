import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { TokenService } from '../../services/token-service';
import { UserActivityService } from '../../services/user-activity-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-loginpage',
  standalone: false,
  templateUrl: './loginpage.html',
  styleUrl: './loginpage.css'
})
export class Loginpage implements OnInit{

  userName = '';
  password = '';

  constructor(
    private authService: AuthService,
    private tokenService: TokenService,
    private userActivityService: UserActivityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    
  }

  login(){
    console.log("page clicked")
    this.authService.login({ userName: this.userName, password: this.password }).subscribe({
      next: res => {
          this.tokenService.setTokens(res.access_token, res.refresh_token);
          this.userActivityService.startTracking();
          // this.router.navigate(['/dashboard']); // change as needed
          console.log("login successfully" , res)
        },
        error: err => alert('Login failed')
    })
  }
}

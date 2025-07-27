import { Component } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { TokenService } from '../../services/token-service';

@Component({
  selector: 'app-navbar-component',
  standalone: false,
  templateUrl: './navbar-component.html',
  styleUrl: './navbar-component.css'
})
export class NavbarComponent {

  constructor(public authService: AuthService, private tokenService: TokenService) {}

  logout(){
    const decoded = this.tokenService.decodeAccessToken();
    const userId = decoded?.sub;

    console.log("userid", userId)

    if(userId){
      this.authService.logoutFromServer(userId).subscribe({
        next: () => {
          console.log('✅ Logout successful');
          this.tokenService.clear();
        },error: (err) => {
          console.error('❌ Logout API failed', err);
          this.tokenService.clear();
        }
      })
    } else {
      this.tokenService.clear();
    }
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}

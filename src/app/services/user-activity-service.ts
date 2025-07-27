import { Injectable, NgZone } from '@angular/core';
import { fromEvent, merge, interval, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { TokenService } from './token-service';
import { AuthService } from './auth-service';

@Injectable({ providedIn: 'root' })
export class UserActivityService {
  private activitySub!: Subscription;
  private refreshSub!: Subscription;

  constructor(
    private authService: AuthService,
    private tokenService: TokenService,
    private zone: NgZone
  ) {}

  startTracking() {
    this.zone.runOutsideAngular(() => {
      const events = merge(
        fromEvent(window, 'mousemove'),
        fromEvent(window, 'keydown'),
        fromEvent(window, 'click')
      );

      this.activitySub = events
        .pipe(debounceTime(2000))
        .subscribe(() => this.scheduleRefresh());
    });
  }

  private scheduleRefresh() {
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }

    this.refreshSub = interval(4 * 60 * 1000).subscribe(() => {
      const refreshToken = this.tokenService.getRefreshToken();
      if (refreshToken) {
        this.authService.refreshToken().subscribe({
          next: res => this.tokenService.setTokens(res.access_token, res.refresh_token),
          error: err => {
            console.error('Refresh failed', err);
            this.tokenService.clear();
          }
        });
      }
    });
  }

  stopTracking() {
    this.activitySub?.unsubscribe();
    this.refreshSub?.unsubscribe();
  }
}

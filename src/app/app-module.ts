import { NgModule, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { NavbarComponent } from './navbar/navbar-component/navbar-component';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Homepage } from './homepages/homepage/homepage';
import { Loginpage } from './homepages/loginpage/loginpage';
import { EventPage } from './homepages/event-page/event-page';
import { Register } from './homepages/register/register';
import { MatList, MatListItem } from '@angular/material/list';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardContent, MatCardModule, MatCardTitle } from '@angular/material/card';
import { MatError, MatFormField, MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInput, MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatOption } from '@angular/material/autocomplete';
import { MatSelectModule } from '@angular/material/select';
import { HTTP_INTERCEPTORS, HttpClientModule, withInterceptors } from '@angular/common/http';
import { AuthInterceptor } from './services/auth-interceptor';
import { Dashboard } from './pages/super_admin/dashboard/dashboard';
import { ComponentDashboard } from './pages/super_admin/component-dashboard/component-dashboard';
import { CategoryCreateDashboard } from './pages/super_admin/category-create-dashboard/category-create-dashboard';
import { EventList } from './pages/super_admin/event-list/event-list';
import { HomePage } from './pages/organizer/home-page/home-page';
import { CreateEvents } from './pages/organizer/create-events/create-events';
import { MatDatepicker } from '@angular/material/datepicker';
import {MatDatepickerModule} from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import {MatTimepickerModule} from '@angular/material/timepicker';
import { Myevents } from './pages/organizer/myevents/myevents';

@NgModule({
  declarations: [
    App,
    NavbarComponent,
    Homepage,
    Loginpage,
    EventPage,
    Register,
    Dashboard,
    ComponentDashboard,
    CategoryCreateDashboard,
    EventList,
    HomePage,
    CreateEvents,
    Myevents
  ],
  imports: [
    BrowserModule, ReactiveFormsModule,
    AppRoutingModule, MatOption, MatFormFieldModule, 
    MatToolbarModule, MatInputModule, MatSelectModule, MatCardModule,
    MatButtonModule, HttpClientModule,
    MatList, MatError, FormsModule,
    MatIcon, MatLabel, MatFormField, MatInput,
    MatListItem, MatCard, MatCardTitle, MatCardContent, MatDatepicker,MatDatepickerModule,
    MatNativeDateModule,MatTimepickerModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }

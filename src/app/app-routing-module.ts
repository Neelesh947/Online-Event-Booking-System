import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Homepage } from './homepages/homepage/homepage';
import { Loginpage } from './homepages/loginpage/loginpage';
import { Register } from './homepages/register/register';
import { Dashboard } from './pages/super_admin/dashboard/dashboard';
import { CategoryCreateDashboard } from './pages/super_admin/category-create-dashboard/category-create-dashboard';
import { HomePage } from './pages/organizer/home-page/home-page';
import { CreateEvents } from './pages/organizer/create-events/create-events';
import { Myevents } from './pages/organizer/myevents/myevents';

const routes: Routes = [
  { path: '', component: Homepage },
  { path: 'login', component: Loginpage },
  { path: 'sign-up', component: Register },
  { path: 'admin/dashboard', component: Dashboard },
  { path: 'admin/categories', component: CategoryCreateDashboard },
  { path: 'organizer/dashboard', component: HomePage },
  { path: 'organizer/create-event', component: CreateEvents },
  { path: 'organizer/my-event', component: Myevents },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

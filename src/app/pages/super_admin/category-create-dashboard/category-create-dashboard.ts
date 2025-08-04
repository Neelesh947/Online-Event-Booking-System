import { Location } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CategoryService } from '../../../services/category-service';

@Component({
  selector: 'app-category-create-dashboard',
  standalone: false,
  templateUrl: './category-create-dashboard.html',
  styleUrl: './category-create-dashboard.css'
})
export class CategoryCreateDashboard implements OnInit {

  category = { name: '' };
  message: string = '';
  isError: boolean = false;
  categories: any[] = [];

  constructor(private http: HttpClient, private location: Location, private router: Router, private categorys: CategoryService) { }


  ngOnInit() {
    this.categorys.getListOfAllCategory().subscribe({
      next: (data: any) => {
        console.log(data);
        this.categories = data;
      },
      error: (err) => {
        console.log("Error loading categories:", err)
      }
    })
  }

  createCategory() {
    if (!this.category.name.trim()) {
      this.message = 'Category name is required.';
      this.isError = true;
      return;
    }

    this.categorys.createCategory(this.category).subscribe({
      next: (res: any) => {
        this.message = 'Category created successfully!';
        this.isError = false;
        this.categories.push(res);
        this.category.name = '';
      },
      error: (err) => {
        console.error('Error creating category:', err);
        this.message = 'Failed to create category.';
        this.isError = true;
      }
    });
  }

  goBack() {
    this.location.back();
    this.router.navigate(['']);
  }
}

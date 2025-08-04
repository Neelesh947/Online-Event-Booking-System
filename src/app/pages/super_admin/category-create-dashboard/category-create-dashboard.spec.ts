import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryCreateDashboard } from './category-create-dashboard';

describe('CategoryCreateDashboard', () => {
  let component: CategoryCreateDashboard;
  let fixture: ComponentFixture<CategoryCreateDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CategoryCreateDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoryCreateDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

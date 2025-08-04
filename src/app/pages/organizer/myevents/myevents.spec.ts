import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Myevents } from './myevents';

describe('Myevents', () => {
  let component: Myevents;
  let fixture: ComponentFixture<Myevents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Myevents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Myevents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

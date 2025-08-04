import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateEvents } from './create-events';

describe('CreateEvents', () => {
  let component: CreateEvents;
  let fixture: ComponentFixture<CreateEvents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateEvents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateEvents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

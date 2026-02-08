import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedulePicker } from './schedule-picker';

describe('SchedulePicker', () => {
  let component: SchedulePicker;
  let fixture: ComponentFixture<SchedulePicker>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchedulePicker]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SchedulePicker);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

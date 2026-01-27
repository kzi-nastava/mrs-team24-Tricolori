import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRideTracking } from './driver-ride-tracking';

describe('DriverRideTracking', () => {
  let component: DriverRideTracking;
  let fixture: ComponentFixture<DriverRideTracking>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRideTracking]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRideTracking);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

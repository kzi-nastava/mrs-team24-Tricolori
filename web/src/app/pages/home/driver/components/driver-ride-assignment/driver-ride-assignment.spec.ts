import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRideAssignment } from './driver-ride-assignment';

describe('DriverRideAssignment', () => {
  let component: DriverRideAssignment;
  let fixture: ComponentFixture<DriverRideAssignment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRideAssignment]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRideAssignment);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideEstimationInitial } from './ride-estimation-initial';

describe('RideEstimationInitial', () => {
  let component: RideEstimationInitial;
  let fixture: ComponentFixture<RideEstimationInitial>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideEstimationInitial]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideEstimationInitial);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideEstimationResult } from './ride-estimation-result';

describe('RideEstimationResult', () => {
  let component: RideEstimationResult;
  let fixture: ComponentFixture<RideEstimationResult>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideEstimationResult]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideEstimationResult);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

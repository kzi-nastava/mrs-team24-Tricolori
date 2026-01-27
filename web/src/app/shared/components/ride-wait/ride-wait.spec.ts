import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideWait } from './ride-wait';

describe('RideWait', () => {
  let component: RideWait;
  let fixture: ComponentFixture<RideWait>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideWait]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideWait);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

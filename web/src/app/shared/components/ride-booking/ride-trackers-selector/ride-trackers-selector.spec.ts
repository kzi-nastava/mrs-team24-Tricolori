import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideTrackersSelector } from './ride-trackers-selector';

describe('RideTrackersSelector', () => {
  let component: RideTrackersSelector;
  let fixture: ComponentFixture<RideTrackersSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideTrackersSelector]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTrackersSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

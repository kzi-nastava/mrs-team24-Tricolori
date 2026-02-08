import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RouteSelector } from './route-selector';

describe('RouteSelector', () => {
  let component: RouteSelector;
  let fixture: ComponentFixture<RouteSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouteSelector]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RouteSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavoriteRouteSelector } from './favorite-route-selector';

describe('FavoriteRouteSelector', () => {
  let component: FavoriteRouteSelector;
  let fixture: ComponentFixture<FavoriteRouteSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavoriteRouteSelector]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavoriteRouteSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

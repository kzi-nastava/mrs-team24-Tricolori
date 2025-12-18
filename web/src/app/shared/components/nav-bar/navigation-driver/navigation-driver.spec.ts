import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavigationDriver } from './navigation-driver';

describe('NavigationDriver', () => {
  let component: NavigationDriver;
  let fixture: ComponentFixture<NavigationDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigationDriver]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavigationDriver);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

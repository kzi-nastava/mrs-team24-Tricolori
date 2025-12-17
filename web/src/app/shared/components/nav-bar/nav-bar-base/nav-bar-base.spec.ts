import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavBarBase } from './nav-bar-base';

describe('NavBarBase', () => {
  let component: NavBarBase;
  let fixture: ComponentFixture<NavBarBase>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarBase]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavBarBase);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseProfile } from './base-profile';

describe('BaseProfile', () => {
  let component: BaseProfile;
  let fixture: ComponentFixture<BaseProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseProfile]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseProfile);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

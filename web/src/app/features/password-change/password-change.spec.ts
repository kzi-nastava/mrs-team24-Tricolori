import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordChange } from './password-change';

describe('PasswordChange', () => {
  let component: PasswordChange;
  let fixture: ComponentFixture<PasswordChange>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordChange]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordChange);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

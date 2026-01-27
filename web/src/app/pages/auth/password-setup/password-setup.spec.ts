import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordSetup } from './password-setup';

describe('PasswordSetup', () => {
  let component: PasswordSetup;
  let fixture: ComponentFixture<PasswordSetup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordSetup]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordSetup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

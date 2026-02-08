import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepOneForm } from './step-one-form';

describe('StepOneForm', () => {
  let component: StepOneForm;
  let fixture: ComponentFixture<StepOneForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepOneForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StepOneForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

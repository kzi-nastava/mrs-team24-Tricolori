import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepTwoForm } from './step-two-form';

describe('StepTwoForm', () => {
  let component: StepTwoForm;
  let fixture: ComponentFixture<StepTwoForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepTwoForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StepTwoForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

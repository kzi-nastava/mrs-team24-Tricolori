
import { StepTwoForm } from './step-two-form';
import { hugeCar03 } from '@ng-icons/huge-icons';
import { mynaBaby } from '@ng-icons/mynaui/outline';
import { phosphorDog } from '@ng-icons/phosphor-icons/regular';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { StepTwoDriverRegistrationData } from '../../../../../model/driver-registration';

describe('StepTwoForm', () => {
  let component: StepTwoForm;
  let fixture: ComponentFixture<StepTwoForm>;

  const validFormData = {
    email: 'driver@example.com',
    vehicleModel: 'Toyota Corolla',
    vehicleType: 'standard',
    registrationPlate: 'NS-1234-AB',
    seatNumber: 4,
    petFriendly: false,
    babyFriendly: false
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepTwoForm, ReactiveFormsModule],
      providers: [
        provideIcons({ hugeCar03, mynaBaby, phosphorDog })
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StepTwoForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // --- Kreiranje ---

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // --- Inicijalizacija ---

  describe('Form Initialization', () => {
    it('should initialize email as empty', () => {
      expect(component.email.value).toBe('');
    });

    it('should initialize vehicleModel as empty', () => {
      expect(component.vehicleModel.value).toBe('');
    });

    it('should initialize registrationPlate as empty', () => {
      expect(component.registrationPlate.value).toBe('');
    });

    it('should initialize seatNumber as empty', () => {
      expect(component.seatNumber.value).toBe('');
    });

    it('should initialize petFriendly as false', () => {
      expect(component.petFriendly.value).toBeFalse();
    });

    it('should initialize babyFriendly as false', () => {
      expect(component.babyFriendly.value).toBeFalse();
    });

    it('should have invalid form when empty', () => {
      expect(component.secondStepForm.valid).toBeFalsy();
    });
  });

  // --- Validacija ---

  describe('Form Validation', () => {
    it('should require email', () => {
      expect(component.email.hasError('required')).toBeTruthy();
    });

    it('should validate email format', () => {
      component.email.setValue('invalid-email');
      expect(component.email.hasError('email')).toBeTruthy();

      component.email.setValue('valid@email.com');
      expect(component.email.valid).toBeTruthy();
    });

    it('should require vehicleModel', () => {
      expect(component.vehicleModel.hasError('required')).toBeTruthy();

      component.vehicleModel.setValue('Toyota Corolla');
      expect(component.vehicleModel.hasError('required')).toBeFalsy();
    });

    it('should require registrationPlate', () => {
      expect(component.registrationPlate.hasError('required')).toBeTruthy();

      component.registrationPlate.setValue('NS-1234-AB');
      expect(component.registrationPlate.hasError('required')).toBeFalsy();
    });

    it('should require seatNumber', () => {
      expect(component.seatNumber.hasError('required')).toBeTruthy();
    });

    it('should require seatNumber to be at least 1', () => {
      component.seatNumber.setValue(0);
      expect(component.seatNumber.hasError('min')).toBeTruthy();

      component.seatNumber.setValue(1);
      expect(component.seatNumber.hasError('min')).toBeFalsy();
    });

    it('should be valid when all required fields are filled correctly', () => {
      component.secondStepForm.patchValue(validFormData);
      expect(component.secondStepForm.valid).toBeTruthy();
    });
  });

  // --- handleSubmit ---

  describe('handleSubmit()', () => {
    it('should not emit when form is invalid', () => {
      let emitted = false;
      fixture.componentRef.instance.newStepData.subscribe(() => emitted = true);

      component.handleSubmit();

      expect(emitted).toBeFalse();
    });

    it('should mark all fields as touched when form is invalid', () => {
      component.handleSubmit();

      expect(component.email.touched).toBeTruthy();
      expect(component.vehicleModel.touched).toBeTruthy();
      expect(component.registrationPlate.touched).toBeTruthy();
      expect(component.seatNumber.touched).toBeTruthy();
    });

    it('should emit newStepData when form is valid', () => {
      let emittedData: StepTwoDriverRegistrationData | undefined;
      fixture.componentRef.instance.newStepData.subscribe((data: StepTwoDriverRegistrationData) => emittedData = data);

      component.secondStepForm.patchValue(validFormData);
      component.handleSubmit();

      expect(emittedData).toBeDefined();
      expect(emittedData?.email).toBe('driver@example.com');
      expect(emittedData?.vehicleModel).toBe('Toyota Corolla');
      expect(emittedData?.registrationPlate).toBe('NS-1234-AB');
      expect(emittedData?.seatNumber).toBe(4);
    });

    it('should emit petFriendly and babyFriendly values', () => {
      let emittedData: StepTwoDriverRegistrationData | undefined;
      fixture.componentRef.instance.newStepData.subscribe((data: StepTwoDriverRegistrationData) => emittedData = data);

      component.secondStepForm.patchValue({ ...validFormData, petFriendly: true, babyFriendly: true });
      component.handleSubmit();

      expect(emittedData?.petFriendly).toBeTrue();
      expect(emittedData?.babyFriendly).toBeTrue();
    });
  });

  // --- goBack ---

  describe('goBack()', () => {
    it('should emit back output with current form values', () => {
      let emittedData: StepTwoDriverRegistrationData | undefined;
      fixture.componentRef.instance.back.subscribe((data: StepTwoDriverRegistrationData) => emittedData = data);

      component.secondStepForm.patchValue(validFormData);
      component.goBack();

      expect(emittedData).toBeDefined();
      expect(emittedData?.email).toBe('driver@example.com');
    });

    it('should emit back even when form is invalid', () => {
      let emitted = false;
      fixture.componentRef.instance.back.subscribe(() => emitted = true);

      component.goBack();

      expect(emitted).toBeTrue();
    });
  });

  // --- oldStepData input ---

  describe('oldStepData input', () => {
    it('should patch form when oldStepData is provided', () => {
      const oldData: StepTwoDriverRegistrationData = {
        ...validFormData,
        petFriendly: true,
        babyFriendly: false
      };

      fixture.componentRef.setInput('oldStepData', oldData);
      fixture.detectChanges();

      expect(component.email.value).toBe('driver@example.com');
      expect(component.vehicleModel.value).toBe('Toyota Corolla');
      expect(component.registrationPlate.value).toBe('NS-1234-AB');
      expect(component.seatNumber.value).toBe(4);
      expect(component.petFriendly.value).toBeTrue();
    });
  });
});
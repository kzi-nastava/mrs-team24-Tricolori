import { DriverRegister } from './driver-register';
import { AuthService } from '../../../services/auth.service';
import { of, throwError } from 'rxjs';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StepOneDriverRegistrationData, StepTwoDriverRegistrationData } from '../../../model/driver-registration';
import { StepOneForm } from './components/step-one-form/step-one-form';
import { StepTwoForm } from './components/step-two-form/step-two-form';

import { NO_ERRORS_SCHEMA } from '@angular/core';

@Component({ selector: 'app-step-one-form', template: '', standalone: true })
class StepOneFormStub {}

@Component({ selector: 'app-step-two-form', template: '', standalone: true })
class StepTwoFormStub {}

describe('DriverRegister', () => {
  let component: DriverRegister;
  let fixture: ComponentFixture<DriverRegister>;
  let authService: jasmine.SpyObj<AuthService>;

  const mockStepOneData: StepOneDriverRegistrationData = {
    firstName: 'John',
    lastName: 'Doe',
    phone: '+381641234567',
    address: 'Bulevar Oslobodjenja 1',
    pfpFile: null
  };

  const mockStepTwoData: StepTwoDriverRegistrationData = {
    email: 'driver@example.com',
    vehicleModel: 'Toyota Corolla',
    vehicleType: 'standard',
    registrationPlate: 'NS-1234-AB',
    seatNumber: 4,
    petFriendly: false,
    babyFriendly: false
  };

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['registerDriver']);

    await TestBed.configureTestingModule({
      imports: [DriverRegister],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]  // ovo ignorise nepoznate property bindinge na child komponentama
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture = TestBed.createComponent(DriverRegister);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Step Navigation', () => {
    it('should start on step 1', () => {
      expect(component.step()).toBe(1);
    });

    it('should advance to step 2 after handleStepOne', () => {
      component.handleStepOne(mockStepOneData);
      expect(component.step()).toBe(2);
    });

    it('should save step one data after handleStepOne', () => {
      component.handleStepOne(mockStepOneData);
      expect(component.savedStepOneData).toEqual(mockStepOneData);
    });

    it('should go back to step 1 when prevStep is called', () => {
      component.handleStepOne(mockStepOneData);
      component.prevStep(mockStepTwoData);
      expect(component.step()).toBe(1);
    });

    it('should save step two data when going back', () => {
      component.prevStep(mockStepTwoData);
      expect(component.savedStepTwoData).toEqual(mockStepTwoData);
    });
  });

  describe('handleStepTwo() - Registration', () => {
    beforeEach(() => {
      component.savedStepOneData = mockStepOneData;
    });

    it('should call authService.registerDriver with correct data', () => {
      authService.registerDriver.and.returnValue(of('Success'));

      component.handleStepTwo(mockStepTwoData);

      expect(authService.registerDriver).toHaveBeenCalledWith(
        jasmine.objectContaining({
          firstName: 'John',
          lastName: 'Doe',
          phone: '+381641234567',
          address: 'Bulevar Oslobodjenja 1',
          email: 'driver@example.com',
          vehicleModel: 'Toyota Corolla',
          registrationPlate: 'NS-1234-AB',
          seatNumber: 4,
          petFriendly: false,
          babyFriendly: false
        }),
        null
      );
    });

    it('should pass pfpFile to registerDriver when file is selected', () => {
      const mockFile = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
      component.savedStepOneData = { ...mockStepOneData, pfpFile: mockFile };
      authService.registerDriver.and.returnValue(of('Success'));

      component.handleStepTwo(mockStepTwoData);

      expect(authService.registerDriver).toHaveBeenCalledWith(
        jasmine.any(Object),
        mockFile
      );
    });

    it('should pass null as file when no pfp selected', () => {
      authService.registerDriver.and.returnValue(of('Success'));

      component.handleStepTwo(mockStepTwoData);

      expect(authService.registerDriver).toHaveBeenCalledWith(
        jasmine.any(Object),
        null
      );
    });

    it('should reset to step 1 after successful registration', () => {
      authService.registerDriver.and.returnValue(of('Success'));

      component.handleStepOne(mockStepOneData);
      component.handleStepTwo(mockStepTwoData);

      expect(component.step()).toBe(1);
    });

    it('should clear saved data after registration', () => {
      authService.registerDriver.and.returnValue(of('Success'));

      component.handleStepTwo(mockStepTwoData);

      expect(component.savedStepOneData).toBeUndefined();
      expect(component.savedStepTwoData).toBeUndefined();
    });

    it('should handle registration error gracefully', () => {
      authService.registerDriver.and.returnValue(throwError(() => ({ error: 'Error' })));

      expect(() => component.handleStepTwo(mockStepTwoData)).not.toThrow();
    });
  });
});
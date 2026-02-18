import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { heroUser } from '@ng-icons/heroicons/outline';
import { matAccountCircleOutline } from '@ng-icons/material-icons/outline';
import { StepOneForm } from './step-one-form';
import { Component, signal } from '@angular/core';
import { PfpPicker } from '../../../../../components/pfp-picker/pfp-picker';
import { StepOneDriverRegistrationData } from '../../../../../model/driver-registration';

// Stub za PfpPicker da izbjegnemo kompleksnost child komponente
@Component({ selector: 'app-pfp-picker', template: '', standalone: true })
class PfpPickerStub {}

describe('StepOneForm', () => {
  let component: StepOneForm;
  let fixture: ComponentFixture<StepOneForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
        imports: [StepOneForm, ReactiveFormsModule, PfpPicker],
        providers: [
        provideIcons({ 
            heroUser, 
            matAccountCircleOutline,
        })
        ]
    }).compileComponents();

    fixture = TestBed.createComponent(StepOneForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
    });

  // --- Kreiranje ---

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // --- Inicijalizacija forme ---

  describe('Form Initialization', () => {
    it('should initialize form with empty values', () => {
      expect(component.firstName.value).toBe('');
      expect(component.lastName.value).toBe('');
      expect(component.phone.value).toBe('');
      expect(component.address.value).toBe('');
    });

    it('should have invalid form when empty', () => {
      expect(component.firstStepForm.valid).toBeFalsy();
    });

    it('should initialize selectedFile signal as null', () => {
      expect(component.selectedFile()).toBeNull();
    });
  });

  // --- Validacija ---

  describe('Form Validation', () => {
    it('should require firstName', () => {
      expect(component.firstName.hasError('required')).toBeTruthy();

      component.firstName.setValue('John');
      expect(component.firstName.hasError('required')).toBeFalsy();
    });

    it('should require lastName', () => {
      expect(component.lastName.hasError('required')).toBeTruthy();

      component.lastName.setValue('Doe');
      expect(component.lastName.hasError('required')).toBeFalsy();
    });

    it('should require phone', () => {
      expect(component.phone.hasError('required')).toBeTruthy();

      component.phone.setValue('+381641234567');
      expect(component.phone.hasError('required')).toBeFalsy();
    });

    it('should require address', () => {
      expect(component.address.hasError('required')).toBeTruthy();

      component.address.setValue('Bulevar Oslobodjenja 1');
      expect(component.address.hasError('required')).toBeFalsy();
    });

    it('should be valid when all fields are filled', () => {
      component.firstStepForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        phone: '+381641234567',
        address: 'Bulevar Oslobodjenja 1'
      });

      expect(component.firstStepForm.valid).toBeTruthy();
    });
  });

  // --- completeStep ---

  describe('completeStep()', () => {
    it('should not emit when form is invalid', () => {
      let emitted = false;
      const outputRef = fixture.componentRef.instance.newStepData;
      outputRef.subscribe(() => emitted = true);

      component.completeStep();

      expect(emitted).toBeFalse();
    });

    it('should mark all fields as touched when form is invalid', () => {
      component.completeStep();

      expect(component.firstName.touched).toBeTruthy();
      expect(component.lastName.touched).toBeTruthy();
      expect(component.phone.touched).toBeTruthy();
      expect(component.address.touched).toBeTruthy();
    });

    it('should emit newStepData when form is valid', () => {
      let emittedData: StepOneDriverRegistrationData | undefined;
      fixture.componentRef.instance.newStepData.subscribe((data: StepOneDriverRegistrationData) => emittedData = data);

      component.firstStepForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        phone: '+381641234567',
        address: 'Bulevar Oslobodjenja 1'
      });

      component.completeStep();

      expect(emittedData).toBeDefined();
      expect(emittedData?.firstName).toBe('John');
      expect(emittedData?.lastName).toBe('Doe');
      expect(emittedData?.phone).toBe('+381641234567');
      expect(emittedData?.address).toBe('Bulevar Oslobodjenja 1');
    });

    it('should include pfpFile in emitted data', () => {
      let emittedData: StepOneDriverRegistrationData | undefined;
      fixture.componentRef.instance.newStepData.subscribe((data: StepOneDriverRegistrationData) => emittedData = data);

      const mockFile = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
      component.selectedFile.set(mockFile);

      component.firstStepForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        phone: '+381641234567',
        address: 'Bulevar Oslobodjenja 1'
      });

      component.completeStep();

      expect(emittedData?.pfpFile).toBe(mockFile);
    });

    it('should include null pfpFile when no file selected', () => {
      let emittedData: StepOneDriverRegistrationData | undefined;
      fixture.componentRef.instance.newStepData.subscribe((data: StepOneDriverRegistrationData) => emittedData = data);

      component.firstStepForm.patchValue({
        firstName: 'John',
        lastName: 'Doe',
        phone: '+381641234567',
        address: 'Bulevar Oslobodjenja 1'
      });

      component.completeStep();

      expect(emittedData?.pfpFile).toBeNull();
    });
  });

  // --- handleNewFile ---

  describe('handleNewFile()', () => {
    it('should update selectedFile signal', () => {
      const mockFile = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
      component.handleNewFile(mockFile);
      expect(component.selectedFile()).toBe(mockFile);
    });
  });

  // --- oldStepData input (patch) ---

  describe('oldStepData input', () => {
    it('should patch form when oldStepData is provided', () => {
      const oldData: StepOneDriverRegistrationData = {
        firstName: 'Marko',
        lastName: 'Markovic',
        phone: '+381611111111',
        address: 'Jovana Subotica 5',
        pfpFile: null
      };

      fixture.componentRef.setInput('oldStepData', oldData);
      fixture.detectChanges();

      expect(component.firstName.value).toBe('Marko');
      expect(component.lastName.value).toBe('Markovic');
      expect(component.phone.value).toBe('+381611111111');
      expect(component.address.value).toBe('Jovana Subotica 5');
    });

    it('should set selectedFile from oldStepData', () => {
      const mockFile = new File(['img'], 'old.jpg', { type: 'image/jpeg' });
      const oldData: StepOneDriverRegistrationData = {
        firstName: 'Marko',
        lastName: 'Markovic',
        phone: '+381611111111',
        address: 'Jovana Subotica 5',
        pfpFile: mockFile
      };

      fixture.componentRef.setInput('oldStepData', oldData);
      fixture.detectChanges();

      expect(component.selectedFile()).toBe(mockFile);
    });
  });
});
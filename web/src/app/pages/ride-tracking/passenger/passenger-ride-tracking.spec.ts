import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PassengerRideTrackingComponent } from './passenger-ride-tracking';

describe('PassengerRideTrackingComponent', () => {
  let component: PassengerRideTrackingComponent;
  let fixture: ComponentFixture<PassengerRideTrackingComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerRideTrackingComponent, ReactiveFormsModule],
      providers: [
        {
          provide: Router,
          useValue: {
            navigate: jasmine.createSpy('navigate')
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PassengerRideTrackingComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with default values', () => {
      expect(component.showReportForm()).toBe(false);
      expect(component.isSubmittingReport()).toBe(false);
      expect(component.reportSubmitted()).toBe(false);
      expect(component.estimatedArrival()).toBeGreaterThan(0);
      expect(component.remainingDistance()).toBeGreaterThan(0);
    });

    it('should have valid ride details', () => {
      const rideDetails = component.rideDetails();
      expect(rideDetails.id).toBeDefined();
      expect(rideDetails.pickup).toBeDefined();
      expect(rideDetails.destination).toBeDefined();
      expect(rideDetails.pickupCoords).toHaveSize(2);
      expect(rideDetails.destinationCoords).toHaveSize(2);
      expect(rideDetails.driverName).toBeDefined();
      expect(rideDetails.vehicleType).toBeDefined();
      expect(rideDetails.licensePlate).toBeDefined();
    });

    it('should initialize report form with validators', () => {
      expect(component.reportForm).toBeDefined();
      expect(component.reportForm.get('description')).toBeDefined();
      
      const descriptionControl = component.reportForm.get('description');
      expect(descriptionControl?.hasError('required')).toBe(true);
    });
  });

  describe('Report Form', () => {
    it('should toggle report form visibility', () => {
      expect(component.showReportForm()).toBe(false);
      
      component.toggleReportForm();
      expect(component.showReportForm()).toBe(true);
      
      component.toggleReportForm();
      expect(component.showReportForm()).toBe(false);
    });

    it('should validate description field - required', () => {
      const descriptionControl = component.reportForm.get('description');
      
      descriptionControl?.setValue('');
      expect(descriptionControl?.hasError('required')).toBe(true);
      
      descriptionControl?.setValue('Valid description with enough characters');
      expect(descriptionControl?.hasError('required')).toBe(false);
    });

    it('should validate description field - minlength', () => {
      const descriptionControl = component.reportForm.get('description');
      
      descriptionControl?.setValue('short');
      expect(descriptionControl?.hasError('minlength')).toBe(true);
      
      descriptionControl?.setValue('This is a valid description');
      expect(descriptionControl?.hasError('minlength')).toBe(false);
    });

    it('should validate description field - maxlength', () => {
      const descriptionControl = component.reportForm.get('description');
      const longText = 'a'.repeat(501);
      
      descriptionControl?.setValue(longText);
      expect(descriptionControl?.hasError('maxlength')).toBe(true);
      
      descriptionControl?.setValue('Valid description');
      expect(descriptionControl?.hasError('maxlength')).toBe(false);
    });

    it('should not submit invalid form', () => {
      component.reportForm.get('description')?.setValue('');
      component.submitReport();
      
      expect(component.isSubmittingReport()).toBe(false);
    });

    it('should submit valid report', (done) => {
      component.reportForm.get('description')?.setValue('Driver took an unusual route through the park');
      
      component.submitReport();
      expect(component.isSubmittingReport()).toBe(true);
      
      // Wait for simulated API call
      setTimeout(() => {
        expect(component.isSubmittingReport()).toBe(false);
        expect(component.reportSubmitted()).toBe(true);
        expect(component.showReportForm()).toBe(false);
        done();
      }, 2000);
    });

    it('should reset form when toggling off', () => {
      component.reportForm.get('description')?.setValue('Some text');
      component.showReportForm.set(true);
      
      component.toggleReportForm();
      
      expect(component.showReportForm()).toBe(false);
      expect(component.reportForm.get('description')?.value).toBe(null);
    });
  });

  describe('Progress Calculation', () => {
    it('should calculate progress percentage correctly', () => {
      component.rideDetails.set({
        ...component.rideDetails(),
        totalDistance: 10
      });
      component.remainingDistance.set(5);
      
      expect(component.progressPercentage()).toBe(50);
    });

    it('should show 0% when no progress', () => {
      const totalDistance = component.rideDetails().totalDistance;
      component.remainingDistance.set(totalDistance);
      
      expect(component.progressPercentage()).toBe(0);
    });

    it('should show 100% when complete', () => {
      component.remainingDistance.set(0);
      
      expect(component.progressPercentage()).toBe(100);
    });
  });

  describe('Navigation', () => {
    it('should navigate back to passenger home', () => {
      component.handleBack();
      expect(router.navigate).toHaveBeenCalledWith(['/passenger/home']);
    });
  });

  describe('Vehicle Position', () => {
    it('should have initial vehicle position', () => {
      const position = component.vehiclePosition();
      expect(position.lat).toBeDefined();
      expect(position.lng).toBeDefined();
      expect(position.timestamp).toBeInstanceOf(Date);
    });

    it('should update vehicle position', (done) => {
      const initialPosition = component.vehiclePosition();
      
      // Trigger position update
      setTimeout(() => {
        const newPosition = component.vehiclePosition();
        expect(newPosition.lat).not.toBe(initialPosition.lat);
        done();
      }, 5500); // Wait for update interval
    });
  });

  describe('Component Cleanup', () => {
    it('should clean up map on destroy', () => {
      spyOn<any>(component, 'stopTracking');
      
      component.ngOnDestroy();
      
      expect(component['stopTracking']).toHaveBeenCalled();
    });
  });

  describe('Estimated Arrival', () => {
    it('should decrease estimated arrival time', (done) => {
      const initialTime = component.estimatedArrival();
      
      setTimeout(() => {
        const newTime = component.estimatedArrival();
        expect(newTime).toBeLessThan(initialTime);
        done();
      }, 5500);
    });

    it('should not go below 1 minute', (done) => {
      component.estimatedArrival.set(1);
      
      setTimeout(() => {
        expect(component.estimatedArrival()).toBeGreaterThanOrEqual(1);
        done();
      }, 5500);
    });
  });

  describe('Remaining Distance', () => {
    it('should decrease remaining distance', (done) => {
      const initialDistance = component.remainingDistance();
      
      setTimeout(() => {
        const newDistance = component.remainingDistance();
        expect(newDistance).toBeLessThan(initialDistance);
        done();
      }, 5500);
    });

    it('should not go below 0', (done) => {
      component.remainingDistance.set(0.05);
      
      setTimeout(() => {
        expect(component.remainingDistance()).toBeGreaterThanOrEqual(0);
        done();
      }, 5500);
    });
  });
});
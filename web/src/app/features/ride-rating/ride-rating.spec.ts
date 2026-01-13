import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RideRatingComponent } from './ride-rating';
import 'jasmine';

describe('RideRatingComponent', () => {
  let component: RideRatingComponent;
  let fixture: ComponentFixture<RideRatingComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [RideRatingComponent, ReactiveFormsModule],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RideRatingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with rating form', () => {
    expect(component.ratingForm).toBeDefined();
    expect(component.ratingForm.get('driverRating')).toBeDefined();
    expect(component.ratingForm.get('vehicleRating')).toBeDefined();
    expect(component.ratingForm.get('comment')).toBeDefined();
  });

  it('should have initial ratings set to 0', () => {
    expect(component.driverRating()).toBe(0);
    expect(component.vehicleRating()).toBe(0);
  });

  it('should calculate hours remaining correctly', () => {
    component.ngOnInit();
    expect(component.hoursRemaining()).toBeGreaterThanOrEqual(0);
    expect(component.hoursRemaining()).toBeLessThanOrEqual(72);
  });

  it('should mark as expired if more than 72 hours passed', () => {
    const oldDate = new Date(Date.now() - 80 * 60 * 60 * 1000); // 80 hours ago
    component.rideDetails.set({
      ...component.rideDetails(),
      completedAt: oldDate
    });
    
    component.ngOnInit();
    expect(component.isExpired()).toBeTruthy();
  });

  it('should not mark as expired if within 72 hours', () => {
    const recentDate = new Date(Date.now() - 24 * 60 * 60 * 1000); // 24 hours ago
    component.rideDetails.set({
      ...component.rideDetails(),
      completedAt: recentDate
    });
    
    component.ngOnInit();
    expect(component.isExpired()).toBeFalsy();
  });

  it('should update driver rating when setDriverRating is called', () => {
    component.setDriverRating(4);
    expect(component.driverRating()).toBe(4);
    expect(component.ratingForm.get('driverRating')?.value).toBe(4);
  });

  it('should update vehicle rating when setVehicleRating is called', () => {
    component.setVehicleRating(5);
    expect(component.vehicleRating()).toBe(5);
    expect(component.ratingForm.get('vehicleRating')?.value).toBe(5);
  });

  it('should not allow rating changes when expired', () => {
    component.isExpired.set(true);
    component.setDriverRating(4);
    expect(component.driverRating()).toBe(0);
  });

  it('should not allow rating changes when already submitted', () => {
    component.isSubmitted.set(true);
    component.setVehicleRating(3);
    expect(component.vehicleRating()).toBe(0);
  });

  it('should return correct rating text', () => {
    expect(component.getRatingText(1)).toBe('Poor');
    expect(component.getRatingText(2)).toBe('Fair');
    expect(component.getRatingText(3)).toBe('Good');
    expect(component.getRatingText(4)).toBe('Very Good');
    expect(component.getRatingText(5)).toBe('Excellent');
  });

  it('should validate form correctly', () => {
    expect(component.ratingForm.valid).toBeFalsy();
    
    component.setDriverRating(4);
    component.setVehicleRating(5);
    expect(component.ratingForm.valid).toBeTruthy();
  });

  it('should require minimum rating of 1', () => {
    component.ratingForm.patchValue({
      driverRating: 0,
      vehicleRating: 3
    });
    expect(component.ratingForm.get('driverRating')?.hasError('min')).toBeTruthy();
  });

  it('should enforce maximum rating of 5', () => {
    component.ratingForm.patchValue({
      driverRating: 6,
      vehicleRating: 3
    });
    expect(component.ratingForm.get('driverRating')?.hasError('max')).toBeTruthy();
  });

  it('should enforce comment max length of 500 characters', () => {
    const longComment = 'a'.repeat(501);
    component.ratingForm.patchValue({ comment: longComment });
    expect(component.ratingForm.get('comment')?.hasError('maxlength')).toBeTruthy();
  });

  it('should allow valid comment', () => {
    const validComment = 'Great ride! Very professional driver.';
    component.ratingForm.patchValue({
      driverRating: 5,
      vehicleRating: 5,
      comment: validComment
    });
    expect(component.ratingForm.valid).toBeTruthy();
  });

  it('should not submit if form is invalid', () => {
    spyOn(console, 'log');
    component.submitRating();
    expect(component.isSubmitting()).toBeFalsy();
  });

  it('should not submit if expired', () => {
    component.setDriverRating(5);
    component.setVehicleRating(5);
    component.isExpired.set(true);
    
    component.submitRating();
    expect(component.isSubmitting()).toBeFalsy();
  });

  it('should navigate back to ride history when skip is clicked', () => {
    component.skipRating();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/ride-history']);
  });

  it('should navigate back when handleBack is called', () => {
    component.handleBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/ride-history']);
  });

  it('should set submitting state during submission', (done) => {
    component.setDriverRating(4);
    component.setVehicleRating(5);
    
    component.submitRating();
    
    expect(component.isSubmitting()).toBeTruthy();
    
    setTimeout(() => {
      expect(component.isSubmitting()).toBeFalsy();
      expect(component.isSubmitted()).toBeTruthy();
      done();
    }, 2000);
  });

  it('should have correct ride details structure', () => {
    const details = component.rideDetails();
    expect(details.id).toBeDefined();
    expect(details.pickup).toBeDefined();
    expect(details.destination).toBeDefined();
    expect(details.pickupCoords).toBeDefined();
    expect(details.destinationCoords).toBeDefined();
    expect(details.driverName).toBeDefined();
    expect(details.vehicleType).toBeDefined();
    expect(details.distance).toBeGreaterThan(0);
    expect(details.duration).toBeGreaterThan(0);
    expect(details.price).toBeGreaterThan(0);
  });
});
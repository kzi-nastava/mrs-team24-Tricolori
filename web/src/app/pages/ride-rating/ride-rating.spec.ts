import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RideRatingComponent } from './ride-rating';
import { RatingService } from '../../services/rating.service';
import { RideService } from '../../services/ride.service';
import { MapService } from '../../services/map.service';
import { EstimationService } from '../../services/estimation.service';
import { provideIcons } from '@ng-icons/core';
import {
  heroArrowLeft, heroExclamationCircle, heroCheckCircle, heroMapPin,
  heroCalendar, heroStar, heroClock, heroArrowsRightLeft, heroCurrencyDollar
} from '@ng-icons/heroicons/outline';
import { heroStarSolid as heroStarSolidFill } from '@ng-icons/heroicons/solid';

describe('RideRatingComponent', () => {
  let component: RideRatingComponent;
  let fixture: ComponentFixture<RideRatingComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let mockRatingService: jasmine.SpyObj<RatingService>;
  let mockRideService: jasmine.SpyObj<RideService>;
  let mockMapService: jasmine.SpyObj<MapService>;
  let mockEstimationService: jasmine.SpyObj<EstimationService>;

  const mockRideDetails = {
    id: 1,
    pickupAddress: '123 Main St',
    dropoffAddress: '456 Oak Ave',
    startedAt: '2025-02-10T10:00:00',
    createdAt: '2025-02-10T09:30:00',
    driverName: 'John Doe',
    vehicleModel: 'Toyota Camry',
    distance: 15,
    duration: 25,
    totalPrice: 1500
  };

  const mockRatingStatus = {
    canRate: true,
    alreadyRated: false,
    deadlinePassed: false,
    deadline: '2025-02-13T10:00:00'
  };

  const mockEstimation = {
  routeGeometry: [
    { lat: 45.25, lng: 19.85 },
    { lat: 45.26, lng: 19.86 }
  ],
  distance: 15,
  duration: 25,
  price: 1500
};

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('1')
        }
      }
    };
    mockRatingService = jasmine.createSpyObj('RatingService', ['submitRating', 'getRatingStatus']);
    mockRideService = jasmine.createSpyObj('RideService', ['getPassengerRideDetail']);
    mockMapService = jasmine.createSpyObj('MapService', ['drawRoute']);
    mockEstimationService = jasmine.createSpyObj('EstimationService', ['calculateRouteFromAddress']);

    await TestBed.configureTestingModule({
      imports: [RideRatingComponent, ReactiveFormsModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: RatingService, useValue: mockRatingService },
        { provide: RideService, useValue: mockRideService },
        { provide: MapService, useValue: mockMapService },
        { provide: EstimationService, useValue: mockEstimationService },
        provideIcons({
          heroArrowLeft, heroExclamationCircle, heroCheckCircle, heroMapPin,
          heroCalendar, heroStar, heroStarSolid: heroStarSolidFill,
          heroClock, heroArrowsRightLeft, heroCurrencyDollar
        })
      ]
    }).compileComponents();

    mockRatingService.getRatingStatus.and.returnValue(of(mockRatingStatus));
    mockRideService.getPassengerRideDetail.and.returnValue(of(mockRideDetails as any));
    mockEstimationService.calculateRouteFromAddress.and.returnValue(of(mockEstimation as any));

    fixture = TestBed.createComponent(RideRatingComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should navigate to history if no ride ID in route params', () => {
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue(null);
      component.ngOnInit();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/passenger/history']);
    });

    it('should load ride data with valid ride ID', fakeAsync(() => {
      component.ngOnInit();
      tick();
      expect(mockRatingService.getRatingStatus).toHaveBeenCalledWith(1);
      expect(mockRideService.getPassengerRideDetail).toHaveBeenCalledWith(1);
      expect(component.isLoading()).toBe(false);
      expect(component.rideDetails()).toEqual(mockRideDetails);
    }));

    it('should set isSubmitted when already rated', fakeAsync(() => {
      const ratedStatus = { ...mockRatingStatus, alreadyRated: true };
      mockRatingService.getRatingStatus.and.returnValue(of(ratedStatus));
      component.ngOnInit();
      tick();
      expect(component.isSubmitted()).toBe(true);
    }));

    it('should set isExpired when deadline passed', fakeAsync(() => {
      const expiredStatus = { ...mockRatingStatus, deadlinePassed: true };
      mockRatingService.getRatingStatus.and.returnValue(of(expiredStatus));
      component.ngOnInit();
      tick();
      expect(component.isExpired()).toBe(true);
    }));

    it('should handle rating status error gracefully', fakeAsync(() => {
      mockRatingService.getRatingStatus.and.returnValue(throwError(() => new Error('API Error')));
      component.ngOnInit();
      tick();
      expect(component.isLoading()).toBe(false);
    }));

    it('should handle ride details error gracefully', fakeAsync(() => {
      mockRideService.getPassengerRideDetail.and.returnValue(throwError(() => new Error('API Error')));
      component.ngOnInit();
      tick();
      expect(component.isLoading()).toBe(false);
    }));

    it('should call map service to draw route when ride details loaded', fakeAsync(() => {
      component.ngOnInit();
      tick();
      expect(mockEstimationService.calculateRouteFromAddress).toHaveBeenCalledWith(
        '123 Main St',
        '456 Oak Ave'
      );
      expect(mockMapService.drawRoute).toHaveBeenCalledWith(mockEstimation.routeGeometry as any);
    }));

    it('should handle route estimation error gracefully', fakeAsync(() => {
      mockEstimationService.calculateRouteFromAddress.and.returnValue(throwError(() => new Error('Route Error')));
      component.ngOnInit();
      tick();
      expect(component.rideDetails()).toEqual(mockRideDetails);
    }));
  });

  describe('Form Initialization', () => {
    it('should initialize form with correct default values', () => {
      expect(component.ratingForm.value).toEqual({
        driverRating: 0,
        vehicleRating: 0,
        comment: ''
      });
    });

    it('should have required validators on driverRating', () => {
      const control = component.ratingForm.get('driverRating');
      expect(control?.hasError('required')).toBe(true);
      control?.setValue(0);
      expect(control?.hasError('min')).toBe(true);
      control?.setValue(6);
      expect(control?.hasError('max')).toBe(true);
      control?.setValue(3);
      expect(control?.valid).toBe(true);
    });

    it('should have required validators on vehicleRating', () => {
      const control = component.ratingForm.get('vehicleRating');
      expect(control?.hasError('required')).toBe(true);
      control?.setValue(0);
      expect(control?.hasError('min')).toBe(true);
      control?.setValue(6);
      expect(control?.hasError('max')).toBe(true);
      control?.setValue(4);
      expect(control?.valid).toBe(true);
    });

    it('should have maxLength validator on comment', () => {
      const control = component.ratingForm.get('comment');
      control?.setValue('a'.repeat(501));
      expect(control?.hasError('maxlength')).toBe(true);
      control?.setValue('a'.repeat(500));
      expect(control?.valid).toBe(true);
    });
  });

  describe('setDriverRating', () => {
    it('should update driver rating signal', () => {
      component.setDriverRating(4);
      expect(component.driverRating()).toBe(4);
    });

    it('should update form control value', () => {
      component.setDriverRating(5);
      expect(component.ratingForm.get('driverRating')?.value).toBe(5);
    });

    it('should accept ratings from 1 to 5', () => {
      for (let i = 1; i <= 5; i++) {
        component.setDriverRating(i);
        expect(component.driverRating()).toBe(i);
        expect(component.ratingForm.get('driverRating')?.value).toBe(i);
      }
    });
  });

  describe('setVehicleRating', () => {
    it('should update vehicle rating signal', () => {
      component.setVehicleRating(3);
      expect(component.vehicleRating()).toBe(3);
    });

    it('should update form control value', () => {
      component.setVehicleRating(2);
      expect(component.ratingForm.get('vehicleRating')?.value).toBe(2);
    });

    it('should accept ratings from 1 to 5', () => {
      for (let i = 1; i <= 5; i++) {
        component.setVehicleRating(i);
        expect(component.vehicleRating()).toBe(i);
        expect(component.ratingForm.get('vehicleRating')?.value).toBe(i);
      }
    });
  });

  describe('getRatingText', () => {
    it('should return correct text for rating 1', () => {
      expect(component.getRatingText(1)).toBe('Poor');
    });

    it('should return correct text for rating 2', () => {
      expect(component.getRatingText(2)).toBe('Fair');
    });

    it('should return correct text for rating 3', () => {
      expect(component.getRatingText(3)).toBe('Good');
    });

    it('should return correct text for rating 4', () => {
      expect(component.getRatingText(4)).toBe('Very Good');
    });

    it('should return correct text for rating 5', () => {
      expect(component.getRatingText(5)).toBe('Excellent');
    });

    it('should return empty string for rating 0', () => {
      expect(component.getRatingText(0)).toBe('');
    });

    it('should return empty string for invalid ratings', () => {
      expect(component.getRatingText(6)).toBe('');
      expect(component.getRatingText(-1)).toBe('');
    });
  });

  describe('submitRating', () => {
    beforeEach(() => {
      component.setDriverRating(4);
      component.setVehicleRating(5);
      mockRatingService.submitRating.and.returnValue(of(void 0));
    });

    it('should not submit if form is invalid', () => {
      component.ratingForm.patchValue({ driverRating: 0 });
      component.submitRating();
      expect(mockRatingService.submitRating).not.toHaveBeenCalled();
    });

    it('should not submit if already submitting', () => {
      component.isSubmitting.set(true);
      component.submitRating();
      expect(mockRatingService.submitRating).not.toHaveBeenCalled();
    });

    it('should call rating service with correct data', fakeAsync(() => {
      component.ratingForm.patchValue({ comment: 'Great ride!' });
      component.submitRating();
      tick();
      expect(mockRatingService.submitRating).toHaveBeenCalledWith(1, {
        driverRating: 4,
        vehicleRating: 5,
        comment: 'Great ride!'
      });
    }));

    it('should set isSubmitting to true during submission', () => {
      component.submitRating();
      expect(component.isSubmitting()).toBe(true);
    });

    it('should set isSubmitted to true on success', fakeAsync(() => {
      component.submitRating();
      tick();
      expect(component.isSubmitted()).toBe(true);
    }));

    it('should navigate to history after 2 seconds on success', fakeAsync(() => {
      component.submitRating();
      tick(2000);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/passenger/history']);
    }));

    it('should handle submission error', fakeAsync(() => {
      mockRatingService.submitRating.and.returnValue(throwError(() => new Error('Submit failed')));
      component.submitRating();
      tick();
      expect(component.errorMessage()).toBe('Failed to submit rating.');
      expect(component.isSubmitting()).toBe(false);
    }));

    it('should not set isSubmitted on error', fakeAsync(() => {
      mockRatingService.submitRating.and.returnValue(throwError(() => new Error('Submit failed')));
      component.submitRating();
      tick();
      expect(component.isSubmitted()).toBe(false);
    }));

    it('should submit without comment', fakeAsync(() => {
      component.submitRating();
      tick();
      expect(mockRatingService.submitRating).toHaveBeenCalledWith(1, {
        driverRating: 4,
        vehicleRating: 5,
        comment: ''
      });
    }));
  });

  describe('handleBack', () => {
    it('should navigate to passenger history', () => {
      component.handleBack();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/passenger/history']);
    });
  });

  describe('skipRating', () => {
    it('should navigate to passenger history', () => {
      component.skipRating();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/passenger/history']);
    });
  });

  describe('Signal States', () => {
    it('should initialize signals with correct default values', () => {
      expect(component.driverRating()).toBe(0);
      expect(component.vehicleRating()).toBe(0);
      expect(component.isSubmitting()).toBe(false);
      expect(component.isSubmitted()).toBe(false);
      expect(component.isExpired()).toBe(false);
      expect(component.isLoading()).toBe(true);
      expect(component.errorMessage()).toBe('');
      expect(component.rideDetails()).toBeNull();
    });
  });

  describe('Edge Cases', () => {
    it('should handle ride ID as string from route params', fakeAsync(() => {
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue('123');
      component.ngOnInit();
      tick();
      expect(mockRatingService.getRatingStatus).toHaveBeenCalledWith(123);
    }));

    it('should handle both alreadyRated and deadlinePassed true', fakeAsync(() => {
      const status = { ...mockRatingStatus, alreadyRated: true, deadlinePassed: true };
      mockRatingService.getRatingStatus.and.returnValue(of(status));
      component.ngOnInit();
      tick();
      expect(component.isSubmitted()).toBe(true);
      expect(component.isExpired()).toBe(true);
    }));

    it('should handle missing startedAt in ride details', fakeAsync(() => {
      const detailsWithoutStarted = { ...mockRideDetails, startedAt: null };
      mockRideService.getPassengerRideDetail.and.returnValue(of(detailsWithoutStarted as any));
      component.ngOnInit();
      tick();
      expect(component.rideDetails()).toEqual(detailsWithoutStarted);
    }));

    it('should handle comment with exactly 500 characters', () => {
      const maxComment = 'a'.repeat(500);
      component.ratingForm.patchValue({ comment: maxComment });
      expect(component.ratingForm.get('comment')?.valid).toBe(true);
    });

    it('should handle multiple rapid rating changes', () => {
      component.setDriverRating(1);
      component.setDriverRating(3);
      component.setDriverRating(5);
      expect(component.driverRating()).toBe(5);
      expect(component.ratingForm.get('driverRating')?.value).toBe(5);
    });

    it('should handle null estimation result', fakeAsync(() => {
      mockEstimationService.calculateRouteFromAddress.and.returnValue(of(null));
      component.ngOnInit();
      tick();
      expect(mockMapService.drawRoute).not.toHaveBeenCalled();
    }));
  });

  describe('Form Validation States', () => {
    it('should be invalid when both ratings are 0', () => {
      expect(component.ratingForm.valid).toBe(false);
    });

    it('should be valid when both ratings are set', () => {
      component.setDriverRating(3);
      component.setVehicleRating(4);
      expect(component.ratingForm.valid).toBe(true);
    });

    it('should be invalid when only driver rating is set', () => {
      component.setDriverRating(3);
      expect(component.ratingForm.valid).toBe(false);
    });

    it('should be invalid when only vehicle rating is set', () => {
      component.setVehicleRating(4);
      expect(component.ratingForm.valid).toBe(false);
    });

    it('should be valid with ratings and comment', () => {
      component.setDriverRating(5);
      component.setVehicleRating(5);
      component.ratingForm.patchValue({ comment: 'Excellent service!' });
      expect(component.ratingForm.valid).toBe(true);
    });
  });
});
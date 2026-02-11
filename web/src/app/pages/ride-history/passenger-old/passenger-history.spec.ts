import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PassengerHistory } from './passenger-history';

describe('PassengerHistory', () => {
  let component: PassengerHistory;
  let fixture: ComponentFixture<PassengerHistory>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [PassengerHistory],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(PassengerHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with all rides', () => {
    expect(component.allRides.length).toBeGreaterThan(0);
    expect(component.filteredRides.length).toBe(component.allRides.length);
  });

  it('should filter rides by start date', () => {
    component.startDate = '2024-12-14';
    component.filterByDate();
    
    const filteredCount = component.filteredRides.length;
    expect(filteredCount).toBeGreaterThan(0);
    component.filteredRides.forEach(ride => {
      expect(new Date(ride.startDate) >= new Date('2024-12-14')).toBeTruthy();
    });
  });

  it('should filter rides by end date', () => {
    component.endDate = '2024-12-13';
    component.filterByDate();
    
    component.filteredRides.forEach(ride => {
      expect(new Date(ride.startDate) <= new Date('2024-12-13')).toBeTruthy();
    });
  });

  it('should filter rides by date range', () => {
    component.startDate = '2024-12-13';
    component.endDate = '2024-12-14';
    component.filterByDate();
    
    component.filteredRides.forEach(ride => {
      const rideDate = new Date(ride.startDate);
      expect(rideDate >= new Date('2024-12-13')).toBeTruthy();
      expect(rideDate <= new Date('2024-12-14')).toBeTruthy();
    });
  });

  it('should show all rides when no date filter is applied', () => {
    component.startDate = '';
    component.endDate = '';
    component.filterByDate();
    
    expect(component.filteredRides.length).toBe(component.allRides.length);
  });

  it('should open modal with correct ride details', () => {
    const testRideId = 1;
    component.viewRideDetails(testRideId);
    
    expect(component.selectedRide).not.toBeNull();
    expect(component.selectedRide?.id).toBe(testRideId);
  });

  it('should close modal', () => {
    component.selectedRide = component.allRides[0];
    component.closeModal();
    
    expect(component.selectedRide).toBeNull();
  });

  it('should navigate to rating page with correct ride id', () => {
    const testRideId = 2;
    component.navigateToRating(testRideId);
    
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/rate-ride', testRideId]);
  });

  it('should return correct status class for completed rides', () => {
    const statusClass = component.getStatusClass('Completed');
    expect(statusClass).toBe('bg-green-100 text-green-800');
  });

  it('should return correct status class for cancelled rides', () => {
    const statusClass = component.getStatusClass('Cancelled');
    expect(statusClass).toBe('bg-red-100 text-red-800');
  });

  it('should return correct status class for pending rides', () => {
    const statusClass = component.getStatusClass('Pending');
    expect(statusClass).toBe('bg-yellow-100 text-yellow-800');
  });

  it('should calculate hours remaining correctly', () => {
    const ride = component.allRides.find(r => r.canRate && !r.ratingExpired);
    if (ride) {
      const hoursRemaining = component.getHoursRemaining(ride);
      expect(hoursRemaining).toBeGreaterThanOrEqual(0);
      expect(hoursRemaining).toBeLessThanOrEqual(72);
    }
  });

  it('should identify when rating deadline is near', () => {
    const now = new Date();
    const testRide = {
      ...component.allRides[0],
      completedAt: new Date(now.getTime() - 50 * 60 * 60 * 1000), // 50 hours ago
      canRate: true,
      ratingExpired: false
    };
    
    const isNear = component.isRatingDeadlineNear(testRide);
    expect(isNear).toBeTruthy();
  });

  it('should not show deadline warning for rides with plenty of time', () => {
    const now = new Date();
    const testRide = {
      ...component.allRides[0],
      completedAt: new Date(now.getTime() - 20 * 60 * 60 * 1000), // 20 hours ago
      canRate: true,
      ratingExpired: false
    };
    
    const isNear = component.isRatingDeadlineNear(testRide);
    expect(isNear).toBeFalsy();
  });

  it('should generate correct rating stars array', () => {
    const stars = component.getRatingStars(5);
    expect(stars.length).toBe(5);
    expect(stars).toEqual([1, 2, 3, 4, 5]);
  });

  it('should correctly determine if star should be filled', () => {
    expect(component.isStarFilled(1, 3)).toBeTruthy();
    expect(component.isStarFilled(3, 3)).toBeTruthy();
    expect(component.isStarFilled(4, 3)).toBeFalsy();
    expect(component.isStarFilled(5, 3)).toBeFalsy();
  });

  it('should have rides with different rating states', () => {
    const hasRatedRide = component.allRides.some(ride => ride.rating !== undefined);
    const hasUnratedRide = component.allRides.some(ride => ride.canRate && !ride.rating);
    const hasExpiredRide = component.allRides.some(ride => ride.ratingExpired);
    
    expect(hasRatedRide).toBeTruthy();
    expect(hasUnratedRide).toBeTruthy();
    expect(hasExpiredRide).toBeTruthy();
  });

  it('should not allow rating for cancelled rides', () => {
    const cancelledRide = component.allRides.find(ride => ride.status === 'Cancelled');
    expect(cancelledRide?.canRate).toBeFalsy();
  });

  it('should have correct ride details structure', () => {
    const ride = component.allRides[0];
    expect(ride.id).toBeDefined();
    expect(ride.route).toBeDefined();
    expect(ride.driverName).toBeDefined();
    expect(ride.vehicleType).toBeDefined();
    expect(ride.licensePlate).toBeDefined();
    expect(ride.completedAt).toBeInstanceOf(Date);
  });

  it('should handle rides with and without ratings', () => {
    const ratedRide = component.allRides.find(ride => ride.rating);
    const unratedRide = component.allRides.find(ride => !ride.rating && ride.canRate);
    
    expect(ratedRide?.rating).toBeDefined();
    expect(ratedRide?.rating?.driverRating).toBeGreaterThan(0);
    expect(unratedRide?.rating).toBeUndefined();
  });

  it('should show correct number of rides in initial state', () => {
    expect(component.filteredRides.length).toBe(5);
  });

  it('should handle empty filter results gracefully', () => {
    component.startDate = '2025-01-01';
    component.endDate = '2025-01-02';
    component.filterByDate();
    
    expect(component.filteredRides.length).toBe(0);
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AdminRideHistoryComponent } from './admin-history';

describe('AdminRideHistoryComponent', () => {
  let component: AdminRideHistoryComponent;
  let fixture: ComponentFixture<AdminRideHistoryComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AdminRideHistoryComponent],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminRideHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with all rides', () => {
    expect(component.filteredRides.length).toBe(component.allRides.length);
  });

  it('should filter rides by status', () => {
    component.selectedStatus = 'Completed';
    component.applyFilters();
    expect(component.filteredRides.every(ride => ride.status === 'Completed')).toBe(true);
  });

  it('should filter rides by date range', () => {
    component.startDate = '2025-01-15';
    component.endDate = '2025-01-17';
    component.applyFilters();
    expect(component.filteredRides.length).toBeGreaterThan(0);
  });

  it('should reset filters', () => {
    component.selectedStatus = 'Completed';
    component.searchUser = 'test';
    component.resetFilters();
    expect(component.selectedStatus).toBe('all');
    expect(component.searchUser).toBe('');
    expect(component.filteredRides.length).toBe(component.allRides.length);
  });

  it('should sort rides by field', () => {
    const initialFirst = component.filteredRides[0].id;
    component.sortBy('price');
    expect(component.sortField).toBe('price');
    expect(component.filteredRides[0].id).not.toBe(initialFirst);
  });

  it('should toggle sort direction', () => {
    component.sortBy('price');
    const firstDirection = component.sortDirection;
    component.sortBy('price');
    expect(component.sortDirection).not.toBe(firstDirection);
  });

  it('should open ride details modal', () => {
    const rideId = component.allRides[0].id;
    component.viewRideDetails(rideId);
    expect(component.selectedRide).not.toBeNull();
    expect(component.selectedRide?.id).toBe(rideId);
  });

  it('should close modal', () => {
    component.selectedRide = component.allRides[0];
    component.closeModal();
    expect(component.selectedRide).toBeNull();
  });

  it('should navigate to map view', () => {
    const ride = component.allRides[0];
    component.viewOnMap(ride);
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['/admin/map-view'],
      jasmine.objectContaining({
        queryParams: jasmine.objectContaining({
          rideId: ride.id
        })
      })
    );
  });

  it('should navigate to booking with route', () => {
    const ride = component.allRides[0];
    component.bookSameRoute(ride);
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['/admin/book-ride'],
      jasmine.objectContaining({
        queryParams: jasmine.objectContaining({
          pickup: ride.pickupLocation,
          destination: ride.destinationLocation
        })
      })
    );
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('Completed')).toContain('green');
    expect(component.getStatusClass('Cancelled')).toContain('red');
    expect(component.getStatusClass('Active')).toContain('blue');
  });

  it('should filter rides by user search', () => {
    component.searchUser = 'Marko';
    component.applyFilters();
    const hasMarko = component.filteredRides.some(ride => 
      ride.driverName.includes('Marko') || 
      ride.passengers?.some(p => p.name.includes('Marko'))
    );
    expect(hasMarko).toBe(true);
  });
});
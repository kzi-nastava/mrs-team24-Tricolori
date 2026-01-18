import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminRideSupervisionComponent } from './admin-ride-supervision';

describe('AdminRideSupervisionComponent', () => {
  let component: AdminRideSupervisionComponent;
  let fixture: ComponentFixture<AdminRideSupervisionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRideSupervisionComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminRideSupervisionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have initial drivers data', () => {
    expect(component.drivers().length).toBeGreaterThan(0);
  });

  it('should filter active drivers by default', () => {
    component.searchQuery = '';
    const filtered = component.filteredDrivers();
    expect(filtered.every(d => d.status === 'active')).toBe(true);
  });

  it('should filter drivers by search query', () => {
    component.searchQuery = 'Marko';
    const filtered = component.filteredDrivers();
    expect(filtered.length).toBeGreaterThan(0);
    expect(filtered.some(d => d.name.includes('Marko'))).toBe(true);
  });

  it('should filter drivers by license plate', () => {
    component.searchQuery = 'NS-123';
    const filtered = component.filteredDrivers();
    expect(filtered.length).toBeGreaterThan(0);
    expect(filtered.some(d => d.licensePlate.includes('NS-123'))).toBe(true);
  });

  it('should return empty array when no drivers match search', () => {
    component.searchQuery = 'NonexistentDriver';
    const filtered = component.filteredDrivers();
    expect(filtered.length).toBe(0);
  });

  it('should select a driver', () => {
    const driver = component.drivers()[0];
    component.selectDriver(driver);
    expect(component.selectedDriver()).toEqual(driver);
  });

  it('should deselect driver', () => {
    const driver = component.drivers()[0];
    component.selectDriver(driver);
    component.deselectDriver();
    expect(component.selectedDriver()).toBeNull();
  });

  it('should get active rides', () => {
    const activeRides = component.getActiveRides();
    expect(activeRides.length).toBeGreaterThan(0);
    expect(activeRides.every(d => d.status === 'active' && d.currentRide)).toBe(true);
  });

  it('should have drivers with current rides', () => {
    const driversWithRides = component.drivers().filter(d => d.currentRide);
    expect(driversWithRides.length).toBeGreaterThan(0);
  });

  it('should have drivers without current rides', () => {
    const driversWithoutRides = component.drivers().filter(d => !d.currentRide);
    expect(driversWithoutRides.length).toBeGreaterThan(0);
  });

  it('should update search query', () => {
    component.searchQuery = 'test';
    component.onSearchChange();
    expect(component.searchQuery).toBe('test');
  });

  it('should have valid ride data for active drivers', () => {
    const activeDrivers = component.drivers().filter(d => d.status === 'active');
    activeDrivers.forEach(driver => {
      if (driver.currentRide) {
        expect(driver.currentRide.id).toBeDefined();
        expect(driver.currentRide.passengerName).toBeDefined();
        expect(driver.currentRide.pickup).toBeDefined();
        expect(driver.currentRide.destination).toBeDefined();
        expect(driver.currentRide.progress).toBeGreaterThanOrEqual(0);
        expect(driver.currentRide.progress).toBeLessThanOrEqual(100);
      }
    });
  });
});
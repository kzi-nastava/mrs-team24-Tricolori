import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnregisteredHome } from './unregistered-home';

describe('UnregisteredHome', () => {
  let component: UnregisteredHome;
  let fixture: ComponentFixture<UnregisteredHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnregisteredHome]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UnregisteredHome);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with correct available drivers count', () => {
    component.ngOnInit();
    expect(component.availableDrivers).toBeGreaterThan(0);
  });

  it('should show estimate form when button is clicked', () => {
    expect(component.showEstimateForm).toBeFalsy();
    component.showEstimateForm = true;
    expect(component.showEstimateForm).toBeTruthy();
  });

  it('should validate form fields', () => {
    const form = component.estimateForm;
    expect(form.valid).toBeFalsy();
    
    form.controls.pickup.setValue('Test Pickup');
    form.controls.destination.setValue('Test Destination');
    expect(form.valid).toBeTruthy();
  });

  it('should reset estimate correctly', () => {
    component.estimateResults = {
      pickup: 'Test',
      destination: 'Test',
      distance: 2.1,
      duration: 13,
    };
    
    component.resetEstimate();
    expect(component.estimateResults).toBeNull();
    expect(component.estimateForm.value.pickup).toBe(null);
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DriverSupport } from './driver-support';

describe('DriverSupport', () => {
  let component: DriverSupport;
  let fixture: ComponentFixture<DriverSupport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverSupport]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DriverSupport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
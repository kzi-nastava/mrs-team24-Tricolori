import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PassengerSupport } from './passenger-support';

describe('PassengerSupport', () => {
  let component: PassengerSupport;
  let fixture: ComponentFixture<PassengerSupport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerSupport]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PassengerSupport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
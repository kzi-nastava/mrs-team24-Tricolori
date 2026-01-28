import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeDriver } from './driver-home';

describe('HomeDriver', () => {
  let component: HomeDriver;
  let fixture: ComponentFixture<HomeDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeDriver]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeDriver);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
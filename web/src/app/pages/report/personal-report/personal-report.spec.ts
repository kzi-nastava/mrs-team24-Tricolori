import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalReport } from './personal-report';

describe('PersonalReport', () => {
  let component: PersonalReport;
  let fixture: ComponentFixture<PersonalReport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalReport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonalReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

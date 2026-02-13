import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminReport } from './admin-report';

describe('AdminReport', () => {
  let component: AdminReport;
  let fixture: ComponentFixture<AdminReport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminReport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeRequests } from './change-requests';

describe('ChangeRequests', () => {
  let component: ChangeRequests;
  let fixture: ComponentFixture<ChangeRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeRequests]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChangeRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

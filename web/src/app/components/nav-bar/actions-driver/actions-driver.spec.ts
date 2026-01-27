import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionsDriver } from './actions-driver';

describe('ActionsDriver', () => {
  let component: ActionsDriver;
  let fixture: ComponentFixture<ActionsDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActionsDriver]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActionsDriver);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

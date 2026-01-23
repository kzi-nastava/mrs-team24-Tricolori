import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PfpPicker } from './pfp-picker';

describe('PfpPicker', () => {
  let component: PfpPicker;
  let fixture: ComponentFixture<PfpPicker>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PfpPicker]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PfpPicker);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreferencesSelector } from './preferences-selector';

describe('PreferencesSelector', () => {
  let component: PreferencesSelector;
  let fixture: ComponentFixture<PreferencesSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PreferencesSelector]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PreferencesSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

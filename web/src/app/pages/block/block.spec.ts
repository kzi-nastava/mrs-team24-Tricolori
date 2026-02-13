import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Block } from './block';

describe('Block', () => {
  let component: Block;
  let fixture: ComponentFixture<Block>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Block]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Block);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoogleRegistration } from './google-registration';

describe('GoogleRegistration', () => {
  let component: GoogleRegistration;
  let fixture: ComponentFixture<GoogleRegistration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GoogleRegistration],
    }).compileComponents();

    fixture = TestBed.createComponent(GoogleRegistration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

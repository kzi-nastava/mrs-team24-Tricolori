import { Component, effect, inject, input, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FavoriteRouteSelector } from '../favorite-route-selector/favorite-route-selector';
import { Stop, Route } from '../../../model/route';
import { HttpClient } from '@angular/common/http';
import { debounceTime, distinctUntilChanged, map, of, Subject, switchMap, takeUntil } from 'rxjs';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-route-selector',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './route-selector.html',
  styleUrl: './route-selector.css',
})

export class RouteSelector implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private destroy$ = new Subject<void>();
  readonly nsCoords = `lat=${environment.nsLat}&lon=${environment.nsLon}`

  routeForm: FormGroup;
  selectedRoute = input<Route>();

  suggestions: { [key: string]: any[] } = {};

  constructor() {
    this.routeForm = this.fb.group({
      pickup: this.createStopGroup(),
      stops: this.fb.array([]),
      destination: this.createStopGroup()
    });

    effect(() => {
      const route = this.selectedRoute();
      if (route) {
        this.routeForm.patchValue({
          pickup: route.pickup,
          destination: route.destination
        }, { emitEvent: false });

        this.populateStops(route.stops || []);
      }
    })
  }

  ngOnInit(): void {
      this.setupAutocomplete('pickup');
      this.setupAutocomplete('destination');
  }

  ngOnDestroy(): void {
      this.destroy$.next();
      this.destroy$.complete();
  }

  setupAutocomplete(controlKey: string, isStop: boolean = false) {
    const group = isStop ? this.stops.at(Number(controlKey)) : this.routeForm.get(controlKey);
    const addressControl = group?.get('address');

    addressControl?.valueChanges.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(value => {
        if (!value || typeof value !== 'string' || value.length < 3) return of([]);
        const request = `https://photon.komoot.io/api/?q=${encodeURIComponent(value)}&${this.nsCoords}&limit=5`;
        console.log("ZAHTJEV:", request)
        return this.http.get<any>(request).pipe(
          map(res => res.features)
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(results => {
      console.log("STIGLO", results);
      this.suggestions = {
        ...this.suggestions,
        [controlKey]: results
      };
    });
  }

  selectSuggestion(feature: any, controlKey: string, isStop: boolean = false) {
    const group = isStop ? this.stops.at(Number(controlKey)) : this.routeForm.get(controlKey);
    const props = feature.properties;

    let fullAddress = props.street || props.name || ''; 
    if (props.housenumber) {
      fullAddress += ` ${props.housenumber}`;
    }

    const stopData: Stop = {
      address: fullAddress.trim(),
      longitude: feature.geometry.coordinates[0],
      latitude: feature.geometry.coordinates[1]
    };

    group?.patchValue(stopData, { emitEvent: false });

    this.suggestions = {
      ...this.suggestions,
      [controlKey]: []
    };
  }

  get pickup() { return this.routeForm.get("pickup")!; }
  get destination() { return this.routeForm.get("destination")!; }
  get stops() { return this.routeForm.get('stops') as FormArray; }

  addStop() {
    const newStopGroup = this.createStopGroup();
    this.stops.push(newStopGroup);
    this.setupAutocomplete((this.stops.length - 1).toString(), true);
  }

  removeStop(index: number) {
    this.stops.removeAt(index);
    delete this.suggestions[index.toString()];
  }

  populateStops(stopsData: Stop[]) {
    this.stops.clear();
    stopsData.forEach((stop, index) => {
      this.stops.push(this.createStopGroup(stop));
      this.setupAutocomplete(index.toString(), true);
    });
  }

  private createStopGroup(stopData?: Stop): FormGroup {
    return this.fb.group({
      address: [stopData?.address || '', Validators.required],
      longitude: [stopData?.longitude || null],
      latitude: [stopData?.latitude || null]
    });
  }
}

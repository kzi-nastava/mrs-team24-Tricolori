import { Injectable } from '@angular/core';
import * as L from 'leaflet';


const pickupIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="background: #00acc1; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
  iconSize: [22, 22],
  iconAnchor: [11, 11]
});

const destinationIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="background: #ec407a; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
  iconSize: [22, 22],
  iconAnchor: [11, 11]
});


@Injectable({
  providedIn: 'root',
})
export class MapService {
  private map!: L.Map;
  private routeLayer?: L.Polyline;
  private pickupMarker?: L.Marker;
  private destinationMarker?: L.Marker;
  private vehicleMarker?: L.Marker;

  /**
   * Initializes the map on the given element
   */
  initMap(elementId: string, center: [number, number] = [45.2671, 19.8335], zoom: number = 13): L.Map {
    this.map = L.map(elementId, { center, zoom });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    return this.map;
  }

  /**
   * Draws a route with pickup and destination markers
   * @param routeGeometry - Not used for frontend calculation
   * @param pickup - Pickup coordinates [lat, lng]
   * @param destination - Destination coordinates [lat, lng]
   * @param routeCoordinates - Route coordinates from OSRM
   */
  drawRoute(
    routeGeometry: string,
    pickup: [number, number],
    destination: [number, number],
    routeCoordinates?: L.LatLng[]
  ): void {

    this.clearMap();

    // Add markers
    this.pickupMarker = L.marker(pickup, { icon: pickupIcon }).addTo(this.map);
    this.destinationMarker = L.marker(destination, { icon: destinationIcon }).addTo(this.map);

    // Draw route line if coordinates provided
    if (routeCoordinates && routeCoordinates.length > 0) {
      this.routeLayer = L.polyline(routeCoordinates, { color: '#00acc1', weight: 5, opacity: 1 }).addTo(this.map);
    }

    // Fit bounds to show all markers and route
    const bounds = L.latLngBounds([pickup, destination]);
    this.map.fitBounds(bounds.pad(0.2));
  }

  /**
   * Adds or updates vehicle marker on the map
   * @param position - Current vehicle position [lat, lng]
   * @param isPanic - Whether the vehicle is in panic mode
   */
  addOrUpdateVehicleMarker(position: [number, number], isPanic: boolean = false): void {
    const vehicleIcon = this.createVehicleIcon(isPanic);

    if (this.vehicleMarker) {
      this.vehicleMarker.setLatLng(position);
      this.vehicleMarker.setIcon(vehicleIcon);
    } else {
      this.vehicleMarker = L.marker(position, {
        icon: vehicleIcon,
        zIndexOffset: 1000
      }).addTo(this.map);
    }
  }

  /**
   * Updates vehicle marker to panic/emergency state
   */
  setVehiclePanicState(isPanic: boolean): void {
    if (this.vehicleMarker) {
      const panicIcon = this.createVehicleIcon(isPanic);
      this.vehicleMarker.setIcon(panicIcon);
    }
  }

  /**
   * Creates a vehicle icon
   * @param isPanic - Whether to create panic (red) or normal (green) icon
   */
  private createVehicleIcon(isPanic: boolean): L.DivIcon {
    const bgColor = isPanic ? '#dc2626' : '#10b981';
    const pulseAnimation = isPanic ? 'animation: pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite;' : '';

    return L.divIcon({
      className: 'vehicle-marker',
      html: `
        <style>
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
          }
        </style>
        <div style="background: ${bgColor}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; ${pulseAnimation}">
          <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="12" height="12">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
          </svg>
        </div>`,
      iconSize: [26, 26],
      iconAnchor: [13, 13]
    });
  }

  /**
   * Centers the map on given coordinates
   */
  centerMap(position: [number, number], zoom?: number): void {
    if (zoom) {
      this.map.setView(position, zoom);
    } else {
      this.map.panTo(position);
    }
  }

  /**
   * Clears all markers and routes from the map
   */
  clearMap(): void {
    if (this.pickupMarker) {
      this.map.removeLayer(this.pickupMarker);
      this.pickupMarker = undefined;
    }
    if (this.destinationMarker) {
      this.map.removeLayer(this.destinationMarker);
      this.destinationMarker = undefined;
    }
    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = undefined;
    }
    if (this.vehicleMarker) {
      this.map.removeLayer(this.vehicleMarker);
      this.vehicleMarker = undefined;
    }
  }

  /**
   * Destroys the map instance
   */
  destroyMap(): void {
    if (this.map) {
      this.clearMap();
      this.map.remove();
    }
  }

  /**
   * Gets the current map instance
   */
  getMap(): L.Map {
    return this.map;
  }
}

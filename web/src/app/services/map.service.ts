import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import * as polyline from '@mapbox/polyline';
import { Vehicle } from '../model/vehicle.model';

/**
 * Custom Icons for Route Endpoints
 */
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
  private vehicleLayer: L.LayerGroup = L.layerGroup(); // LayerGroup to manage multiple vehicle markers efficiently

  /**
   * Initializes the Leaflet map instance
   */
  initMap(elementId: string, center: [number, number] = [45.2671, 19.8335], zoom: number = 13): L.Map {

    if (this.map) {
      console.warn('Map already initialized');
      return this.map;
    }

    this.map = L.map(elementId, { center, zoom });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Add the vehicle group to the map immediately
    this.vehicleLayer.addTo(this.map);

    return this.map;
  }

  /**
   * Clears the current vehicle layer and populates it with new markers
   * Ideal for showing all available/active vehicles on a dashboard
   */
  updateVehicleMarkers(vehicles: Vehicle[]): void {
    if (!this.map) return;

    this.vehicleLayer.clearLayers();

    vehicles.forEach(vehicle => {
      const isOccupied = !vehicle.available;
      const icon = this.createVehicleIcon(isOccupied);

      const marker = L.marker([vehicle.latitude, vehicle.longitude], {
        icon: icon,
        zIndexOffset: 1000
      });

      const statusText = vehicle.available ? 'Available' : 'Occupied';
      marker.bindPopup(`
        <div style="font-family: sans-serif;">
          <strong>${vehicle.model}</strong><br>
          Plate: ${vehicle.plateNum}<br>
          Status: <span style="color: ${vehicle.available ? '#10b981' : '#dc2626'}">${statusText}</span>
        </div>
      `);

      this.vehicleLayer.addLayer(marker);
    });
  }

  private decodePolyline(encodedPolyline: string): L.LatLng[] {
    const decoded = polyline.decode(encodedPolyline);

    console.log('Decoded coordinates:', decoded);
    return decoded.map(coord => L.latLng(coord[0], coord[1]));
  }

  drawRoute(geometry: string | L.LatLng[]): void {
    if (!this.map) {
      console.error('Map not initialized');
      return;
    }

    this.clearRouteAndMarkers();

    let coordinates: L.LatLng[];

    if (typeof geometry === 'string') {
      console.log("decoding...")
      try {
        coordinates = this.decodePolyline(geometry);
        console.log(`Decoded ${coordinates.length} coordinates from polyline`);
      } catch (error) {
        console.error('Failed to decode polyline:', error);
        return;
      }
    } else {
      coordinates = geometry;
    }

    if (!coordinates || coordinates.length < 2) {
      console.error('Invalid geometry provided to drawRoute');
      return;
    }

    const pickup = coordinates[0];
    const destination = coordinates[coordinates.length - 1];

    this.pickupMarker = L.marker(pickup, { icon: pickupIcon }).addTo(this.map);
    this.destinationMarker = L.marker(destination, { icon: destinationIcon }).addTo(this.map);

    this.routeLayer = L.polyline(coordinates, {
      color: '#00acc1',
      weight: 5,
      opacity: 0.8,
      lineJoin: 'round'
    }).addTo(this.map);

    this.map.fitBounds(this.routeLayer.getBounds(), {
      padding: [50, 50]
    });
  }

  /**
   * Helper to generate consistent vehicle DivIcons
   */
  private createVehicleIcon(isAlertState: boolean): L.DivIcon {
    const bgColor = isAlertState ? '#dc2626' : '#10b981';
    const animation = isAlertState ? 'animation: pulse 1.5s infinite;' : '';

    return L.divIcon({
      className: 'vehicle-marker',
      html: `
        <style>
          @keyframes pulse {
            0% { box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.7); }
            70% { box-shadow: 0 0 0 10px rgba(220, 38, 38, 0); }
            100% { box-shadow: 0 0 0 0 rgba(220, 38, 38, 0); }
          }
        </style>
        <div style="background: ${bgColor}; width: 22px; height: 22px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 6px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; ${animation}">
          <svg xmlns="http://www.w3.org/2000/svg" fill="white" viewBox="0 0 24 24" width="14" height="14">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
          </svg>
        </div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 14]
    });
  }

  /**
   * Resets the view to specific coordinates
   */
  centerMap(position: [number, number], zoom?: number): void {
    if (!this.map) return;
    zoom ? this.map.setView(position, zoom) : this.map.panTo(position);
  }

  /**
   * Removes route-related layers but keeps the vehicle layer intact
   */
  clearRouteAndMarkers(): void {
    if (!this.map) return;
    if (this.pickupMarker) this.map.removeLayer(this.pickupMarker);
    if (this.destinationMarker) this.map.removeLayer(this.destinationMarker);
    if (this.routeLayer) this.map.removeLayer(this.routeLayer);

    this.pickupMarker = undefined;
    this.destinationMarker = undefined;
    this.routeLayer = undefined;
  }

  /**
   * Fully destroys the map instance (useful for OnDestroy)
   */
  destroyMap(): void {
    if (this.map) {
      this.vehicleLayer.clearLayers();
      this.map.remove();
      this.map = undefined as any;
    }
  }

  getMap(): L.Map {
    return this.map;
  }
}

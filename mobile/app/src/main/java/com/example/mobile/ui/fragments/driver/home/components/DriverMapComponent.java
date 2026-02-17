package com.example.mobile.ui.fragments.driver.home.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.example.mobile.R;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;
import java.util.List;

public class DriverMapComponent {
    private final MapView mapView;
    private final Context context;
    private Polyline routeOverlay;
    private Marker pickupMarker;
    private Marker destinationMarker;

    public DriverMapComponent(MapView mapView, Context context) {
        this.mapView = mapView;
        this.context = context;
    }

    public void drawRoute(String encodedPolyline) {
        try {
            ArrayList<GeoPoint> geometry = PolylineEncoder.decode(encodedPolyline, 10, false);
            drawRoute(geometry);

        } catch (Exception e) {
            Log.e("MAP_ERROR", "Error: " + e.getMessage());
        }
    }

    public void drawRoute(List<GeoPoint> geometry) {
        clearRouteAndMarkers();

        if (geometry == null || geometry.size() < 2) return;

        pickupMarker = createMarker(geometry.get(0), R.drawable.ic_start_marker, "Pickup");
        if (pickupMarker.getIcon() != null) {
            pickupMarker.getIcon().setTint(Color.parseColor("#00acc1"));
        }

        destinationMarker = createMarker(geometry.get(geometry.size() - 1), R.drawable.ic_start_marker, "Destination");
        if (destinationMarker.getIcon() != null) {
            destinationMarker.getIcon().setTint(Color.parseColor("#8B0000"));
        }

        routeOverlay = createRoutePolyline(geometry);

        mapView.getOverlays().add(routeOverlay);
        mapView.getOverlays().add(pickupMarker);
        mapView.getOverlays().add(destinationMarker);

        zoomToRoute(geometry);

        mapView.invalidate();
    }

    private Marker createMarker(GeoPoint position, int iconRes, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);

        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        if (icon != null) {
            marker.setIcon(icon.mutate());
        }
        return marker;
    }

    private Polyline createRoutePolyline(List<GeoPoint> geometry) {
        Polyline polyline = new Polyline(mapView);
        polyline.setPoints(new ArrayList<>(geometry));

        Paint paint = polyline.getOutlinePaint();
        paint.setColor(Color.parseColor("#00acc1"));
        paint.setStrokeWidth(12f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        return polyline;
    }

    private void zoomToRoute(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) return;

        BoundingBox bbox = BoundingBox.fromGeoPoints(points);

        mapView.zoomToBoundingBox(bbox, true, 200);
    }

    public void clearRouteAndMarkers() {
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
            routeOverlay = null;
        }
        if (pickupMarker != null) {
            mapView.getOverlays().remove(pickupMarker);
            pickupMarker = null;
        }
        if (destinationMarker != null) {
            mapView.getOverlays().remove(destinationMarker);
            destinationMarker = null;
        }
        mapView.invalidate();
    }
}
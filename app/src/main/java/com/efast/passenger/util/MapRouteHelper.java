package com.efast.passenger.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for drawing route polylines on Google Maps without a Directions API call.
 * Generates a smooth Bezier-style curve between two points to approximate a road route.
 * Good enough for a demo; production should use the Routes API.
 */
public final class MapRouteHelper {

    /** Number of interpolation points along the curve. */
    private static final int ROUTE_SEGMENTS = 60;

    private MapRouteHelper() { }

    // -------------------------------------------------------------------
    // Clean, modern map styling (silver/light with branded green roads)
    // -------------------------------------------------------------------

    /** Minimal JSON style that tones down POIs and transit for a ride-hailing UI. */
    private static final String MAP_STYLE_JSON = "[\n"
            + "  { \"featureType\": \"poi\", \"stylers\": [{ \"visibility\": \"off\" }] },\n"
            + "  { \"featureType\": \"transit\", \"stylers\": [{ \"visibility\": \"off\" }] },\n"
            + "  { \"featureType\": \"road\", \"elementType\": \"labels.icon\", \"stylers\": [{ \"visibility\": \"off\" }] },\n"
            + "  { \"featureType\": \"water\", \"elementType\": \"geometry.fill\", \"stylers\": [{ \"color\": \"#c9e7f2\" }] },\n"
            + "  { \"featureType\": \"landscape\", \"elementType\": \"geometry.fill\", \"stylers\": [{ \"color\": \"#f0f4f2\" }] },\n"
            + "  { \"featureType\": \"road.highway\", \"elementType\": \"geometry.fill\", \"stylers\": [{ \"color\": \"#d4e8de\" }] },\n"
            + "  { \"featureType\": \"road.arterial\", \"elementType\": \"geometry.fill\", \"stylers\": [{ \"color\": \"#e8efe9\" }] }\n"
            + "]";

    /**
     * Applies a clean, EFast-branded style to the map and disables unnecessary UI controls.
     */
    public static void applyEfastStyle(GoogleMap map) {
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(null, 0));
        } catch (Exception ignored) { /* fallback: no custom style */ }
        // Apply inline JSON style
        map.setMapStyle(new MapStyleOptions(MAP_STYLE_JSON));
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    /**
     * Disables all gestures — useful for the Home screen background map.
     */
    public static void disableGestures(GoogleMap map) {
        map.getUiSettings().setAllGesturesEnabled(false);
    }

    // -------------------------------------------------------------------
    // Route generation
    // -------------------------------------------------------------------

    /**
     * Generates a smooth curved route between two points using a quadratic Bezier curve.
     * The control point is offset perpendicular to the line, simulating a road path.
     */
    public static List<LatLng> generateRoute(LatLng from, LatLng to) {
        // Control point: midpoint offset perpendicular to the line
        double midLat = (from.latitude + to.latitude) / 2;
        double midLng = (from.longitude + to.longitude) / 2;

        double dLat = to.latitude - from.latitude;
        double dLng = to.longitude - from.longitude;
        double dist = Math.sqrt(dLat * dLat + dLng * dLng);

        // Offset perpendicular, proportional to distance (gives a natural curve)
        double offsetFactor = 0.15;
        double ctrlLat = midLat + dLng * offsetFactor;
        double ctrlLng = midLng - dLat * offsetFactor;

        List<LatLng> points = new ArrayList<>(ROUTE_SEGMENTS + 1);
        for (int i = 0; i <= ROUTE_SEGMENTS; i++) {
            double t = (double) i / ROUTE_SEGMENTS;
            double oneMinusT = 1 - t;
            double lat = oneMinusT * oneMinusT * from.latitude
                    + 2 * oneMinusT * t * ctrlLat
                    + t * t * to.latitude;
            double lng = oneMinusT * oneMinusT * from.longitude
                    + 2 * oneMinusT * t * ctrlLng
                    + t * t * to.longitude;
            points.add(new LatLng(lat, lng));
        }
        return points;
    }

    // -------------------------------------------------------------------
    // Camera helpers
    // -------------------------------------------------------------------

    /** Zooms the camera to fit both the pickup and drop markers with padding. */
    public static void fitRoute(GoogleMap map, LatLng from, LatLng to, int paddingPx) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(from);
        builder.include(to);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), paddingPx));
    }

    // -------------------------------------------------------------------
    // Marker helpers
    // -------------------------------------------------------------------

    /** Adds a colored dot marker. */
    public static void addMarker(GoogleMap map, LatLng pos, String title, float hue) {
        map.addMarker(new MarkerOptions()
                .position(pos)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
    }

    /** Adds the pickup marker (green). */
    public static void addPickupMarker(GoogleMap map, LatLng pos, String title) {
        addMarker(map, pos, title, BitmapDescriptorFactory.HUE_GREEN);
    }

    /** Adds the drop marker (red). */
    public static void addDropMarker(GoogleMap map, LatLng pos, String title) {
        addMarker(map, pos, title, BitmapDescriptorFactory.HUE_RED);
    }

    // -------------------------------------------------------------------
    // Polyline drawing
    // -------------------------------------------------------------------

    /** Draws the route polyline in EFast green. */
    public static void drawRoute(GoogleMap map, List<LatLng> route, @ColorInt int color) {
        map.addPolyline(new PolylineOptions()
                .addAll(route)
                .width(10f)
                .color(color)
                .geodesic(true));
    }

    /**
     * Returns the interpolated LatLng at fraction t (0..1) along a list of route points.
     */
    public static LatLng interpolate(List<LatLng> route, double t) {
        if (route.isEmpty()) return new LatLng(0, 0);
        if (t <= 0) return route.get(0);
        if (t >= 1) return route.get(route.size() - 1);

        double totalSegments = route.size() - 1;
        double exactIndex = t * totalSegments;
        int idx = (int) exactIndex;
        double frac = exactIndex - idx;

        if (idx >= route.size() - 1) return route.get(route.size() - 1);

        LatLng a = route.get(idx);
        LatLng b = route.get(idx + 1);
        return new LatLng(
                a.latitude + (b.latitude - a.latitude) * frac,
                a.longitude + (b.longitude - a.longitude) * frac
        );
    }
}

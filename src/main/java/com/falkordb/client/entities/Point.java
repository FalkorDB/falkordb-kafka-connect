package com.falkordb.client.entities;

import java.util.List;
import java.util.Objects;

/**
 * This class represents a (geographical) point in the graph.
 */
public final class Point {

    private static final double EPSILON = 1e-5;

    private final double latitude;
    private final double longitude;

    /**
     * @param latitude The latitude in degrees. It must be in the range [-90.0, +90.0]
     * @param longitude The longitude in degrees. It must be in the range [-180.0, +180.0]
     */
    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @param values {@code [latitude, longitude]}
     */
    public Point(List<Double> values) {
        if (values == null || values.size() != 2) {
            throw new IllegalArgumentException("Point requires two doubles.");
        }
        this.latitude = values.get(0);
        this.longitude = values.get(1);
    }

    /**
     * Get the latitude in degrees
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude in degrees
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other){
            return true;
        }
        if (!(other instanceof Point o)){
            return false;
        }
        return Math.abs(latitude - o.latitude) < EPSILON &&
                Math.abs(longitude - o.longitude) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Point{latitude=" + latitude + ", longitude=" + longitude + "}";
    }
}

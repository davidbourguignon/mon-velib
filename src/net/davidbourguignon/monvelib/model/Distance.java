package net.davidbourguignon.monvelib.model;

import java.lang.Math;

/**
 * Inspired by http://stackoverflow.com/questions/1502590/calculate-distance-between-two-points-in-google-maps-v3
 */
public class Distance {

    private static final double RADIUS = 6378.16 * 1E3; // Earth radius in meters

    // This class cannot be instantiated.
    private Distance() { }

    // Convert degrees to radians
    private static double radians(double x) {
        return x * Math.PI / 180;
    }

    // Calculate the distance between two places on Earth
    public static double between(double lon1, double lat1,
                                    double lon2, double lat2) {
        double dlon =  radians(lon2 - lon1);
        double dlat =  radians(lat2 - lat1);

        double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2)) + Math.cos(radians(lat1)) * Math.cos(radians(lat2)) * (Math.sin(dlon / 2) * Math.sin(dlon / 2));
        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (angle * RADIUS); // distance in m
    }

}

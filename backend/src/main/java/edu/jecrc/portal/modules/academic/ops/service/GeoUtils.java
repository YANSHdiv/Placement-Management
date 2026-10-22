package edu.jecrc.portal.modules.academic.ops.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoUtils() {
    }

    /**
     * Calculates the great-circle distance between two geographical points using the Haversine formula.
     * All calculations are executed strictly server-side to guarantee integrity.
     *
     * @param lat1 Latitude of point 1 in decimal degrees
     * @param lon1 Longitude of point 1 in decimal degrees
     * @param lat2 Latitude of point 2 in decimal degrees
     * @param lon2 Longitude of point 2 in decimal degrees
     * @return Distance in meters rounded to 2 decimal places, or null if any coordinate is missing
     */
    public static BigDecimal calculateHaversineDistanceMeters(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double phi1 = Math.toRadians(lat1.doubleValue());
        double phi2 = Math.toRadians(lat2.doubleValue());
        double deltaPhi = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLambda = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double sinHalfDeltaPhi = Math.sin(deltaPhi / 2.0);
        double sinHalfDeltaLambda = Math.sin(deltaLambda / 2.0);

        double a = (sinHalfDeltaPhi * sinHalfDeltaPhi)
                + Math.cos(phi1) * Math.cos(phi2) * (sinHalfDeltaLambda * sinHalfDeltaLambda);

        // Protect against rounding errors that could cause domain errors in sqrt/asin
        a = Math.min(1.0, Math.max(0.0, a));
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        double distanceMeters = EARTH_RADIUS_METERS * c;
        return BigDecimal.valueOf(distanceMeters).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Institutional Spoofing & Evidence Disclaimer:
     * Note: Server-side distance validation cross-references browser/device-reported coordinates against
     * authorized campus landmarks. Client-side location can theoretically be manipulated using mock GPS tools
     * or rooted device spoofing. Hence, high-stakes attendance events should incorporate geotagged photo proof
     * or faculty proctor roster validation.
     */
    public static final String SPOOFING_DISCLAIMER =
            "Institutional Disclaimer: Server-side distance calculations rely on device geolocation telemetry. " +
            "In environments where device-level mock location or GPS spoofing is possible, photo evidence and " +
            "faculty proctor confirmation serve as authoritative secondary factors.";
}

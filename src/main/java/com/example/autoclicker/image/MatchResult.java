package com.example.autoclicker.image;

import java.awt.Point;

/**
 * Result of template matching image detection.
 */
public class MatchResult {

    private final boolean matched;
    private final double confidence;
    private final Point location;

    public MatchResult(boolean matched, double confidence, Point location) {
        this.matched = matched;
        this.confidence = confidence;
        this.location = location != null ? new Point(location) : new Point(0, 0);
    }

    public static MatchResult notMatched(double confidence) {
        return new MatchResult(false, confidence, new Point(0, 0));
    }

    public static MatchResult matched(double confidence, Point location) {
        return new MatchResult(true, confidence, location);
    }

    public boolean isMatched() {
        return matched;
    }

    public double getConfidence() {
        return confidence;
    }

    public Point getLocation() {
        return new Point(location);
    }

    @Override
    public String toString() {
        return String.format("MatchResult[matched=%b, confidence=%.4f, location=(%d, %d)]",
                matched, confidence, location.x, location.y);
    }
}

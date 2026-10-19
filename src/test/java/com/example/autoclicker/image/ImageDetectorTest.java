package com.example.autoclicker.image;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ImageDetectorTest {

    private static ImageDetector detector;

    @BeforeAll
    static void setup() {
        detector = new ImageDetector();
    }

    @Test
    void testOpenCvTemplateMatchingExactMatch() {
        // Create a 200x200 canvas with some background pattern
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 200, 200);

        // Draw a distinct pattern (e.g. blue box with yellow circle inside) at (50, 60)
        g.setColor(Color.BLUE);
        g.fillRect(50, 60, 40, 40);
        g.setColor(Color.YELLOW);
        g.fillOval(55, 65, 30, 30);
        g.dispose();

        // Create the matching template (40x40) with the exact same pattern
        BufferedImage template = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D tg = template.createGraphics();
        tg.setColor(Color.BLUE);
        tg.fillRect(0, 0, 40, 40);
        tg.setColor(Color.YELLOW);
        tg.fillOval(5, 5, 30, 30);
        tg.dispose();

        MatchResult result = detector.match(canvas, template, 0.90);

        assertNotNull(result);
        assertTrue(result.isMatched(), "Should match the distinct pattern");
        assertTrue(result.getConfidence() >= 0.98, "Confidence should be close to 1.0, was: " + result.getConfidence());
        assertEquals(50, result.getLocation().x, "X location should be 50");
        assertEquals(60, result.getLocation().y, "Y location should be 60");
    }

    @Test
    void testOpenCvTemplateMatchingNotMatched() {
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.BLUE);
        g.fillRect(50, 60, 40, 40);
        g.dispose();

        // Distinct template that is not present in canvas
        BufferedImage template = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D tg = template.createGraphics();
        tg.setColor(Color.RED);
        tg.fillRect(0, 0, 30, 30);
        tg.setColor(Color.GREEN);
        tg.fillOval(5, 5, 20, 20);
        tg.dispose();

        MatchResult result = detector.match(canvas, template, 0.90);

        assertNotNull(result);
        assertFalse(result.isMatched(), "Should not match a red/green pattern that doesn't exist");
    }
}

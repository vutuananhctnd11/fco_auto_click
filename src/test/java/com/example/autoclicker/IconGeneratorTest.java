package com.example.autoclicker;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IconGeneratorTest {

    @Test
    void generateAppIcon() throws IOException {
        int size = 128;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();

        // High quality rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Background rounded rectangle with sleek dark gradient
        RoundRectangle2D rounded = new RoundRectangle2D.Float(4, 4, size - 8, size - 8, 28, 28);
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(15, 23, 42), size, size, new Color(30, 41, 59));
        g.setPaint(bgGradient);
        g.fill(rounded);

        // Neon border
        g.setColor(new Color(56, 189, 248, 200));
        g.setStroke(new BasicStroke(3.0f));
        g.draw(rounded);

        // Crosshair target circle
        g.setColor(new Color(14, 165, 233, 120));
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(size / 2 - 32, size / 2 - 24, 64, 64);
        g.drawOval(size / 2 - 16, size / 2 - 8, 32, 32);

        // Inner glowing dot
        g.setColor(new Color(244, 63, 94));
        g.fillOval(size / 2 - 5, size / 2 + 3, 10, 10);

        // Target cross lines
        g.setColor(new Color(56, 189, 248, 160));
        g.drawLine(size / 2 - 38, size / 2 + 8, size / 2 + 38, size / 2 + 8);
        g.drawLine(size / 2, size / 2 - 30, size / 2, size / 2 + 46);

        // Bold "FCO" Title text on top
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.setColor(new Color(248, 250, 252));
        FontMetrics fm = g.getFontMetrics();
        String text = "FCO";
        int textX = (size - fm.stringWidth(text)) / 2;
        g.drawString(text, textX, 36);

        // Bottom "CLICK" text badge
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.setColor(new Color(56, 189, 248));
        FontMetrics fm2 = g.getFontMetrics();
        String text2 = "AUTOCLICK";
        int textX2 = (size - fm2.stringWidth(text2)) / 2;
        g.drawString(text2, textX2, 114);

        g.dispose();

        File iconDir = new File("src/main/resources/icons");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        File targetFile = new File(iconDir, "app-icon.png");
        ImageIO.write(icon, "png", targetFile);

        assertTrue(targetFile.exists());
    }
}

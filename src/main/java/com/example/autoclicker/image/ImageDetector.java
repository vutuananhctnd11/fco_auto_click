package com.example.autoclicker.image;

import com.example.autoclicker.util.LoggerUtil;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 * Performs template matching using OpenCV.
 */
public class ImageDetector {

    private static volatile boolean openCvLoaded = false;

    static {
        loadOpenCvLibrary();
    }

    public static synchronized void loadOpenCvLibrary() {
        if (!openCvLoaded) {
            try {
                OpenCV.loadLocally();
                openCvLoaded = true;
                LoggerUtil.info("OpenCV native library loaded successfully.");
            } catch (Throwable t) {
                LoggerUtil.error("Failed to load OpenCV via OpenPnP, attempting System.loadLibrary: " + t.getMessage());
                try {
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    openCvLoaded = true;
                    LoggerUtil.info("OpenCV loaded via System.loadLibrary.");
                } catch (Throwable t2) {
                    LoggerUtil.error("Critical: Could not load OpenCV native libraries.", t2);
                    throw new RuntimeException("OpenCV initialization failed", t2);
                }
            }
        }
    }

    /**
     * Finds if template matches within the screenshot above or equal to the specified threshold.
     *
     * @param screenshot Source image
     * @param template   Template image to find
     * @param threshold  Confidence threshold (e.g., 0.90)
     * @return MatchResult containing match status, confidence score, and best match location
     */
    public MatchResult match(BufferedImage screenshot, BufferedImage template, double threshold) {
        if (screenshot == null || template == null) {
            throw new IllegalArgumentException("Screenshot and template cannot be null");
        }

        if (template.getWidth() > screenshot.getWidth() || template.getHeight() > screenshot.getHeight()) {
            LoggerUtil.warn(String.format("Template size (%dx%d) is larger than screenshot (%dx%d)",
                    template.getWidth(), template.getHeight(), screenshot.getWidth(), screenshot.getHeight()));
            return MatchResult.notMatched(0.0);
        }

        Mat srcMat = bufferedImageToMat(screenshot);
        Mat tplMat = bufferedImageToMat(template);
        Mat resultMat = new Mat();

        try {
            Imgproc.matchTemplate(srcMat, tplMat, resultMat, Imgproc.TM_CCOEFF_NORMED);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);

            double confidence = mmr.maxVal;
            // Clean edge cases like NaN or negative values
            if (Double.isNaN(confidence) || Double.isInfinite(confidence)) {
                confidence = 0.0;
            }

            boolean isMatched = confidence >= threshold;
            Point matchPoint = new Point((int) mmr.maxLoc.x, (int) mmr.maxLoc.y);

            return new MatchResult(isMatched, confidence, matchPoint);
        } finally {
            srcMat.release();
            tplMat.release();
            resultMat.release();
        }
    }

    /**
     * Loads a template image from disk.
     */
    public BufferedImage loadTemplate(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Template file path cannot be empty");
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("Template file does not exist: " + filePath);
        }
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unsupported image format for file: " + filePath);
        }
        return image;
    }

    /**
     * Converts a Java BufferedImage to an OpenCV Mat (CV_8UC3 BGR).
     */
    public static Mat bufferedImageToMat(BufferedImage bi) {
        BufferedImage converted;
        if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            converted = bi;
        } else {
            converted = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g2d = converted.createGraphics();
            try {
                g2d.drawImage(bi, 0, 0, null);
            } finally {
                g2d.dispose();
            }
        }

        byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(converted.getHeight(), converted.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }
}

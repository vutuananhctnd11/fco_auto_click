package com.example.autoclicker.config;

import com.example.autoclicker.automation.AutomationAction;
import com.example.autoclicker.automation.AutomationActionType;
import com.example.autoclicker.screen.CaptureRegion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonConfigRepositoryTest {

    @Test
    void testSaveAndLoadConfig(@TempDir Path tempDir) throws IOException {
        JsonConfigRepository repo = new JsonConfigRepository();

        AutomationConfig config = new AutomationConfig();
        config.setTargetScreen(1);
        config.setTargetWindowTitle("FCO Game Window");
        config.setCoordinateMode(com.example.autoclicker.automation.CoordinateMode.WINDOW_RELATIVE);
        config.setCaptureRegion(new CaptureRegion(150, 250, 400, 300, com.example.autoclicker.automation.CoordinateMode.WINDOW_RELATIVE));
        config.setTemplatePath("templates/sample.png");
        config.setThreshold(0.88);

        AutomationAction initialAction = new AutomationAction(AutomationActionType.CLICK, 100, 100, 200, com.example.autoclicker.automation.CoordinateMode.WINDOW_RELATIVE);
        AutomationAction checkAction = new AutomationAction();
        checkAction.setType(AutomationActionType.CHECK_IMAGE);
        checkAction.setX(100);
        checkAction.setY(200);
        checkAction.setWidth(400);
        checkAction.setHeight(300);
        checkAction.setTemplatePath("templates/sample.png");
        checkAction.setThreshold(0.92);

        AutomationAction action1 = new AutomationAction(AutomationActionType.CLICK, 500, 600, 450, com.example.autoclicker.automation.CoordinateMode.WINDOW_RELATIVE);
        AutomationAction action2 = new AutomationAction(AutomationActionType.KEY_PRESS, "ESC", 200);

        config.setInitialActions(List.of(initialAction, checkAction));
        config.setBranchA(List.of(action1));
        config.setBranchB(List.of(action2));

        File file = tempDir.resolve("test-config.json").toFile();
        repo.save(config, file);

        assertTrue(file.exists());

        AutomationConfig loaded = repo.load(file);
        assertNotNull(loaded);
        assertEquals(1, loaded.getTargetScreen());
        assertEquals("FCO Game Window", loaded.getTargetWindowTitle());
        assertEquals(com.example.autoclicker.automation.CoordinateMode.WINDOW_RELATIVE, loaded.getCoordinateMode());
        assertEquals("templates/sample.png", loaded.getTemplatePath());

        assertEquals(2, loaded.getInitialActions().size());
        assertEquals(AutomationActionType.CLICK, loaded.getInitialActions().get(0).getType());
        assertEquals(AutomationActionType.CHECK_IMAGE, loaded.getInitialActions().get(1).getType());
        assertEquals("templates/sample.png", loaded.getInitialActions().get(1).getTemplatePath());
        assertEquals(0.92, loaded.getInitialActions().get(1).getThreshold(), 0.001);
        assertEquals(400, loaded.getInitialActions().get(1).getWidth());
        assertEquals(300, loaded.getInitialActions().get(1).getHeight());

        assertEquals(1, loaded.getBranchA().size());
        assertEquals(AutomationActionType.CLICK, loaded.getBranchA().get(0).getType());
        assertEquals(500, loaded.getBranchA().get(0).getX());

        assertEquals(1, loaded.getBranchB().size());
        assertEquals(AutomationActionType.KEY_PRESS, loaded.getBranchB().get(0).getType());
        assertEquals("ESC", loaded.getBranchB().get(0).getKeyName());
    }

    @Test
    void createSampleTemplateImage() throws IOException {
        File dir = new File("templates");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File sampleFile = new File(dir, "sample-template.png");

        BufferedImage img = new BufferedImage(80, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(24, 119, 242));
        g.fillRect(0, 0, 80, 32);
        g.setColor(Color.WHITE);
        g.drawRect(1, 1, 77, 29);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("FCO", 24, 21);
        g.dispose();

        ImageIO.write(img, "png", sampleFile);
        assertTrue(sampleFile.exists());
    }
}

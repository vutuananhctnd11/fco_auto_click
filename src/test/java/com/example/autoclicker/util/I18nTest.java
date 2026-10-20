package com.example.autoclicker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    @Test
    void testEnglishTranslations() {
        I18n.setLocale(I18n.LOCALE_EN);
        assertEquals(I18n.LOCALE_EN, I18n.getLocale());
        assertEquals("FCOAutoClick", I18n.get("app.title"));
        assertTrue(I18n.get("app.author").contains("TunEngDev2003"));
        assertTrue(I18n.get("app.copyright").contains("TunEngDev2003"));
        assertEquals("START (F8)", I18n.get("btn.start"));
    }

    @Test
    void testVietnameseTranslations() {
        I18n.setLocale(I18n.LOCALE_VI);
        assertEquals(I18n.LOCALE_VI, I18n.getLocale());
        assertEquals("FCOAutoClick", I18n.get("app.title"));
        assertTrue(I18n.get("app.author").contains("TunEngDev2003"));
        assertTrue(I18n.get("app.copyright").contains("TunEngDev2003"));
        assertEquals("BẮT ĐẦU (F8)", I18n.get("btn.start"));
        assertEquals("DỪNG (F9)", I18n.get("btn.stop"));
    }

    @Test
    void testMessageFormatting() {
        I18n.setLocale(I18n.LOCALE_EN);
        String formatted = I18n.format("lbl.confidence", "0.95");
        assertEquals("Confidence: 0.95", formatted);

        I18n.setLocale(I18n.LOCALE_VI);
        String formattedVi = I18n.format("lbl.confidence", "0.95");
        assertEquals("Độ tin cậy: 0.95", formattedVi);
    }

    @Test
    void testActionTypesAndCellFormatting() {
        I18n.setLocale(I18n.LOCALE_VI);
        assertTrue(I18n.get("action.type.click").contains("Click chuột trái"));
        assertTrue(I18n.get("action.type.check_image").contains("Kiểm tra hình ảnh"));
        assertTrue(I18n.get("action.type.stop").contains("Dừng kịch bản"));
        assertEquals("CLICK", I18n.get("badge.action.click"));
        assertEquals("CHECK_IMAGE", I18n.get("badge.action.check_image"));
        assertEquals("STOP", I18n.get("badge.action.stop"));

        String cellRegionVi = I18n.get("cell.region", 300, 200, 100, 150);
        assertEquals("Vùng: 300x200 tại (100, 150)", cellRegionVi);

        I18n.setLocale(I18n.LOCALE_EN);
        assertTrue(I18n.get("action.type.click").contains("Left Click"));
        assertTrue(I18n.get("action.type.check_image").contains("Check Image"));
        assertTrue(I18n.get("action.type.stop").contains("Stop Script"));
        assertEquals("CHECK_IMAGE", I18n.get("badge.action.check_image"));
        assertEquals("STOP", I18n.get("badge.action.stop"));

        String cellRegionEn = I18n.get("cell.region", 300, 200, 100, 150);
        assertEquals("Region: 300x200 at (100, 150)", cellRegionEn);
    }
}

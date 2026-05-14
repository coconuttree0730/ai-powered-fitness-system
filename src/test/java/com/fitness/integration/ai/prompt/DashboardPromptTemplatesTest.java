package com.fitness.integration.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardPromptTemplatesTest {

    private final DashboardPromptTemplates promptTemplates = new DashboardPromptTemplates();

    @Test
    void createOverallAnalysisPromptShouldSeparateSystemAndUserContent() {
        AiPromptSpec prompt = promptTemplates.createOverallAnalysisPrompt(Map.ofEntries(
                Map.entry("totalMembers", 120),
                Map.entry("activeMembers", 80),
                Map.entry("activeRate", "66.7"),
                Map.entry("totalCourses", 16),
                Map.entry("totalBookings", 320),
                Map.entry("avgBookingPerCourse", "20.0"),
                Map.entry("totalEquipment", 40),
                Map.entry("normalEquipment", 35),
                Map.entry("maintenanceEquipment", 2),
                Map.entry("repairEquipment", 2),
                Map.entry("offlineEquipment", 1),
                Map.entry("equipmentGoodRate", "87.5"),
                Map.entry("todayOrders", 18),
                Map.entry("todayRevenue", "1888.00"),
                Map.entry("peakHoursData", "- 18:00 20 visits"),
                Map.entry("courseStatsData", "- strength 8 courses"),
                Map.entry("revenueTrendData", "- 05-14: 1888.00"),
                Map.entry("userGrowthData", "- 05-14: +6"),
                Map.entry("repairStatsData", "- pending: 2"),
                Map.entry("memberCardStatsData", "- month card: 50")
        ));

        assertFalse(prompt.system().isBlank());
        assertTrue(prompt.system().length() > 30);
        assertTrue(prompt.user().contains("120"));
        assertTrue(prompt.user().contains("strength 8 courses"));
        assertTrue(prompt.user().contains("5."));
    }
}

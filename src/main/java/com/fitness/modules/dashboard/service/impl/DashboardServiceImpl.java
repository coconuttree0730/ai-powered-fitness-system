package com.fitness.modules.dashboard.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.prompt.DashboardPromptTemplates;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.dashboard.mapper.DashboardMapper;
import com.fitness.modules.dashboard.model.enums.AnalysisType;
import com.fitness.modules.dashboard.model.vo.AnalysisReportVO;
import com.fitness.modules.dashboard.model.vo.CourseStatsVO;
import com.fitness.modules.dashboard.model.vo.DashboardStatsVO;
import com.fitness.modules.dashboard.model.vo.EquipmentStatusVO;
import com.fitness.modules.dashboard.model.vo.MemberCardStatsVO;
import com.fitness.modules.dashboard.model.vo.PeakHoursVO;
import com.fitness.modules.dashboard.model.vo.RepairStatsVO;
import com.fitness.modules.dashboard.model.vo.RevenueTrendVO;
import com.fitness.modules.dashboard.model.vo.UserGrowthVO;
import com.fitness.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 仪表盘服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final AIService aiService;
    private final DashboardPromptTemplates dashboardPromptTemplates;
    private final Random random = new Random();

    @Override
    public DashboardStatsVO getDashboardStats() {
        log.info("获取仪表盘统计数据");

        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setTotalMembers(dashboardMapper.countTotalMembers());
        stats.setActiveMembers(dashboardMapper.countActiveMembers());
        stats.setTotalCourses(dashboardMapper.countTotalCourses());
        stats.setTotalBookings(dashboardMapper.countTotalBookings());
        stats.setTotalEquipment(dashboardMapper.countTotalEquipment());
        stats.setTodayOrders(dashboardMapper.countTodayOrders());
        
        BigDecimal todayRevenue = dashboardMapper.sumTodayRevenue();
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public MemberCardStatsVO getMemberCardStats() {
        log.info("获取会员卡销量统计（模拟数据）");
        //TODO 当前还是 模拟数据，未开发
        MemberCardStatsVO stats = new MemberCardStatsVO();
        stats.setMonthCard(random.nextInt(51) + 50);   // 50-100
        stats.setQuarterCard(random.nextInt(31) + 30); // 30-60
        stats.setYearCard(random.nextInt(21) + 10);    // 10-30

        return stats;
    }

    @Override
    public List<PeakHoursVO> getPeakHours() {
        log.info("获取到店高峰时间统计");
        return dashboardMapper.selectPeakHours();
    }

    @Override
    public List<CourseStatsVO> getCourseStats() {
        log.info("获取课程统计");
        return dashboardMapper.selectCourseStats();
    }

    @Override
    public AnalysisReportVO generateAnalysisReport(String analysisType) {
        log.info("生成AI分析报告，分析类型: {}", analysisType);

        try {
            // 解析分析类型
            AnalysisType type = AnalysisType.fromCode(analysisType);

            // 收集运营数据
            Map<String, Object> variables = collectAnalysisData(type);
            log.info("收集到的分析数据变量: {}", variables.keySet());

            // 构建分析 Prompt
            String prompt = buildAnalysisPrompt(type, variables);
            log.info("生成的Prompt内容:\n{}", prompt);

            // 调用 AI 生成报告
            String aiResponse = aiService.chat(prompt);
            log.info("AI返回的原始响应:\n{}", aiResponse);

            // 解析 AI 返回结果
            AnalysisReportVO report = parseAIResponse(type, aiResponse);
            log.info("解析后的报告标题: {}, 建议数量: {}", report.getReportTitle(), 
                    report.getSuggestions() != null ? report.getSuggestions().size() : 0);
            
            return report;
        } catch (Exception e) {
            log.error("生成AI分析报告失败", e);
            throw new BusinessException(ErrorCode.ANALYSIS_ERROR);
        }
    }

    @Override
    public List<RevenueTrendVO> getRevenueTrend(String range) {
        log.info("获取营收趋势数据，时间范围: {}", range);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        String groupBy;
        
        switch (range) {
            case "today":
                startDate = endDate;
                groupBy = "hour";
                break;
            case "week":
                startDate = endDate.minusDays(6);
                groupBy = "day";
                break;
            case "month":
                startDate = endDate.minusDays(29);
                groupBy = "day";
                break;
            case "year":
                startDate = endDate.minusDays(364);
                groupBy = "day";
                break;
            default:
                startDate = endDate.minusDays(6);
                groupBy = "day";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<RevenueTrendVO> data = dashboardMapper.selectRevenueTrend(
            startDate.format(formatter), 
            endDate.format(formatter), 
            groupBy
        );
        
        // 如果是今日数据，需要填充没有数据的小时
        if ("today".equals(range) && groupBy.equals("hour")) {
            data = fillTodayRevenueHours(data);
        }
        
        return data;
    }

    @Override
    public List<UserGrowthVO> getUserGrowth(String range) {
        log.info("获取用户增长趋势，时间范围: {}", range);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (range) {
            case "today":
                startDate = endDate;
                break;
            case "week":
                startDate = endDate.minusDays(6);
                break;
            case "month":
                startDate = endDate.minusDays(29);
                break;
            case "year":
                startDate = endDate.minusDays(364);
                break;
            default:
                startDate = endDate.minusDays(6);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<UserGrowthVO> data = dashboardMapper.selectUserGrowth(
            startDate.format(formatter), 
            endDate.format(formatter)
        );
        
        // 填充没有数据的日期
        return fillMissingDates(data, startDate, endDate);
    }

    @Override
    public EquipmentStatusVO getEquipmentStatus() {
        log.info("获取器材使用状态统计");
        
        EquipmentStatusVO status = new EquipmentStatusVO();
        status.setNormal(dashboardMapper.countNormalEquipment());
        status.setMaintenance(dashboardMapper.countMaintenanceEquipment());
        status.setRepair(dashboardMapper.countRepairEquipment());
        status.setOffline(dashboardMapper.countOfflineEquipment());
        
        return status;
    }

    @Override
    public List<RepairStatsVO> getRepairStats() {
        log.info("获取报修处理统计");
        return dashboardMapper.selectRepairStats();
    }

    /**
     * 填充今日24小时的营收数据
     */
    private List<RevenueTrendVO> fillTodayRevenueHours(List<RevenueTrendVO> data) {
        Map<String, RevenueTrendVO> dataMap = new HashMap<>();
        for (RevenueTrendVO vo : data) {
            dataMap.put(vo.getDate(), vo);
        }
        
        List<RevenueTrendVO> filledData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d:00", i);
            RevenueTrendVO vo = dataMap.get(hour);
            if (vo == null) {
                vo = new RevenueTrendVO();
                vo.setDate(hour);
                vo.setAmount(BigDecimal.ZERO);
            }
            filledData.add(vo);
        }
        return filledData;
    }

    /**
     * 填充缺失的日期数据
     */
    private List<UserGrowthVO> fillMissingDates(List<UserGrowthVO> data, LocalDate startDate, LocalDate endDate) {
        Map<String, UserGrowthVO> dataMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (UserGrowthVO vo : data) {
            dataMap.put(vo.getDate(), vo);
        }
        
        List<UserGrowthVO> filledData = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            UserGrowthVO vo = dataMap.get(dateStr);
            if (vo == null) {
                vo = new UserGrowthVO();
                vo.setDate(dateStr);
                vo.setCount(0);
            }
            filledData.add(vo);
            current = current.plusDays(1);
        }
        return filledData;
    }

    /**
     * 收集分析数据
     */
    private Map<String, Object> collectAnalysisData(AnalysisType type) {
        Map<String, Object> variables = new HashMap<>();

        // 会员数据
        Integer totalMembers = dashboardMapper.countTotalMembers();
        Integer activeMembers = dashboardMapper.countActiveMembers();
        double activeRate = totalMembers > 0 ? (activeMembers * 100.0 / totalMembers) : 0;
        variables.put("totalMembers", totalMembers);
        variables.put("activeMembers", activeMembers);
        variables.put("activeRate", String.format("%.1f", activeRate));

        // 课程数据
        Integer totalCourses = dashboardMapper.countTotalCourses();
        Integer totalBookings = dashboardMapper.countTotalBookings();
        double avgBookingPerCourse = totalCourses > 0 ? (totalBookings * 1.0 / totalCourses) : 0;
        variables.put("totalCourses", totalCourses);
        variables.put("totalBookings", totalBookings);
        variables.put("avgBookingPerCourse", String.format("%.1f", avgBookingPerCourse));

        // 器材数据
        Integer totalEquipment = dashboardMapper.countTotalEquipment();
        Integer normalEquipment = dashboardMapper.countNormalEquipment();
        Integer maintenanceEquipment = dashboardMapper.countMaintenanceEquipment();
        Integer repairEquipment = dashboardMapper.countRepairEquipment();
        Integer offlineEquipment = dashboardMapper.countOfflineEquipment();
        double equipmentGoodRate = totalEquipment > 0 ? (normalEquipment * 100.0 / totalEquipment) : 0;
        variables.put("totalEquipment", totalEquipment);
        variables.put("normalEquipment", normalEquipment);
        variables.put("maintenanceEquipment", maintenanceEquipment);
        variables.put("repairEquipment", repairEquipment);
        variables.put("offlineEquipment", offlineEquipment);
        variables.put("equipmentGoodRate", String.format("%.1f", equipmentGoodRate));

        // 到店高峰时间
        List<PeakHoursVO> peakHours = dashboardMapper.selectPeakHours();
        variables.put("peakHoursData", formatPeakHours(peakHours));

        // 课程分类统计
        List<CourseStatsVO> courseStats = dashboardMapper.selectCourseStats();
        variables.put("courseStatsData", formatCourseStats(courseStats));

        // 营收趋势数据（本周）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<RevenueTrendVO> revenueTrend = dashboardMapper.selectRevenueTrend(
            startDate.format(formatter), 
            endDate.format(formatter), 
            "day"
        );
        variables.put("revenueTrendData", formatRevenueTrend(revenueTrend));

        // 用户增长趋势（本周）
        List<UserGrowthVO> userGrowth = dashboardMapper.selectUserGrowth(
            startDate.format(formatter), 
            endDate.format(formatter)
        );
        variables.put("userGrowthData", formatUserGrowth(userGrowth));

        // 报修处理统计
        List<RepairStatsVO> repairStats = dashboardMapper.selectRepairStats();
        variables.put("repairStatsData", formatRepairStats(repairStats));

        // 会员卡销售统计（模拟数据）
        MemberCardStatsVO memberCardStats = getMemberCardStats();
        variables.put("memberCardStatsData", formatMemberCardStats(memberCardStats));

        // 今日数据
        Integer todayOrders = dashboardMapper.countTodayOrders();
        BigDecimal todayRevenue = dashboardMapper.sumTodayRevenue();
        variables.put("todayOrders", todayOrders);
        variables.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        return variables;
    }

    /**
     * 构建分析 Prompt
     */
    private String buildAnalysisPrompt(AnalysisType type, Map<String, Object> variables) {
        return switch (type) {
            case MEMBER -> dashboardPromptTemplates.generateMemberAnalysis(variables);
            case COURSE -> dashboardPromptTemplates.generateCourseAnalysis(variables);
            case EQUIPMENT -> dashboardPromptTemplates.generateEquipmentAnalysis(variables);
            default -> dashboardPromptTemplates.generateOverallAnalysis(variables);
        };
    }

    /**
     * 解析 AI 返回结果
     */
    private AnalysisReportVO parseAIResponse(AnalysisType type, String aiResponse) {
        List<String> suggestions = extractSuggestions(aiResponse);

        return AnalysisReportVO.builder()
                .analysisType(type.getCode())
                .reportTitle(type.getDescription() + "报告")
                .reportContent(aiResponse)
                .suggestions(suggestions)
                .generateTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 从 AI 响应中提取建议列表
     * 提取 "### 5. 运营综合建议" 或类似章节后的内容
     */
    private List<String> extractSuggestions(String content) {
        List<String> suggestions = new ArrayList<>();

        // 尝试匹配 "运营综合建议" 或 "优化建议" 章节后的内容
        // 匹配模式：从 "### 5. 运营综合建议" 或 "## 5. 运营综合建议" 开始，到下一个 ## 标题或文档结束
        Pattern sectionPattern = Pattern.compile(
            "(?:#{1,3}\\s*(?:5\\.\\s*)?(?:运营综合建议|优化建议|改进措施|建议).*?)(?:\\r?\\n)(.*?)(?=(?:\\r?\\n#{1,3}\\s*\\d+\\.|\\z))",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String suggestionsSection = sectionMatcher.group(1).trim();

            // 从建议章节中提取具体的建议项
            // 匹配模式：以数字或 - 开头的列表项
            Pattern itemPattern = Pattern.compile(
                "(?:^|\\n)\\s*(?:\\d+[.、]\\s*|-\\s*|•\\s*)([^\\n]+)"
            );
            Matcher itemMatcher = itemPattern.matcher(suggestionsSection);

            while (itemMatcher.find()) {
                String suggestion = itemMatcher.group(1).trim();
                // 过滤掉标题行（如 "短期改进措施"、"中期优化计划" 等）
                if (!suggestion.isEmpty() &&
                    !suggestion.matches(".*?(?:短期|中期|长期|措施|计划|战略).*?") &&
                    suggestion.length() > 10) {
                    suggestions.add(suggestion);
                }
            }
        }

        // 如果没有提取到建议，尝试备用方案：提取所有包含 "建议" 关键词的段落
        if (suggestions.isEmpty()) {
            Pattern fallbackPattern = Pattern.compile(
                "(?:^|\\n)\\s*(?:\\d+[.、]\\s*|•\\s*|-\\s*)([^\\n]{10,200})"
            );
            Matcher fallbackMatcher = fallbackPattern.matcher(content);
            int count = 0;
            while (fallbackMatcher.find() && count < 8) {
                String suggestion = fallbackMatcher.group(1).trim();
                if (!suggestion.isEmpty()) {
                    suggestions.add(suggestion);
                    count++;
                }
            }
        }

        // 如果还是没有提取到，返回默认提示
        if (suggestions.isEmpty()) {
            suggestions.add("请参考报告内容中的建议部分");
        }

        log.info("提取到 {} 条建议", suggestions.size());
        return suggestions;
    }

    /**
     * 格式化高峰时间数据
     */
    private String formatPeakHours(List<PeakHoursVO> peakHours) {
        if (peakHours == null || peakHours.isEmpty()) {
            return "暂无数据";
        }

        StringBuilder sb = new StringBuilder();
        for (PeakHoursVO vo : peakHours) {
            sb.append(String.format("- %d点: %d人次\n", vo.getHour(), vo.getCount()));
        }
        return sb.toString();
    }

    /**
     * 格式化课程统计数据
     */
    private String formatCourseStats(List<CourseStatsVO> courseStats) {
        if (courseStats == null || courseStats.isEmpty()) {
            return "暂无数据";
        }

        StringBuilder sb = new StringBuilder();
        for (CourseStatsVO vo : courseStats) {
            sb.append(String.format("- %s: 课程数%d, 预约数%d\n",
                    vo.getCategoryName(), vo.getCourseCount(), vo.getBookingCount()));
        }
        return sb.toString();
    }

    /**
     * 格式化营收趋势数据
     */
    private String formatRevenueTrend(List<RevenueTrendVO> revenueTrend) {
        if (revenueTrend == null || revenueTrend.isEmpty()) {
            return "暂无数据";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("本周营收趋势：\n");
        for (RevenueTrendVO vo : revenueTrend) {
            sb.append(String.format("- %s: ¥%.2f\n", vo.getDate(), vo.getAmount()));
        }
        return sb.toString();
    }

    /**
     * 格式化用户增长数据
     */
    private String formatUserGrowth(List<UserGrowthVO> userGrowth) {
        if (userGrowth == null || userGrowth.isEmpty()) {
            return "暂无数据";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("本周用户增长趋势：\n");
        for (UserGrowthVO vo : userGrowth) {
            sb.append(String.format("- %s: 新增%d人\n", vo.getDate(), vo.getCount()));
        }
        return sb.toString();
    }

    /**
     * 格式化报修处理统计数据
     */
    private String formatRepairStats(List<RepairStatsVO> repairStats) {
        if (repairStats == null || repairStats.isEmpty()) {
            return "暂无数据";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("报修处理统计：\n");
        for (RepairStatsVO vo : repairStats) {
            sb.append(String.format("- %s: %d件\n", vo.getStatus(), vo.getCount()));
        }
        return sb.toString();
    }

    /**
     * 格式化会员卡销售统计数据
     */
    private String formatMemberCardStats(MemberCardStatsVO memberCardStats) {
        if (memberCardStats == null) {
            return "暂无数据";
        }

        return String.format("会员卡销售统计（模拟数据）：\n- 月卡: %d张\n- 季卡: %d张\n- 年卡: %d张\n",
                memberCardStats.getMonthCard(),
                memberCardStats.getQuarterCard(),
                memberCardStats.getYearCard());
    }
}

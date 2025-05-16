package com.hostel.management.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class UtilityCalculator {

    // Bậc thang giá điện (kWh, VNĐ)
    private static final LinkedHashMap<Integer, Double> ELECTRIC_TIERS = new LinkedHashMap<>() {{
        put(50, 1678.0);    // 0-50 kWh: 1,678 đồng/kWh
        put(100, 1734.0);   // 51-100 kWh: 1,734 đồng/kWh
        put(200, 2014.0);   // 101-200 kWh: 2,014 đồng/kWh
        put(300, 2536.0);   // 201-300 kWh: 2,536 đồng/kWh
        put(400, 2834.0);   // 301-400 kWh: 2,834 đồng/kWh
        put(Integer.MAX_VALUE, 2927.0); // Trên 400 kWh: 2,927 đồng/kWh
    }};

    // Bậc thang giá nước (m³, VNĐ)
    private static final LinkedHashMap<Integer, Double> WATER_TIERS = new LinkedHashMap<>() {{
        put(10, 5800.0);     // 0-10 m³: 5,800 đồng/m³
        put(20, 7000.0);     // 11-20 m³: 7,000 đồng/m³
        put(30, 8400.0);     // 21-30 m³: 8,400 đồng/m³
        put(Integer.MAX_VALUE, 9800.0); // Trên 30 m³: 9,800 đồng/m³
    }};

    /**
     * Tính tiền điện theo bậc thang
     * @param usage Lượng điện tiêu thụ (kWh)
     * @return Số tiền (VNĐ)
     */
    public static double calculateElectricCost(double usage) {
        return calculateTieredCost(usage, ELECTRIC_TIERS);
    }

    /**
     * Tính tiền nước theo bậc thang
     * @param usage Lượng nước tiêu thụ (m³)
     * @return Số tiền (VNĐ)
     */
    public static double calculateWaterCost(double usage) {
        return calculateTieredCost(usage, WATER_TIERS);
    }

    /**
     * Tính chi tiết tiền điện theo từng bậc
     * @param usage Lượng điện tiêu thụ (kWh)
     * @return Map chứa chi tiết từng bậc và thành tiền
     */
    public static Map<String, Double> calculateElectricDetails(double usage) {
        return calculateTieredDetails(usage, ELECTRIC_TIERS);
    }

    /**
     * Tính chi tiết tiền nước theo từng bậc
     * @param usage Lượng nước tiêu thụ (m³)
     * @return Map chứa chi tiết từng bậc và thành tiền
     */
    public static Map<String, Double> calculateWaterDetails(double usage) {
        return calculateTieredDetails(usage, WATER_TIERS);
    }

    /**
     * Phương thức chung để tính tiền theo bậc thang
     */
    private static double calculateTieredCost(double usage, LinkedHashMap<Integer, Double> tiers) {
        double totalCost = 0;
        double remainingUsage = usage;
        int previousTier = 0;

        for (Map.Entry<Integer, Double> tier : tiers.entrySet()) {
            int threshold = tier.getKey();
            double rate = tier.getValue();

            double tierUsage = Math.min(remainingUsage, threshold - previousTier);
            if (tierUsage > 0) {
                totalCost += tierUsage * rate;
                remainingUsage -= tierUsage;
            }

            if (remainingUsage <= 0) break;
            previousTier = threshold;
        }

        return totalCost;
    }

    /**
     * Phương thức tính chi tiết từng bậc
     */
    /**
     * Phương thức tính chi tiết từng bậc
     */
    private static Map<String, Double> calculateTieredDetails(double usage, LinkedHashMap<Integer, Double> tiers) {
        Map<String, Double> details = new LinkedHashMap<>();
        double remainingUsage = usage;
        int previousTier = 0;

        int bac = 1;
        for (Map.Entry<Integer, Double> tier : tiers.entrySet()) {
            int threshold = tier.getKey();
            double rate = tier.getValue();

            double tierUsage = Math.min(remainingUsage, threshold - previousTier);
            if (tierUsage > 0) {
                // Sửa cách định dạng key
                String key;
                if (threshold == Integer.MAX_VALUE) {
                    // Đối với bậc cuối cùng, hiển thị gọn hơn
                    key = String.format("Bậc %d (>%d): %.1f x %.0f",
                            bac, previousTier, tierUsage, rate);
                } else {
                    key = String.format("Bậc %d (%d-%d): %.1f x %.0f",
                            bac, previousTier + 1, threshold, tierUsage, rate);
                }

                double cost = tierUsage * rate;
                details.put(key, cost);
                remainingUsage -= tierUsage;
                bac++;
            }

            if (remainingUsage <= 0) break;
            previousTier = threshold;
        }
        double total = 0;
        for (Double cost : details.values()) {
            total += cost;
        }
        details.put("Tổng cộng", total);
        return details;
    }
}
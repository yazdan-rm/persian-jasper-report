package com.jmix.seed;

import com.jmix.entity.Sale;
import com.jmix.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {

    private final SaleRepository repository;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now(clock);
        repository.saveAll(List.of(
                sale("INV-1001", today.minusDays(8), "فروشگاه سپهر", "هدست بی‌سیم اوربیت", "تجهیزات صوتی", 3, "18900000"),
                sale("INV-1002", today.minusDays(7), "شرکت آریا", "نمایشگر همراه ویستا", "نمایشگر", 2, "32900000"),
                sale("INV-1003", today.minusDays(6), "استودیو آیونیکس", "صفحه‌کلید کامپکت کانواس", "لوازم جانبی", 5, "13900000"),
                sale("INV-1004", today.minusDays(5), "صنایع دلتا", "اسپیکر کنفرانس اکو", "تجهیزات صوتی", 2, "27900000"),
                sale("INV-1005", today.minusDays(4), "مشاوران اطلس", "نمایشگر سفری ویستا پرو", "نمایشگر", 1, "41900000"),
                sale("INV-1006", today.minusDays(3), "رسانه پیشرو", "هدست مدیریتی اوربیت", "تجهیزات صوتی", 4, "31900000"),
                sale("INV-1007", today.minusDays(2), "لجستیک افق", "صفحه‌کلید استودیو کانواس", "لوازم جانبی", 3, "17900000"),
                sale("INV-1008", today.minusDays(1), "آزمایشگاه سدر", "کیت نمایشگر دوگانه فریم", "نمایشگر", 1, "64900000")
        ));
    }

    private Sale sale(String invoiceNumber, LocalDate date, String customer, String product,
                      String category, int quantity, String unitPrice) {
        return new Sale(invoiceNumber, date, customer, product, category, quantity, new BigDecimal(unitPrice));
    }
}

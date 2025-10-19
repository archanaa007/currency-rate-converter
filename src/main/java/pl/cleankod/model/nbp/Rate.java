package pl.cleankod.model.nbp;

import java.math.BigDecimal;

public record Rate(String no, String effectiveDate, BigDecimal mid) {
}

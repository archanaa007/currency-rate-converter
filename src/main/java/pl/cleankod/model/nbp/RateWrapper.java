package pl.cleankod.model.nbp;

import java.util.List;

public record RateWrapper(String table, String currency, String code, List<Rate> rates) {
}

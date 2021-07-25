package danny.stock.calculate.service.macd;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.service.CalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

@Service
@Slf4j
public class MacdIndicator {
    @Autowired
    private TcbsClient myClient;

    @Autowired
    private CalculateService calculateService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    public Map<String, Double> calculateHistogram(final String ticker) {
        List<TickerDetail> data = calculateService.retrieveDataProperly(ticker);
        Map<String, BigDecimal> macd = calculateService.calculateMACDBigDecimal(data);
        Map<String, BigDecimal> signal = calculateService.calculateSignal(macd, data);
        return calculateService.calculateHistogram(macd, signal).entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    LocalDate parse = LocalDate.parse(o1.getKey(), formatter);
                    LocalDate parse1 = LocalDate.parse(o2.getKey(), formatter);
                    return (-1) * parse.compareTo(parse1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    public Map<String, Double> calculateHistogram(final List<TickerDetail> data) {
        if (data.size()<30) return new HashMap<>();
        Map<String, BigDecimal> macd = calculateService.calculateMACDBigDecimal(data);
        Map<String, BigDecimal> signal = calculateService.calculateSignal(macd, data);
        return calculateService.calculateHistogram(macd, signal).entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    LocalDate parse = LocalDate.parse(o1.getKey(), formatter);
                    LocalDate parse1 = LocalDate.parse(o2.getKey(), formatter);
                    return (-1) * parse.compareTo(parse1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
}

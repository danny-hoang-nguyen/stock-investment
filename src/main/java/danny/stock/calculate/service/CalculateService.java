package danny.stock.calculate.service;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.model.tcb.BarsLongTerm;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalculateService {

    @Autowired
    private TcbsClient myClient;


    public Map<String, Double> retrieveGeneralClosePrice(List<TickerDetail> data) {
        return data.stream()
                .collect(Collectors.toMap(TickerDetail::getTradingDate, TickerDetail::getClose));
    }


    public List<TickerDetail> retrieveDataProperly(final String ticker) {
        final long epochSecond = Instant.now().getEpochSecond();
        final long from = epochSecond - (365) * 24 * 60 * 60;
        final BarsLongTerm stockInfo = myClient
                .getStockInfo(ticker, "D", from, epochSecond);
        return stockInfo.getData();
    }

    public List<TickerDetail> retrieveDataProperly(final String ticker, final int numberOfDays) {
        final long epochSecond = Instant.now().getEpochSecond();
        final long from = epochSecond - (numberOfDays) * 24 * 60 * 60;
        final BarsLongTerm stockInfo = myClient
                .getStockInfo(ticker, "D", from, epochSecond);
        return stockInfo.getData();
    }

    public List<TickerDetail> retrieveWeeklyData(final String ticker) {
        final long epochSecond = Instant.now().getEpochSecond();
        final long from = epochSecond - (365 * 2) * 24 * 60 * 60;
        final BarsLongTerm stockInfo = myClient
                .getStockInfo(ticker, "W", from, epochSecond);
        return stockInfo.getData();
    }

    public Map<String, Double> calculateMovingAverage(final int maOf, final List<TickerDetail> data) {
        HashMap<String, Double> result = new HashMap<>(data.size(), 0.5f);
        for (int i = data.size() - 1; i > maOf; i--) {
            double sum = 0.0;
            for (int j = 0; j < maOf; j++) {
                sum = sum + data.get(i - j).getClose();
            }
            result.put(data.get(i).getTradingDate(), sum / maOf);
        }
        return result;
    }


    // calculate histogram = MACD - Signal

    public Map<String, Double> calculateHistogram(Map<String, BigDecimal> macd, Map<String, BigDecimal> signal) {
        int loopControl = Math.min(macd.size(), signal.size());
        Map<String, Double> result = new HashMap<>(loopControl, 0.5f);
        for (Map.Entry<String, BigDecimal> e : macd.entrySet()) {
            String key = e.getKey();
            if (signal.containsKey(key)) {
                BigDecimal value = e.getValue();
                BigDecimal signalValue = signal.get(key);
                result.put(key, value.subtract(signalValue).doubleValue());
            }
        }

        return result
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    LocalDate parse = LocalDate.parse(o1.getKey(), Util.DATE_TIME_FORMATTER);
                    LocalDate parse1 = LocalDate.parse(o2.getKey(), Util.DATE_TIME_FORMATTER);
                    return (-1) * parse.compareTo(parse1);
                })
//                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), formatter)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }


    // EMA 9 of MACD
    public Map<String, BigDecimal> calculateSignal(Map<String, BigDecimal> input, List<TickerDetail> tickerDetails) {
        Collections.reverse(tickerDetails);
        Map<String, BigDecimal> stringDoubleMap = calculateExponentialMovingAverage(9, input, tickerDetails);
        return stringDoubleMap
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    LocalDate parse = LocalDate.parse(o1.getKey(), Util.DATE_TIME_FORMATTER);
                    LocalDate parse1 = LocalDate.parse(o2.getKey(), Util.DATE_TIME_FORMATTER);
                    return (-1) * parse.compareTo(parse1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    // EMA 12 - EMA 26
    public Map<String, Double> calculateMACD(final List<TickerDetail> data) {

        Map<String, BigDecimal> stringDoubleMap = calculateExponentialMovingAverage(12, data);
        Map<String, BigDecimal> stringDoubleMap1 = calculateExponentialMovingAverage(26, data);

        int loopControl = Math.min(stringDoubleMap.size(), stringDoubleMap1.size());
        Map<String, Double> result = new HashMap<>(loopControl, 0.5f);

        Collections.reverse(data);
        for (int i = 0; i < loopControl; i++) {
            String tradingDate = data.get(i).getTradingDate();
            BigDecimal ema12 = stringDoubleMap.get(tradingDate);
            BigDecimal ema26 = stringDoubleMap1.get(tradingDate);
//            log.info("ema12 - ema26 : [{}] - [{}]", ema12.doubleValue(), ema26.doubleValue());
            BigDecimal macdAtTradingDate = ema12.subtract(ema26);
            result.put(tradingDate, macdAtTradingDate.doubleValue());
//            log.info("macd at [{}] ===> [{}]", tradingDate, macdAtTradingDate.doubleValue());
        }
        return result;
    }

    public Map<String, BigDecimal> calculateMACDBigDecimal(final List<TickerDetail> data) {

        Map<String, BigDecimal> stringDoubleMap = calculateExponentialMovingAverage(12, data);
        Map<String, BigDecimal> stringDoubleMap1 = calculateExponentialMovingAverage(26, data);

        int loopControl = Math.min(stringDoubleMap.size(), stringDoubleMap1.size());
        Map<String, BigDecimal> result = new HashMap<>(loopControl, 0.5f);

        Collections.reverse(data);
        for (int i = 0; i < loopControl; i++) {
            String tradingDate = data.get(i).getTradingDate();
            BigDecimal ema12 = stringDoubleMap.get(tradingDate);
            BigDecimal ema26 = stringDoubleMap1.get(tradingDate);
//            log.info("ema12 - ema26 : [{}] - [{}]", ema12, ema26);
            BigDecimal macdAtTradingDate = ema12.subtract(ema26);
            result.put(tradingDate, macdAtTradingDate);
//            log.info("macd at [{}] ===> [{}]", tradingDate, macdAtTradingDate.doubleValue());
        }
        return result;
    }

    // data retrieved from tcbs old -> new
    public Map<String, BigDecimal> calculateExponentialMovingAverage(final int n, final List<TickerDetail> data) {
        int size = data.size();
        HashMap<String, BigDecimal> result = new HashMap<>(size, 0.5f);

        List<TickerDetail> sortedDataByDate = data.stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getTradingDate(), Util.DATE_TIME_FORMATTER)))
                .collect(Collectors.toList());
        Double sumOfFirst = 0.0;
        for (int i = 0; i <= n; i++) {
            if (i == n) {
                BigDecimal initValue = BigDecimal.valueOf(sumOfFirst / n);
                result.put(sortedDataByDate.get(n).getTradingDate(), initValue);
                break;
            }
            sumOfFirst = sumOfFirst + sortedDataByDate.get(i).getClose();
        }
        Double k = 2.0 / (n + 1);
        for (int i = n + 1; i < size; i++) {
            BigDecimal fistEMA = BigDecimal.valueOf(sortedDataByDate.get(i).getClose() * k);
            BigDecimal previousResult = result.get(sortedDataByDate.get(i - 1).getTradingDate());
            BigDecimal emaToday = fistEMA.add(previousResult.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(k))));
            result.put(sortedDataByDate.get(i).getTradingDate(), emaToday);
        }
        return result
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    LocalDate parse = LocalDate.parse(o1.getKey(), Util.DATE_TIME_FORMATTER);
                    LocalDate parse1 = LocalDate.parse(o2.getKey(), Util.DATE_TIME_FORMATTER);
                    return (-1) * parse.compareTo(parse1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    public Map<String, BigDecimal> calculateExponentialMovingAverage(final int n, final Map<String, BigDecimal> data, List<TickerDetail> realData) {
        int size = data.size();
        HashMap<String, BigDecimal> result = new HashMap<>(size, 0.5f);
        double sumOfFirst = 0.0;

        String firstValidDate = "";

        for (int j = 0; j < size; j++) {
            String tradingDate = realData.get(j).getTradingDate();
            if (data.get(tradingDate) != null) {
                firstValidDate = tradingDate;
                break;
            }
        }
        for (int i = 0; i <= n; i++) {
            if (i == n) {
                BigDecimal initValue = BigDecimal.valueOf(sumOfFirst / n);
                result.put(firstValidDate, initValue);
                break;
            }
            sumOfFirst = sumOfFirst + realData.get(i).getClose();
        }

        BigDecimal k = BigDecimal.valueOf(2.0 / (n + 1));
        int realDataSize = realData.size();
        for (int i = n + 1; i < realDataSize; i++) {
            String tradingDate = realData.get(i).getTradingDate();
            if (data.get(realData.get(i - 1).getTradingDate()) == null) {
//                log.info("Invalid date [{}]", realData.get(i - 1).getTradingDate());
            } else {
                BigDecimal oneMinusK = BigDecimal.ONE.subtract(k);
                BigDecimal currentPriceMultiplyK = data.get(tradingDate).multiply(k);
                BigDecimal previousEMA = result.get(realData.get(i - 1).getTradingDate());
                BigDecimal emaToday = currentPriceMultiplyK
                        .add(previousEMA.multiply(oneMinusK));
//                log.info("Signal at [{}] ===> [{}]", tradingDate, emaToday.doubleValue());

                result.put(tradingDate, emaToday);
            }

        }

        return result;
    }

}

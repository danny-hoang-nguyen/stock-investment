package danny.stock.calculate.service.sma;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.domain.SMAIndicatorResult;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.service.CalculateService;
import danny.stock.calculate.service.HelperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SMAIndicator {

    public static final int TIME_RANGE = 45;
    @Autowired
    private TcbsClient myClient;

    @Autowired
    private CalculateService calculateService;

    @Autowired
    private HelperService helperService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    private Map<String, Double> getLatestDate(Map<String, Double> inputMap) {
        List<Entry<String, Double>> sortedResult = inputMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), formatter)))
                .collect(Collectors.toList());
        int size = sortedResult.size();
        Entry<String, Double> stringDoubleEntry = sortedResult.get(size - 1);
        return Map.of(stringDoubleEntry.getKey(), stringDoubleEntry.getValue());
    }

    public SMAIndicatorResult matchedSMAIndicator(String duration, final String ticker) {
        List<TickerDetail> data = new ArrayList<>();
        if ("D".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveDataProperly(ticker);
        } else if ("W".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveWeeklyData(ticker);
        }
//    log.info("Receive this number of data {}", data.size());
        SMAIndicatorResult smaIndicatorResult = new SMAIndicatorResult();

        smaIndicatorResult.setCode(ticker);
        Map<String, Double> sma9 = calculateService
                .calculateMovingAverage(9, data);
        Map<String, Double> sma18 = calculateService
                .calculateMovingAverage(18, data);
        Map<String, Double> sma40 = calculateService
                .calculateMovingAverage(40, data);

        smaIndicatorResult.setSma9(sma9);
        smaIndicatorResult.setSma18(sma18);
        smaIndicatorResult.setSma40(sma40);

        log.info("SMA 9 ==> {}", sma9.entrySet().size());
        log.info("SMA 18 ==> {}", sma18.entrySet().size());
        log.info("SMA 40 ==> {}", sma40.entrySet().size());

        int loopControl = Math.min(sma9.entrySet().size(), sma18.entrySet().size());
        loopControl = Math.min(sma40.entrySet().size(), loopControl);



        int sma9Win = 0;
        int sma9Lost = 0;
        int sma12Win = 0;
        int sma12Lost = 0;
        int sma26Win = 0;
        int sma26Lost = 0;
        Map<String, Double> generalData = calculateService.retrieveGeneralClosePrice(data);

        List<String> sortedKey = new ArrayList<>();
        generalData.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), formatter)))
                .collect(Collectors.toList()).forEach(stringDoubleEntry -> sortedKey.add(stringDoubleEntry.getKey()));
        Collections.reverse(sortedKey);

        // TODO them check gia sau nhung diem giao nhau cua cac duong trung binh
        for (int i = 0; i< loopControl; i++) {
            String date = sortedKey.get(i);
            Double sma9Value = sma9.get(date);
            Double sma18Value = sma18.get(date);
            Double sma40Value = sma40.get(date);
            if (Math.abs(sma9Value - sma18Value) < 250) {
                Double currentPrice = generalData.get(date);
                for (int j = i; j < i+5 && i+5< sortedKey.size() -1; j++) {
                    String nextDate = sortedKey.get(j);
                    Double nextDatePrice = generalData.get(nextDate);
                    if(nextDatePrice > sma9Value) {
                        log.info("Buy at the price [{}] : [{}] if possible", currentPrice, date.substring(0,10));
                    }
                    else break;
                }
//                if (currentPrice > sma9Value) {
//                    log.info("Buy at the price [{}] : [{}] if possible", currentPrice, date.substring(0,10));
//                }
            }
        }
//        smaIndicatorResult.setLatestClosePrice(getLatestDate(generalData));

//        for (String s : sortedKey) {
//            String date = s;
//            Double price = generalData.get(s);
//
//            double distantOne = -1;
//            double distantTwo = -1;
//            double distantThree = -1;
//
//            Map<String, Double> distantOfSMA9AndSMA18 = new HashMap<>(data.size(), 0.75f);
//            Map<String, Double> distantOfSMA18AndSMA40 = new HashMap<>(data.size(), 0.75f);
//            Map<String, Double> distantOfSMA40AndSMA9 = new HashMap<>(data.size(), 0.75f);
//
//            if (sma9.get(date) != null) {
//                Double aDouble = sma9.get(date);
//                if (aDouble < price) {
//                    sma9Win++;
//                } else {
//                    sma9Lost++;
//                }
//                distantOne = price - aDouble;
//                if (sma18.get(date) != null) {
//                    distantOfSMA9AndSMA18.put(date, Math.abs(sma18.get(date) - aDouble));
//                }
//
//                if (sma40.get(date) != null) {
//                    distantOfSMA40AndSMA9.put(date, Math.abs(sma40.get(date) - aDouble));
//                }
//            }
//
//            if (sma18.get(date) != null) {
//                Double aDouble = sma18.get(date);
//                if (aDouble < price) {
//                    sma12Win++;
//                } else {
//                    sma12Lost++;
//                }
//                distantTwo = price - aDouble;
//                if (sma40.get(date) != null) {
//                    distantOfSMA18AndSMA40.put(date, Math.abs(sma40.get(date) - aDouble));
//                }
//            }
//
//            if (sma40.get(date) != null) {
//                Double aDouble = sma40.get(date);
//                if (aDouble < price) {
//                    sma26Win++;
//                } else {
//                    sma26Lost++;
//                }
//                distantThree = price - aDouble;
//            }
//
//            distantOne = Math.abs(distantOne);
//            distantTwo = Math.abs(distantTwo);
//            distantThree = Math.abs(distantThree);
//
//            if (distantOfSMA9AndSMA18.get(date) != null
//                    && distantOfSMA18AndSMA40.get(date) != null
//                    && distantOfSMA40AndSMA9.get(date) != null) {
//                if (distantOfSMA9AndSMA18.get(date) < 500 &&
//                        distantOfSMA18AndSMA40.get(date) < 1000 &&
//                        distantOfSMA40AndSMA9.get(date) < 1500)
//                    log.info("SMA meets at: {} price: {}", date, price);
//            }
//
//        }

//        if (sma9Lost == 0) {
//            sma9Lost = 1;
//        }
//
//        if (sma12Lost == 0) {
//            sma12Lost = 1;
//        }
//
//        if (sma26Lost == 0) {
//            sma26Lost = 1;
//        }
//        log.info("ticker {}| SMA 9 WIN/LOSE==>{}:{}", ticker, sma9Win, sma9Lost);
//        if (sma9Win / sma9Lost >= 1.25) {
//            smaIndicatorResult.setSma9Matched(true);
//        }
//
//        log.info("ticker {}| SMA 18 WIN/LOSE==>{}:{}", ticker, sma12Win, sma12Lost);
//        if (sma12Win / sma12Lost >= 1) {
//            smaIndicatorResult.setSma18Matched(true);
//
//        }
//
//        log.info("ticker {}| SMA 40 WIN/LOSE==>{}:{}", ticker, sma26Win, sma26Lost);
//        if (sma26Win / sma26Lost >= 0.75) {
//            smaIndicatorResult.setSma40Matched(true);
//        }
//        calculateBuyAtPrice(smaIndicatorResult);
        return smaIndicatorResult;

    }

    public void calculateBuyAtPrice(final SMAIndicatorResult smaIndicatorResult) {
        double buyPrice;
        String key = smaIndicatorResult.getLatestClosePrice().keySet().stream().findFirst().get();
        Double closePrice = smaIndicatorResult.getLatestClosePrice().values().stream().findFirst()
                .get();
        if (smaIndicatorResult.isSma40Matched() && smaIndicatorResult.isSma18Matched()
                && smaIndicatorResult.isSma9Matched()) {

            buyPrice = Math.min(smaIndicatorResult.getSma9().get(key), closePrice);
            smaIndicatorResult.setBuyPrice(buyPrice);
            return;
        } else {
            smaIndicatorResult.setBuyPrice(null);
        }
    }

    public List<SMAIndicatorResult> scanByGroup(String duration, int groupId) {
        final List<String> tickerGroupBySector = helperService.getTickerGroupBySector(groupId);
        log.info("Ticker belonging to group {}", tickerGroupBySector);
        log.info("Number of tickers {}", tickerGroupBySector.size());
        final List<SMAIndicatorResult> results = new ArrayList<>();

        for (int i = 0; i < tickerGroupBySector.size(); i++) {
            SMAIndicatorResult smaIndicatorResult = matchedSMAIndicator(duration, tickerGroupBySector.get(i));
            smaIndicatorResult.getSma9().clear();
            smaIndicatorResult.getSma18().clear();
            smaIndicatorResult.getSma40().clear();
            if (smaIndicatorResult.getBuyPrice() != null) {
                results.add(smaIndicatorResult);
            }
        }
        return results;

    }
}

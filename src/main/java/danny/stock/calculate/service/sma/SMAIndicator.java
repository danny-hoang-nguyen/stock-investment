package danny.stock.calculate.service.sma;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.document.TickerForCalculation;
import danny.stock.calculate.domain.SMAIndicatorResult;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.model.vietstock.Sector;
import danny.stock.calculate.repository.TickerDetailRepository;
import danny.stock.calculate.service.CalculateService;
import danny.stock.calculate.service.HelperService;
import danny.stock.calculate.service.macd.MacdIndicator;
import danny.stock.calculate.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private MacdIndicator macdIndicator;

    @Autowired
    private TickerDetailRepository tickerDetailRepository;

//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    private Map<String, Double> getLatestDate(Map<String, Double> inputMap) {
        List<Entry<String, Double>> sortedResult = inputMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), Util.DATE_TIME_FORMATTER)))
                .collect(Collectors.toList());
        int size = sortedResult.size();
        Entry<String, Double> stringDoubleEntry = sortedResult.get(size - 1);
        return Map.of(stringDoubleEntry.getKey(), stringDoubleEntry.getValue());
    }

    private void writeToFile(List<SMAIndicatorResult> result) throws IOException {
        RandomAccessFile stream = new RandomAccessFile("ouput_"+ Instant.now().getEpochSecond(), "rw");
        FileChannel channel = stream.getChannel();

        for (SMAIndicatorResult s: result) {
            byte[] bytes = (s.getCode() + "," + s.getBuyPrice().getValue()+"\n").getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.write(buffer);

        }
        stream.close();
        channel.close();
    }

    public SMAIndicatorResult matchedSMAIndicator(String duration, final String ticker) {
        List<TickerDetail> data = new ArrayList<>();
        if ("D".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveDataProperly(ticker);
        } else if ("W".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveWeeklyData(ticker);
        }
        log.info("Receive this number of data [{}] for [{}]", data.size(), ticker);
        SMAIndicatorResult smaIndicatorResult = new SMAIndicatorResult();

        smaIndicatorResult.setCode(ticker);
        Map<String, Double> sma9 = calculateService
                .calculateMovingAverage(9, data);
        Map<String, Double> sma18 = calculateService
                .calculateMovingAverage(18, data);
//        Map<String, Double> sma40 = calculateService
//                .calculateMovingAverage(40, data);

        smaIndicatorResult.setSma9(sma9);
        smaIndicatorResult.setSma18(sma18);
//        smaIndicatorResult.setSma40(sma40);

        log.info("SMA 9 ==> {}", sma9.entrySet().size());
        log.info("SMA 18 ==> {}", sma18.entrySet().size());
//        log.info("SMA 40 ==> {}", sma40.entrySet().size());

        int loopControl = Math.min(sma9.entrySet().size(), sma18.entrySet().size());
//        loopControl = Math.min(sma40.entrySet().size(), loopControl);

        Map<String, Double> generalData = calculateService.retrieveGeneralClosePrice(data);

        List<String> sortedKey = new ArrayList<>();
        generalData.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), Util.DATE_TIME_FORMATTER)))
                .collect(Collectors.toList()).forEach(stringDoubleEntry -> sortedKey.add(stringDoubleEntry.getKey()));
        Collections.reverse(sortedKey);

        smaIndicatorResult.setLatestClosePrice(getLatestDate(generalData));
        Map<String, Double> histogram = macdIndicator.calculateHistogram(data);
        if (histogram.isEmpty()) return null;
        for (int i = 0; i < loopControl; i++) {
            String date = sortedKey.get(i);
            Double sma9Value = sma9.get(date);
            Double sma18Value = sma18.get(date);
//            Double sma40Value = sma40.get(date);
            Double currentPrice = generalData.get(date);
            if (Math.abs(sma9Value - sma18Value) < 1000) {
                if (smaIndicatorResult.getBuyPrice() != null) break;
                if (checkMACD(date, histogram)) {
                    if (smaIndicatorResult.getBuyPrice() == null){
                        log.info("[{}] ::: MACD matched buying [{}] : [{}]",ticker, date, currentPrice);
                        AbstractMap.SimpleEntry<String, Double> buyPrice = new AbstractMap.SimpleEntry<>(date, currentPrice);
                        if (!(smaIndicatorResult.getLatestClosePrice().values().stream().findFirst().get()
                                > buyPrice.getValue() - 10000)) {
                            AbstractMap.SimpleEntry chosenPrice = new AbstractMap.SimpleEntry(smaIndicatorResult.getLatestClosePrice().keySet().stream().findFirst().get(),
                                    smaIndicatorResult.getLatestClosePrice().values().stream().findFirst().get());
                            smaIndicatorResult.setBuyPrice(chosenPrice);
                        }
                    }
                }
            }

        }
        return smaIndicatorResult;

    }

    private void saveTickerDetail(TickerDetail td, String ticker, String group, String period) {
        TickerForCalculation tickerForCalculation = new TickerForCalculation();
        tickerForCalculation.setClose(td.getClose());
        tickerForCalculation.setOpen(td.getOpen());
        tickerForCalculation.setHigh(td.getHigh());
        tickerForCalculation.setLow(td.getLow());
        tickerForCalculation.setTradingDate(Util.convertStringToTime(td.getTradingDate()));
        tickerForCalculation.setTicker(ticker);
        tickerForCalculation.setGroup(group);
        tickerForCalculation.setPeriod(period);
        tickerDetailRepository.save(tickerForCalculation);
    }

    public SMAIndicatorResult matchedSMAIndicator(String duration, final Sector sector) {
        List<TickerDetail> data = new ArrayList<>();
        String ticker = sector.getTicker();
        if ("D".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveDataProperly(ticker, 365/2);
//            data.forEach(tickerDetail -> saveTickerDetail(tickerDetail, sector.getTicker(), sector.getGroup(), "D"));
        } else if ("W".equalsIgnoreCase(duration)) {
            data = calculateService.retrieveWeeklyData(ticker);
        }
        log.info("Receive this number of data [{}] for [{}]", data.size(), ticker);
        SMAIndicatorResult smaIndicatorResult = new SMAIndicatorResult();

        smaIndicatorResult.setCode(ticker);
        Map<String, Double> sma9 = calculateService
                .calculateMovingAverage(9, data);
        Map<String, Double> sma18 = calculateService
                .calculateMovingAverage(18, data);
//        Map<String, Double> sma40 = calculateService
//                .calculateMovingAverage(40, data);

        smaIndicatorResult.setSma9(sma9);
        smaIndicatorResult.setSma18(sma18);
//        smaIndicatorResult.setSma40(sma40);

        log.info("SMA 9 ==> {}", sma9.entrySet().size());
        log.info("SMA 18 ==> {}", sma18.entrySet().size());
//        log.info("SMA 40 ==> {}", sma40.entrySet().size());

        int loopControl = Math.min(sma9.entrySet().size(), sma18.entrySet().size());
//        loopControl = Math.min(sma40.entrySet().size(), loopControl);

        Map<String, Double> generalData = calculateService.retrieveGeneralClosePrice(data);

        List<String> sortedKey = new ArrayList<>();
        generalData.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), Util.DATE_TIME_FORMATTER)))
                .collect(Collectors.toList()).forEach(stringDoubleEntry -> sortedKey.add(stringDoubleEntry.getKey()));
        Collections.reverse(sortedKey);

        smaIndicatorResult.setLatestClosePrice(getLatestDate(generalData));
        Map<String, Double> histogram = macdIndicator.calculateHistogram(data);
        if (histogram.isEmpty()) return null;
        for (int i = 0; i < loopControl; i++) {
            String date = sortedKey.get(i);
            Double sma9Value = sma9.get(date);
            Double sma18Value = sma18.get(date);
//            Double sma40Value = sma40.get(date);
            Double currentPrice = generalData.get(date);
            if (Math.abs(sma9Value - sma18Value) < 1000) {
                if (smaIndicatorResult.getBuyPrice() != null) break;
                if (checkMACD(date, histogram)) {
                    if (smaIndicatorResult.getBuyPrice() == null){
                        log.info("[{}] ::: MACD matched buying [{}] : [{}]", ticker, date, currentPrice);
                        AbstractMap.SimpleEntry<String, Double> buyPrice = new AbstractMap.SimpleEntry<>(date, currentPrice);
                        if (!(smaIndicatorResult.getLatestClosePrice().values().stream().findFirst().get()
                                > buyPrice.getValue() - 10000)) {
                            AbstractMap.SimpleEntry chosenPrice = new AbstractMap.SimpleEntry(smaIndicatorResult.getLatestClosePrice().keySet().stream().findFirst().get(),
                                    smaIndicatorResult.getLatestClosePrice().values().stream().findFirst().get());
                            smaIndicatorResult.setBuyPrice(chosenPrice);
                        }
                    }
                }
            }

        }
        return smaIndicatorResult;

    }
    public  List<SMAIndicatorResult> scanAll(final List<Integer> sectorIds) throws IOException {
         List<SMAIndicatorResult>finalOutput = new ArrayList<>();
//        for (int i = 1; i < 30; i++) {
        for (int temp = 0; temp < sectorIds.size(); temp++) {
            final List<Sector> tickerGroupBySector = helperService.getTickerGroupBySector(sectorIds.get(temp));
            if (!tickerGroupBySector.isEmpty()) {
                int sizeOfSector = tickerGroupBySector.size();
                log.info("Number of tickers {}", sizeOfSector);

                final List<SMAIndicatorResult> results = new ArrayList<>();
                List<Sector> sectorList = tickerGroupBySector.stream().sorted((o1, o2) -> (int) (o1.getCapital() - o2.getCapital())).collect(Collectors.toList());
                int percentile = (int) Math.ceil(0.5 * sectorList.size()) - 1;
                Sector standard = sectorList.get(percentile);

                for (int j = 0; j < sizeOfSector; j=j+1) {
                    Sector sector = tickerGroupBySector.get(j);
                    log.info("Ticker [{}] | Group [{}] is under processing",
                            sector.getTicker(), sector.getGroup());
                    if (sector.getCapital() < standard.getCapital()) {
                        log.info("Ticker [{}] has small capital < avg: [{}] < [{}]", sector.getTicker(), sector.getCapital(), standard.getCapital());
                        continue;
                    }
                    SMAIndicatorResult smaIndicatorResult = matchedSMAIndicator("D", sector.getTicker());
                    if (smaIndicatorResult!=null) {
                        smaIndicatorResult.getSma9().clear();
                        smaIndicatorResult.getSma18().clear();
                        if (smaIndicatorResult.getBuyPrice() != null) {
                            results.add(smaIndicatorResult);
                        }
                    }
                }
                finalOutput.addAll(results);
            }
}
        writeToFile(finalOutput);
        return finalOutput;
    }


    public List<SMAIndicatorResult> scanByGroup(String duration, int groupId) {
        final List<Sector> tickerGroupBySector = helperService.getTickerGroupBySector(groupId);
        log.info("Ticker belonging to group {}", tickerGroupBySector);
        log.info("Number of tickers {}", tickerGroupBySector.size());
        final List<SMAIndicatorResult> results = new ArrayList<>();

        for (int i = 0; i < tickerGroupBySector.size(); i++) {
            SMAIndicatorResult smaIndicatorResult = matchedSMAIndicator(duration, tickerGroupBySector.get(i).getTicker());
            smaIndicatorResult.getSma9().clear();
            smaIndicatorResult.getSma18().clear();
//            smaIndicatorResult.getSma40().clear();
            if (smaIndicatorResult.getBuyPrice() != null) {
                results.add(smaIndicatorResult);
            }
        }
        return results;
    }

    private boolean checkMACD(String date, Map<String, Double> histogram) {
        LocalDateTime localDateTime = Util.convertStringToTime(date);
        LocalDateTime oneDayBefore = localDateTime.minus(1, ChronoUnit.DAYS);
        LocalDateTime twoDaysBefore = localDateTime.minus(2, ChronoUnit.DAYS);
        LocalDateTime threeDaysBefore = localDateTime.minus(3, ChronoUnit.DAYS);
        LocalDateTime fourDaysBefore = localDateTime.minus(4, ChronoUnit.DAYS);
        LocalDateTime fiveDaysBefore = localDateTime.minus(5, ChronoUnit.DAYS);
        int shouldBuy = 0;
        int shouldSell = 0;

        List<LocalDateTime> localDates = Arrays.asList(oneDayBefore, twoDaysBefore, threeDaysBefore, fourDaysBefore, fiveDaysBefore);
        Double currentHistogram = histogram.get(date);
        if (currentHistogram != null && Math.abs(currentHistogram) > 100) return false;

        for (int i = 0; i < 5; i++) {
            String goodFormat = Util.convertTimeToString(localDates.get(i));
            if (histogram.get(goodFormat) != null) {
                if (histogram.get(goodFormat) >= 0) {
                    shouldSell++;
                } else shouldBuy++;
            }

        }
//        log.info("date - histogram - buy - sell: [{}] - [{}] - [{}] - [{}]", date, currentHistogram, shouldBuy, shouldSell);
        return shouldBuy > shouldSell && shouldBuy + shouldSell >= 4;
    }
}

package danny.stock.calculate.service;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.model.tcb.BarsLongTerm;
import danny.stock.calculate.model.tcb.TickerDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public List<TickerDetail> retrieveData(final String ticker, int timeRange) {
        long epochSecond = Instant.now().getEpochSecond();
        BarsLongTerm stockInfo = myClient
                .getStockInfo(ticker, "D", epochSecond - (long) timeRange * 24 * 60 * 60, epochSecond);
        ArrayList<TickerDetail> data = stockInfo.getData();
        int gap = timeRange - data.size();
        int temp = timeRange;
        Integer tempDataSize = 0;

        while (gap > 0) {
            timeRange = timeRange + gap;
            stockInfo = myClient
                    .getStockInfo(ticker, "D", epochSecond - (long) (timeRange) * 24 * 60 * 60, epochSecond);
            data = stockInfo.getData();
            if (tempDataSize == data.size()) {
                log.info("This is all {} figures for this ticker {}", data.size(), ticker);
                break;
            }
            tempDataSize = data.size();
            gap = temp - data.size();
        }
        return data;
    }

    public List<TickerDetail> retrieveDataProperly(final String ticker) {
        final long epochSecond = Instant.now().getEpochSecond();
        final long from = epochSecond - (365) * 24 * 60 * 60;
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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public Map<String, Double> calculateExponentialMovingAverage(final int n, final List<TickerDetail> data) {
        int size = data.size();
        HashMap<String, Double> result = new HashMap<>(size, 0.5f);

        List<TickerDetail> sortedDataByDate = data.stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getTradingDate(), formatter)))
                .collect(Collectors.toList());
        double sumOfFirst = 0.0;
        for (int i = 0 ; i <= n; i++) {
            if (i == n) {
                double initValue = sumOfFirst / n;
                result.put(sortedDataByDate.get(n).getTradingDate(), initValue);
                break;
            }
            sumOfFirst = sumOfFirst + sortedDataByDate.get(i).getClose();
        }
        double k = 2 / (n + 1);
        for (int i = n + 1; i < size; i++) {
            double emaToday = (sortedDataByDate.get(i).getClose() * k) + (result.get(sortedDataByDate.get(i - 1).getTradingDate()) * (1 - k));
            result.put(sortedDataByDate.get(i).getTradingDate(), emaToday);
        }

        return result;
    }

}

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        HashMap<String, Double> sortedResult = new HashMap<>(size, 0.5f);

        List<TickerDetail> sortedDataByDate = data.stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getTradingDate(), formatter)))
                .collect(Collectors.toList());
        double sumOfFirst = 0.0;
        for (int i = 0; i <= n; i++) {
            if (i == n) {
                double initValue = sumOfFirst / n;
                result.put(sortedDataByDate.get(n).getTradingDate(), initValue);
                break;
            }
            sumOfFirst = sumOfFirst + sortedDataByDate.get(i).getClose();
        }
        double k = 2.0 / (n + 1);
        for (int i = n + 1; i < size; i++) {
            double emaToday = (sortedDataByDate.get(i).getClose() * k) + (result.get(sortedDataByDate.get(i - 1).getTradingDate()) * (1 - k));
            result.put(sortedDataByDate.get(i).getTradingDate(), emaToday);
        }

//        List<String> sortedResultKey = result.keySet().stream().sorted(Comparator.comparing(o -> LocalDate.parse(o, formatter))).collect(Collectors.toList());
//        int sortedResultSize = sortedResultKey.size();
//        for (int i = 0; i < sortedResultSize; i++) {
//            sortedResult.put(sortedResultKey.get(i), result.get(sortedResultKey.get(i)));
//        }

        result.entrySet().stream()
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.getKey(), formatter)))
                .forEach(stringDoubleEntry -> {
                    sortedResult.put(stringDoubleEntry.getKey(), stringDoubleEntry.getValue());
                });
//                .collect(Collectors.toList());
//                .flatMap(stringDoubleEntry -> Stream.of(result)).collect(Collectors.toList());
        return sortedResult;
    }

}

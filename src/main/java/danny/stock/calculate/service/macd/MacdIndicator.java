package danny.stock.calculate.service.macd;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.service.CalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MacdIndicator {
    @Autowired
    private TcbsClient myClient;

    @Autowired
    private CalculateService calculateService;

    public Map<String, Double> calculateEMA(final int n, final String ticker) {
        return calculateService.calculateExponentialMovingAverage(n, calculateService.retrieveDataProperly(ticker));
    }
}

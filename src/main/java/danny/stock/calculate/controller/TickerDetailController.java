package danny.stock.calculate.controller;

import danny.stock.calculate.domain.SMAIndicatorResult;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.service.TickerDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class TickerDetailController {


    @Autowired
    private TickerDetailService tickerDetailService;

    private static final String HALF_YEAR = "182";
    private static final String DAY = "D";

    @GetMapping("/ticker")
    ResponseEntity<List<TickerDetail>> getTickerDataWithCachedMechanism(@RequestParam(defaultValue = HALF_YEAR) int day,
                                                                        @RequestParam String ticker, @RequestParam(defaultValue = DAY) String period) {
        return ResponseEntity.of(Optional.of(tickerDetailService.findTickerDetailBetweenTimeRange(day, ticker, period)));
    }

}

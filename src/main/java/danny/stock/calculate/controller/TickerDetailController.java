package danny.stock.calculate.controller;

import danny.stock.calculate.service.Parser;
import danny.stock.calculate.service.TickerDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

@RestController
@Slf4j
public class TickerDetailController {


    @Autowired
    private TickerDetailService tickerDetailService;

    @Autowired
    private Parser parser;

    private static final String HALF_YEAR = "182";
    private static final String DAY = "D";

    @GetMapping("/ticker")
    ResponseEntity<?> getTickerDataWithCachedMechanism(@RequestParam(defaultValue = HALF_YEAR) int day, @RequestParam(defaultValue = DAY) String period) throws IOException {
        List<String> names = parser.retrieveJustSectorName();
        for (String s : names) {
            List<Vector> tickerAndRoe = parser.getTickerAndRoe(s);
            log.info("Vector ===> [{}]", tickerAndRoe);
            for (Vector v : tickerAndRoe) {
                String code = (String) v.get(0);
                log.info("Code ===> [{}]", code);

                tickerDetailService.findTickerDetailBetweenTimeRange(day, code, period);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
//        return ResponseEntity.of(Optional.of(tickerDetailService.findTickerDetailBetweenTimeRange(day, ticker, period)));
    }

}

package danny.stock.calculate.controller;

import danny.stock.calculate.domain.SMAIndicatorResult;
import danny.stock.calculate.service.CalculateService;
import danny.stock.calculate.service.HelperService;
import danny.stock.calculate.service.sma.SMAIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class StockController {

    @Autowired
    private CalculateService calculateService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private SMAIndicator smaIndicator;

    @GetMapping("/sector/{period}/{sectorId}")
    ResponseEntity<List<SMAIndicatorResult>> getTickerGroupedBySector(@PathVariable String period, @PathVariable Integer sectorId) {
        return ResponseEntity.of(Optional.of(smaIndicator.scanByGroup(period, sectorId)));
    }

    @GetMapping("/sectors")
    Map<String, List<String>> getSectors() {
        return helperService.getTickerGroupBySector();
    }


    @GetMapping("/ticker/{period}/{code}")
    SMAIndicatorResult getTickerGroupedBySector(@PathVariable String period,
                                                @PathVariable String code) {
        return smaIndicator.matchedSMAIndicator(period, code);
    }
}


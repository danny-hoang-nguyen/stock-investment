package danny.stock.calculate.controller;

import danny.stock.calculate.domain.SMAIndicatorResult;
import danny.stock.calculate.service.CalculateService;
import danny.stock.calculate.service.HelperService;
import danny.stock.calculate.service.macd.MacdIndicator;
import danny.stock.calculate.service.sma.SMAIndicator;
import danny.stock.calculate.util.Util;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

    @Autowired
    private CalculateService calculateService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private SMAIndicator smaIndicator;

    @Autowired
    private MacdIndicator macdIndicator;

//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    @GetMapping("/sector/{period}/{sectorId}")
    ResponseEntity<List<SMAIndicatorResult>> getTickerGroupedBySector(@PathVariable String period, @PathVariable Integer sectorId) {
        return ResponseEntity.of(Optional.of(smaIndicator.scanByGroup(period, sectorId)));
    }

    @GetMapping("/scan/{sectorIds}")
    ResponseEntity< List<SMAIndicatorResult>> findOutPotentialTickers(@PathVariable List<Integer> sectorIds) throws IOException {
        return ResponseEntity.of(Optional.of(smaIndicator.scanAll(sectorIds)));
    }

    @GetMapping("/scan-all/")
    ResponseEntity<Set<SMAIndicatorResult>> findOutPotentialTickers() throws IOException {
        return ResponseEntity.of(Optional.of(smaIndicator.scanAll()));
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

    @GetMapping("/ticker/macd/{code}")
    Map<String, Double> calculateHistogram(@PathVariable String code) {
        return macdIndicator.calculateHistogram(code);
    }

    @GetMapping("/ticker/each-macd/{n}/{code}")
    Map<String, Double> calculateEMAIndividually(
            @PathVariable String code) {
        return calculateService.calculateMACD(calculateService.retrieveDataProperly(code, 100)).entrySet().stream()
                .sorted((o1, o2) -> {
                    LocalDateTime parse = Util.convertStringToTime(o1.getKey());
                    LocalDateTime parse1 = Util.convertStringToTime(o2.getKey());
                    return (-1) * parse.compareTo(parse1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
}


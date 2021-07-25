package danny.stock.calculate.controller;

import danny.stock.calculate.model.vietstock.TickerFigure;
import danny.stock.calculate.service.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ParseController {
    @Autowired
    private Parser parser;

    @GetMapping("/sector/general")
    public ResponseEntity<List<TickerFigure>> parseSectorGeneralInfo() {
        try {
            return ResponseEntity.ok(parser.retrieveSectorData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

//    @GetMapping("/sector")
//    public ResponseEntity<List<ArrayList<String>>> parseEachSector(@RequestParam(required = true) String name) {
//        try {
//            return ResponseEntity.ok(parser.retrieveTickerDataBasedOnSector(name));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return ResponseEntity.ok(null);
//    }

    @GetMapping("/vietstock")
    public ResponseEntity<TickerFigure> parseEachSector(@RequestParam(required = true) String name) {
        try {
            return ResponseEntity.ok(parser.extractFromVietStock(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
}

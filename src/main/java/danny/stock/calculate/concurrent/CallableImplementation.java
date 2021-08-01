package danny.stock.calculate.concurrent;

import danny.stock.calculate.service.Parser;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class CallableImplementation implements Callable<Object> {
    String ticker;
    Parser parser;

    public CallableImplementation(Parser parser, String ticker) {
        this.ticker = ticker;
        this.parser = parser;
    }

    @Override
    public Object call() throws Exception {
        log.info("[{}] is running" ,Thread.currentThread().getName());
        return parser.extractFromVietStock(ticker);
    }


}

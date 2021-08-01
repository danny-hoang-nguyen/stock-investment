package danny.stock.calculate.concurrent;

import danny.stock.calculate.repository.TickerDetailRepository;
import danny.stock.calculate.repository.TickerFigureRepository;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static danny.stock.calculate.service.Parser.TAI_CHINH;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Service
public class AsyncService {
//    @Autowired
//    private TickerDetailRepository tickerDetailRepository;


    @Autowired
    private TickerFigureRepository tickerFigureRepository;
    public void get(){
        tickerFigureRepository.findAll().forEach(tickerForCalculation -> {
            String url = "https://apipubaws.tcbs.com.vn/stock-insight/v1/stock/bars-long-term?ticker=" + tickerForCalculation.getCode() + "&type=stock&resolution=D&from=1609478485&to=1625116890";
            try {
                try (AsyncHttpClient asyncHttpClientNext = asyncHttpClient(config().setHandshakeTimeout(30000))) {
                    asyncHttpClientNext
                            .prepareGet(url)
                            .execute()
                            .toCompletableFuture()
                            .thenApply(Response::getResponseBody)
                            .thenAccept(s -> Logger.getAnonymousLogger().info(s))
                            .join();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}

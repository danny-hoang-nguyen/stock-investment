package danny.stock.calculate.concurrent;

import danny.stock.calculate.repository.TickerDetailRepository;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Service
public class AsyncService {
    @Autowired
    private TickerDetailRepository tickerDetailRepository;

    public void get() throws IOException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();

        List<Future<Response>> in = new ArrayList<>();
        tickerDetailRepository.findAll().forEach(tickerForCalculation -> {
            String ticker = tickerForCalculation.getTicker();
//            https://apipubaws.tcbs.com.vn/stock-insight/v1/stock/bars-long-term?ticker=PNJ&type=stock&resolution=D&from=1609478485&to=1625116890
            String url = "http://apipubaws.tcbs.com.vn/stock-insight/v1/stock/bars-long-term?ticker=" + ticker + "&type=stock&resolution=D&from=1609478485&to=1625116890";
            Future<Response> whenResponse = asyncHttpClient.prepareGet(url).execute();

            in.add(whenResponse);
        });
        try {

            for (Future<Response> a : in) {
                Response response = a.get();
                System.out.println(Thread.currentThread().getName() + ":::" +Thread.currentThread().getName());
                System.out.println(response.getResponseBody());
            }
            asyncHttpClient.close();

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }

    }
}

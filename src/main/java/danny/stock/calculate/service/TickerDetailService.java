package danny.stock.calculate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.document.TickerForCalculation;
import danny.stock.calculate.model.tcb.BarsLongTerm;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.repository.TickerDetailRepository;
import danny.stock.calculate.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Service
@Slf4j
public class TickerDetailService {

    @Autowired
    private TickerDetailRepository tickerDetailRepository;

    @Autowired
    private TcbsClient tcbsClient;

    @Autowired
    private ObjectMapper objectMapper;

    public List<TickerDetail> findTickerDetailBetweenTimeRange(int numberOfSessions, String ticker, String period, String sector) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minus(numberOfSessions, ChronoUnit.DAYS);

        List<TickerForCalculation> existingData = tickerDetailRepository.findByTradingDateBetweenAndPeriodEqualsAndTickerEquals(from, to, period, ticker);
        log.info("Existing data = [{}]", existingData.size());

        List<TickerDetail> result = existingData.stream().map(this::createFromTickerDetailDocument).collect(Collectors.toList());
        if (existingData.size() < 240) {

            LocalDateTime tradingDate = null;
            if (!existingData.isEmpty()) {
                tradingDate = existingData.stream().max(Comparator.comparing(TickerForCalculation::getTradingDate)).get().getTradingDate();
            } else {
                tradingDate = from;
                long fromDateInSecond = Util.convertDateToSecond(tradingDate);
                long toDateInSecond = Util.convertDateToSecond(LocalDateTime.now());

//                BarsLongTerm stocks = tcbsClient.getStockInfo(ticker, "D", fromDateInSecond, toDateInSecond);
                String url = "https://apipubaws.tcbs.com.vn/stock-insight/v1/stock/bars-long-term?ticker=" + ticker + "&type=stock&resolution=D&from=" + fromDateInSecond + "&to=" + toDateInSecond;
                try {
                    try (AsyncHttpClient asyncHttpClientNext = asyncHttpClient(config().setHandshakeTimeout(30000))) {
                        asyncHttpClientNext
                                .prepareGet(url)
                                .execute()
                                .toCompletableFuture()
                                .thenApply(Response::getResponseBody)
                                .thenAccept(responseBody -> {
                                    try {
                                        BarsLongTerm stocks = objectMapper.readValue(responseBody, BarsLongTerm.class);
                                        log.info("New data retrieved = [{}] ::: [{}]", stocks.getData().size(), Thread.currentThread().getName());
                                        saveNewStockData(ticker, stocks.getData(), period, sector);
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                })
                                .join();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Collections.emptyList();
//                log.info("New data retrieved = [{}]", stocks.getData().size());
//                saveNewStockData(ticker, stocks.getData(), period, sector);
//                return stocks.getData().stream().peek(tickerDetail -> tickerDetail.setCode(ticker)).collect(Collectors.toList());
            }

            long fromDateInSecond = Util.convertDateToSecond(tradingDate);
            long toDateInSecond = Util.convertDateToSecond(LocalDateTime.now());

//            BarsLongTerm stocks = tcbsClient.getStockInfo(ticker, "D", fromDateInSecond, toDateInSecond);

            String url = "https://apipubaws.tcbs.com.vn/stock-insight/v1/stock/bars-long-term?ticker=" + ticker + "&type=stock&resolution=D&from=" + fromDateInSecond + "&to=" + toDateInSecond;
            try {
                try (AsyncHttpClient asyncHttpClientNext = asyncHttpClient(config().setHandshakeTimeout(30000))) {
                    asyncHttpClientNext
                            .prepareGet(url)
                            .execute()
                            .toCompletableFuture()
                            .thenApply(Response::getResponseBody)
                            .thenAccept(responseBody -> {
                                try {
                                    BarsLongTerm stocks = objectMapper.readValue(responseBody, BarsLongTerm.class);
                                    log.info("New data retrieved = [{}]", stocks.getData().size());
                                    saveNewStockData(ticker, stocks.getData(), period, sector);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            })
                            .join();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private TickerDetail createFromTickerDetailDocument(final TickerForCalculation tickerForCalculation) {
        TickerDetail tickerDetail = new TickerDetail();
        tickerDetail.setClose(tickerForCalculation.getClose());
        tickerDetail.setTradingDate(Util.DATE_TIME_FORMATTER.format(tickerForCalculation.getTradingDate()));
        tickerDetail.setCode(tickerForCalculation.getTicker());
        tickerDetail.setHigh(tickerForCalculation.getHigh());
        tickerDetail.setLow(tickerForCalculation.getLow());
        tickerDetail.setOpen(tickerForCalculation.getOpen());
        tickerDetail.setVolume(tickerForCalculation.getVolume());
        tickerDetail.setCode(tickerForCalculation.getTicker());
        return tickerDetail;
    }

    private TickerForCalculation createFromTickerDetail(final String ticker, final TickerDetail tickerDetail, final String period,
                                                        final String sector) {
        TickerForCalculation tickerForCalculation = new TickerForCalculation();
        tickerForCalculation.setPeriod(period);
        tickerForCalculation.setTradingDate(Util.convertStringToTime(tickerDetail.getTradingDate()));
        tickerForCalculation.setLow(tickerDetail.getLow());
        tickerForCalculation.setHigh(tickerDetail.getHigh());
        tickerForCalculation.setOpen(tickerDetail.getOpen());
        tickerForCalculation.setClose(tickerDetail.getClose());
        tickerForCalculation.setVolume(tickerDetail.getVolume());
        tickerForCalculation.setTicker(ticker);
        tickerForCalculation.setSector(sector);
        return tickerForCalculation;
    }

    private void saveNewStockData(String ticker, List<TickerDetail> tickerDetails, String period, String sector) {
        for (TickerDetail tickerDetail: tickerDetails) {
            TickerForCalculation fromTickerDetail = createFromTickerDetail(ticker, tickerDetail, period, sector);
            tickerDetailRepository.save(fromTickerDetail);
        }
    }
}

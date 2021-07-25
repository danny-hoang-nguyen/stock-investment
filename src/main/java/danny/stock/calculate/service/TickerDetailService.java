package danny.stock.calculate.service;

import danny.stock.calculate.client.TcbsClient;
import danny.stock.calculate.document.TickerDetailDocument;
import danny.stock.calculate.model.tcb.BarsLongTerm;
import danny.stock.calculate.model.tcb.TickerDetail;
import danny.stock.calculate.repository.TickerDetailRepository;
import danny.stock.calculate.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TickerDetailService {

    @Autowired
    private TickerDetailRepository tickerDetailRepository;

    @Autowired
    private TcbsClient tcbsClient;

    public List<TickerDetail> findTickerDetailBetweenTimeRange(int numberOfSessions, String ticker, String period) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minus(numberOfSessions, ChronoUnit.DAYS);

        List<TickerDetailDocument> existingData = tickerDetailRepository.findByTradingDateBetweenAndPeriodEqualsAndTickerEquals(from, to, period, ticker);
        log.info("Existing data = [{}]", existingData.size());

        List<TickerDetail> result = existingData.stream().map(this::createFromTickerDetailDocument).collect(Collectors.toList());
        if (existingData.size() < 122) {

            LocalDateTime tradingDate = null;
            if (!existingData.isEmpty()) {
                tradingDate = existingData.stream().max(Comparator.comparing(TickerDetailDocument::getTradingDate)).get().getTradingDate();
            } else {
                tradingDate = from;
                long fromDateInSecond = Util.convertDateToSecond(tradingDate);
                long toDateInSecond = Util.convertDateToSecond(LocalDateTime.now());
                BarsLongTerm stocks = tcbsClient.getStockInfo(ticker, "D", fromDateInSecond, toDateInSecond);
                log.info("New data retrieved = [{}]", stocks.getData().size());
                saveNewStockData(ticker, stocks.getData(), period);
                return stocks.getData().stream().peek(tickerDetail -> tickerDetail.setCode(ticker)).collect(Collectors.toList());
            }

            long fromDateInSecond = Util.convertDateToSecond(tradingDate);
            long toDateInSecond = Util.convertDateToSecond(LocalDateTime.now());
            BarsLongTerm stocks = tcbsClient.getStockInfo(ticker, "D", fromDateInSecond, toDateInSecond);
            log.info("New data retrieved = [{}]", stocks.getData().size());
            saveNewStockData(ticker, stocks.getData(), period);
            result.addAll(stocks.getData().stream().peek(tickerDetail -> tickerDetail.setCode(ticker)).collect(Collectors.toList()));
        }
        return result;
    }

    private TickerDetail createFromTickerDetailDocument(final TickerDetailDocument tickerDetailDocument) {
        TickerDetail tickerDetail = new TickerDetail();
        tickerDetail.setClose(tickerDetailDocument.getClose());
        tickerDetail.setTradingDate(Util.DATE_TIME_FORMATTER.format(tickerDetailDocument.getTradingDate()));
        tickerDetail.setCode(tickerDetailDocument.getTicker());
        tickerDetail.setHigh(tickerDetailDocument.getHigh());
        tickerDetail.setLow(tickerDetailDocument.getLow());
        tickerDetail.setOpen(tickerDetailDocument.getOpen());
        tickerDetail.setVolume(tickerDetailDocument.getVolume());
        tickerDetail.setCode(tickerDetailDocument.getTicker());
        return tickerDetail;
    }

    private TickerDetailDocument createFromTickerDetail(final String ticker, final TickerDetail tickerDetail, final String period) {
        TickerDetailDocument tickerDetailDocument = new TickerDetailDocument();
        tickerDetailDocument.setPeriod(period);
        tickerDetailDocument.setTradingDate(Util.convertStringToTime(tickerDetail.getTradingDate()));
        tickerDetailDocument.setLow(tickerDetail.getLow());
        tickerDetailDocument.setHigh(tickerDetail.getHigh());
        tickerDetailDocument.setOpen(tickerDetail.getOpen());
        tickerDetailDocument.setClose(tickerDetail.getClose());
        tickerDetailDocument.setVolume(tickerDetail.getVolume());
        tickerDetailDocument.setTicker(ticker);
        return tickerDetailDocument;
    }

    private void saveNewStockData(String ticker, List<TickerDetail> tickerDetails, String period ) {
        for (TickerDetail tickerDetail: tickerDetails) {
            TickerDetailDocument fromTickerDetail = createFromTickerDetail(ticker, tickerDetail, period);
            tickerDetailRepository.save(fromTickerDetail);
        }
    }
}

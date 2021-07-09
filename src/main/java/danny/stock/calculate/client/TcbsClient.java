package danny.stock.calculate.client;

import danny.stock.calculate.model.tcb.BarsLongTerm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tcb-stockinsight", url = "https://apipubaws.tcbs.com.vn/stock-insight/v1/"
    ,
    configuration = TcbsClientConfig.class
)
public interface TcbsClient {

  @GetMapping(value = "stock/bars-long-term?ticker={ticker}&type=stock&resolution={resolution}&from={from}&to={to}")
  BarsLongTerm getStockInfo(@PathVariable("ticker") String ticker,
      @PathVariable ("resolution") String resolution,
      @PathVariable("from") long from,
      @PathVariable("to") long to);
}

package danny.stock.calculate.client;

import danny.stock.calculate.model.vndirect.ListedStockResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "vndirect-stocklist-api", url = "https://api-finfo.vndirect.com.vn/v4/")
public interface VndirectClient {

  @GetMapping(value = "stocks?q=type:IFC,ETF,STOCK~status:LISTED&size=3000")
  ListedStockResult getStockList();
}

package danny.stock.calculate.client;

import danny.stock.calculate.model.vietstock.Sector;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vietstock", url = "https://api.vietstock.vn/finance/")
public interface VietStockClient {

  @GetMapping(value = "sectorinfo?sectorID={sectorId}&languageID=1")
  List<Sector> getStockInfo(@PathVariable("sectorId") Integer sectorId);
}

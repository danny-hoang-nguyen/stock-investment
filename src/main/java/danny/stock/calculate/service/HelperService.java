package danny.stock.calculate.service;

import danny.stock.calculate.client.VietStockClient;
import danny.stock.calculate.model.vietstock.Sector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HelperService {

    @Autowired
    VietStockClient vietStockClient;


    Map<String, List<String>> tickerGroupBySector;
    List<Sector> stockInfo;

    private static final Integer BAN_BUON = 1;
    private static final Integer BAO_HIEM = 2;
    private static final Integer BDS = 3;
    private static final Integer QUY = 4;
    private static final Integer CHUNG_KHOAN = 5;
    private static final Integer CNNT = 6;
    private static final Integer BAN_LE = 7;
    private static final Integer CS_SK = 8;

    private static final Integer KHAI_KHOANG = 10;
    private static final Integer NGAN_HANG = 11;
    private static final Integer NONG_NGHIEP = 12;
    private static final Integer SX_MAY = 15;
    private static final Integer VAI = 16;
    private static final Integer CAO_SU = 17;
    private static final Integer NHUA = 18;
    private static final Integer THUC_PHAM = 19;
    private static final Integer THUY_SAN = 20;

    private static final Integer VLXD = 21;
    private static final Integer NHIET_DIEN = 22;
    private static final Integer VAN_TAI_KHO_BAI = 23;
    private static final Integer XAY_DUNG = 24;
    private static final Integer DV_LUUTRU_AN_UONG_GIAI_TRI = 25;
    private static final Integer SX_GO = 26;
    private static final Integer THIET_BI_DIEN = 27;
    private static final Integer TU_VAN_HO_TRO = 28;
    private static final Integer MOI_GIOI = 29;

    public List<String> getTickerGroupBySector(final Integer sectorId) {
        stockInfo = vietStockClient.getStockInfo(sectorId);
        return stockInfo.stream().map(Sector::getTicker).collect(
                Collectors.toList());

    }

    public Map<String, List<String>> getTickerGroupBySector() {
        if (tickerGroupBySector == null) {
            tickerGroupBySector = initTickerGroupBySector();
        }
        return tickerGroupBySector;
    }

    public Map<String, List<String>> initTickerGroupBySector() {
        Map<String, List<String>> result = new HashMap<>();
        for (int i = 1; i < 30; i++) {
            List<Sector> stockInfo = vietStockClient.getStockInfo(i);
            if (!stockInfo.isEmpty()) {
                log.info("sector name ===> {}", stockInfo.get(0).getGroup());
                result.put(stockInfo.get(0).getGroup(), stockInfo.stream().map(Sector::getTicker).collect(
                        Collectors.toList()));
            }
        }
        return result;
    }
}

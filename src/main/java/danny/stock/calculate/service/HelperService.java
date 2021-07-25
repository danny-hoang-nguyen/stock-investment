package danny.stock.calculate.service;

import danny.stock.calculate.client.VietStockClient;
import danny.stock.calculate.document.SectorDocument;
import danny.stock.calculate.model.vietstock.Sector;
import danny.stock.calculate.repository.SectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class HelperService {

    @Autowired
    VietStockClient vietStockClient;


    @Autowired
    SectorRepository sectorRepository;


    Map<String, List<String>> tickerGroupBySector;
    List<Sector> stockInfo;

    public static final Integer BAN_BUON = 1;
    public static final Integer BAO_HIEM = 2;
    public static final Integer BDS = 3;
    public static final Integer QUY = 4;
    public static final Integer CHUNG_KHOAN = 5;
    public static final Integer CNNT = 6;
    public static final Integer BAN_LE = 7;
    public static final Integer CS_SK = 8;

    public static final Integer KHAI_KHOANG = 10;
    public static final Integer NGAN_HANG = 11;
    public static final Integer NONG_NGHIEP = 12;
    public static final Integer SX_MAY = 15;
    public static final Integer VAI = 16;
    public static final Integer CAO_SU = 17;
    public static final Integer NHUA = 18;
    public static final Integer THUC_PHAM = 19;
    public static final Integer THUY_SAN = 20;

    public static final Integer VLXD = 21;
    public static final Integer NHIET_DIEN = 22;
    public static final Integer VAN_TAI_KHO_BAI = 23;
    public static final Integer XAY_DUNG = 24;
    public static final Integer DV_LUUTRU_AN_UONG_GIAI_TRI = 25;
    public static final Integer SX_GO = 26;
    public static final Integer THIET_BI_DIEN = 27;
    public static final Integer TU_VAN_HO_TRO = 28;
    public static final Integer MOI_GIOI = 29;

    public List<Sector> getTickerGroupBySector(final Integer sectorId) {
        stockInfo = vietStockClient.getStockInfo(sectorId);
        return new ArrayList<>(stockInfo);

    }

    public Map<String, List<String>> getTickerGroupBySector() {
        if (tickerGroupBySector == null) {
            tickerGroupBySector = initTickerGroupBySector();
        }
        return tickerGroupBySector;
    }

    public Map<String, List<String>> initTickerGroupBySector() {
        Map<String, List<String>> result = new HashMap<>();
        long allSectorsSize = sectorRepository.findAll().stream().count();

        if (allSectorsSize == 0) {
            for (int i = 1; i < 30; i++) {
                List<Sector> stockInfo = vietStockClient.getStockInfo(i);
                for (Sector s: stockInfo) {
                    createFromSector(s);
                }
                if (!stockInfo.isEmpty()) {
                    result.put(stockInfo.get(0).getGroup(), stockInfo.stream().map(Sector::toString).collect(
                            Collectors.toList()));
                }
            }
        }
        else {
            Map<String, List<SectorDocument>> map = sectorRepository.findAll().stream().collect(groupingBy(SectorDocument::getGroup));
            map.forEach((key, value) -> result.put(key, value.stream().map(this::createFromSectorDocument).map(Sector::toString).collect(Collectors.toList())));
        }

        return result;
    }

    private SectorDocument createFromSector(final Sector sector) {
        SectorDocument sectorDocument = new SectorDocument();
        sectorDocument.setCapital(sector.getCapital());
        sectorDocument.setGroup(sector.getGroup());
        sectorDocument.setTicker(sector.getTicker());
        return sectorRepository.save(sectorDocument);
    }

    private Sector createFromSectorDocument(final SectorDocument sectorDocument) {
        Sector sector = new Sector();
        sector.setCapital(sectorDocument.getCapital());
        sector.setGroup(sectorDocument.getGroup());
        sector.setTicker(sectorDocument.getTicker());
        return sector;
    }
}

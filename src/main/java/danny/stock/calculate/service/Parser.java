package danny.stock.calculate.service;

import danny.stock.calculate.document.TickerFigureDocument;
import danny.stock.calculate.model.cp68.SectorFigure;
import danny.stock.calculate.model.vietstock.TickerFigure;
import danny.stock.calculate.repository.Cp68SectorFigureRepository;
import danny.stock.calculate.repository.TickerFigureRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class Parser {

    private static final String SO_THU_TU = "STT";
    private static final String NHOM_NGANH = "Nhóm Ngành";
    private static final String TANG_GIAM = "+/-";
    private static final String EPS = "EPS";
    private static final String PE = "PE";
    private static final String ROA = "ROA";
    private static final String ROE = "ROE";

    private static final String GIA_TRUNG_BINH = "Giá TB";
    private static final String GIA_SO_SACH = "Giá SS";
    private static final String GIA_THAT_CHIA_GIA_SO_SACH = "P/B";
    private static final String BETA = "Beta";
    private static final String TONG_KL = "Tổng KL";
    private static final String NN_SO_HUU = "NN SởHữu";
    private static final String VON_THI_TRUONG = "Vốn TT (Tỷ)";

    private static final String MA_CO_PHIEU = "Mã CK";

    private static final Map<Integer, String> SECTOR_HEADERS = new HashMap<>(15);
    private static final Map<Integer, String> TICKER_HEADERS = new HashMap<>(15);
    private static final String SECTOR = "sector";
    private static final String TICKER = "ticker";
    private static final String HTTP_WWW_COPHIEU_68_VN_CATEGORYLIST_PHP = "http://www.cophieu68.vn/categorylist.php";
    private static final String HTTP_WWW_COPHIEU_68_VN_CATEGORYLIST_DETAIL_PHP_CATEGORY = "http://www.cophieu68.vn/categorylist_detail.php?category=%5E";
    private static final String HTTP_FINANCE_VIETSTOCK_VN = "http://finance.vietstock.vn/";
    private static final String TAI_CHINH = "/tai-chinh.htm";


    @Autowired
    private TickerFigureRepository tickerFigureRepository;

    @Autowired
    private Cp68SectorFigureRepository sectorFigureRepository;

    private List<SectorFigure> sectorFigures = new ArrayList<>();

    public List<TickerFigure> retrieveSectorData() throws IOException {
        // [{0=STT, 1=Nhóm Ngành, 2=+/-, 3=EPS, 4=PE, 5=ROA, 6=ROE, 7=Giá TB, 8=Giá SS, 9=P/B, 10=Beta, 11=Tổng KL, 12=NN SởHữu, 13=Vốn TT (Tỷ)}]
        List<String> label = new ArrayList<>();
        Document doc = Jsoup.connect(HTTP_WWW_COPHIEU_68_VN_CATEGORYLIST_PHP).get();
        Elements content = doc.getElementsByClass("td_bottom3");
        Elements headers = doc.getElementsByClass("tr_header");

        for (Element headline : headers) {
            Elements elementsByAttributeValue = headline.getElementsByAttributeValue("align", "right");

            for (Element e : elementsByAttributeValue) {
                label.add(e.text());
            }
        }

        Elements ticker = doc.getElementsByAttributeValue("width", "80");
        label.add(1, ticker.text());

        int size = label.size();

        if (SECTOR_HEADERS.isEmpty()) {
            for (int i = 0; i < size; i++) {
                SECTOR_HEADERS.put(i, label.get(i));
            }
        }

        List<TickerFigure> veryFinalResult = new ArrayList<>();

        for (int i = 0; i < content.size(); i = i + size) {
            ArrayList<String> strings = new ArrayList<>();
            for (int adx = 0; adx < size; adx++) {
                if (i + adx == content.size()) break;
                strings.add(content.get(adx + i).text());
            }
            String stt = strings.get(getIdxOfPropertyFromSector(SO_THU_TU, SECTOR));
            String code = strings.get(getIdxOfPropertyFromSector(NHOM_NGANH, SECTOR));
            String tanggiam = strings.get(getIdxOfPropertyFromSector(TANG_GIAM, SECTOR));
            String eps = strings.get(getIdxOfPropertyFromSector(EPS, SECTOR));
            String pe = strings.get(getIdxOfPropertyFromSector(PE, SECTOR));
            String roa = strings.get(getIdxOfPropertyFromSector(ROA, SECTOR));
            String roe = strings.get(getIdxOfPropertyFromSector(ROE, SECTOR));
            String giatb = strings.get(getIdxOfPropertyFromSector(GIA_TRUNG_BINH, SECTOR));
            String giasosach = strings.get(getIdxOfPropertyFromSector(GIA_SO_SACH, SECTOR));
            String giathatchiagiasosach = strings.get(getIdxOfPropertyFromSector(GIA_THAT_CHIA_GIA_SO_SACH, SECTOR));
            String beta = strings.get(getIdxOfPropertyFromSector(BETA, SECTOR));
            String tongkhoiluong = strings.get(getIdxOfPropertyFromSector(TONG_KL, SECTOR));
            String nnsohuu = strings.get(getIdxOfPropertyFromSector(NN_SO_HUU, SECTOR));
            String vonthitruong = strings.get(getIdxOfPropertyFromSector(VON_THI_TRUONG, SECTOR));

            String tenNganh = code.substring(code.indexOf("^") + 1);

            SectorFigure sectorFigure = new SectorFigure();
            sectorFigure.setStt(stt);
            sectorFigure.setTennganh(tenNganh);
            sectorFigure.setTanggiam(tanggiam);
            sectorFigure.setEps(eps);
            sectorFigure.setPe(pe);
            sectorFigure.setRoa(roa);
            sectorFigure.setRoe(roe);
            sectorFigure.setGiatb(giatb);
            sectorFigure.setGiasosach(giasosach);
            sectorFigure.setPb(giathatchiagiasosach);
            sectorFigure.setBeta(beta);
            sectorFigure.setTongkl(tongkhoiluong);
            sectorFigure.setNnsohuu(nnsohuu);
            sectorFigure.setVonthitruong(vonthitruong);
            sectorFigureRepository.save(sectorFigure);


            log.info("=== Enter sector [{}] ===", tenNganh);
            List<Vector> tickerAndRoe = getTickerAndRoe(tenNganh);
            for (Vector c : tickerAndRoe) {
                String tickerName = (String) c.get(0);
                TickerFigure tickerFigure = extractFromVietStock(tickerName);
                tickerFigure.setRoe((Double) c.get(1));
                tickerFigure.setCode(tickerName);
                tickerFigure.setSector(tenNganh);
                veryFinalResult.add(tickerFigure);
                TickerFigureDocument fromTickerFigure = createFromTickerFigure(tickerFigure);
                tickerFigureRepository.save(fromTickerFigure);
            }
            log.info("=== [{}] tickers were processed ===", tickerAndRoe.size());
        }
        return veryFinalResult;
    }

    public List<String> retrieveJustSectorName() throws IOException {
        // [{0=STT, 1=Nhóm Ngành, 2=+/-, 3=EPS, 4=PE, 5=ROA, 6=ROE, 7=Giá TB, 8=Giá SS, 9=P/B, 10=Beta, 11=Tổng KL, 12=NN SởHữu, 13=Vốn TT (Tỷ)}]
        List<String> label = new ArrayList<>();
        Document doc = Jsoup.connect(HTTP_WWW_COPHIEU_68_VN_CATEGORYLIST_PHP).get();
        Elements content = doc.getElementsByClass("td_bottom3");
        Elements headers = doc.getElementsByClass("tr_header");

        for (Element headline : headers) {
            Elements elementsByAttributeValue = headline.getElementsByAttributeValue("align", "right");

            for (Element e : elementsByAttributeValue) {
                label.add(e.text());
            }
        }

        Elements ticker = doc.getElementsByAttributeValue("width", "80");
        label.add(1, ticker.text());

        int size = label.size();

        if (SECTOR_HEADERS.isEmpty()) {
            for (int i = 0; i < size; i++) {
                SECTOR_HEADERS.put(i, label.get(i));
            }
        }

        List<String> sectors = new ArrayList<>();

        for (int i = 0; i < content.size(); i = i + size) {
            ArrayList<String> strings = new ArrayList<>();
            for (int adx = 0; adx < size; adx++) {
                if (i + adx == content.size()) break;
                strings.add(content.get(adx + i).text());
            }
            String code = strings.get(getIdxOfPropertyFromSector(NHOM_NGANH, SECTOR));
            String tenNganh = code.substring(code.indexOf("^") + 1);
            sectors.add(tenNganh);
        }
        return sectors;
    }

    public List<Vector> getTickerAndRoe(String sector) throws IOException {
        // [{0=STT, 1=Mã CK, 2=+/-, 3=EPS, 4=PE, 5=ROA, 6=ROE, 7=Giá SS, 8=P/B, 9=Beta, 10=Tổng KL, 11=NN SởHữu, 12=Vốn TT (Tỷ)}]
        List<String> label = new ArrayList<>();
        String url = HTTP_WWW_COPHIEU_68_VN_CATEGORYLIST_DETAIL_PHP_CATEGORY + sector;
        Document doc = Jsoup.connect(url).get();
        Elements content = doc.getElementsByClass("td_bottom");
        Elements headers = doc.getElementsByClass("tr_header");

        for (Element headline : headers) {
            Elements elementsByAttributeValue = headline.getElementsByAttributeValue("align", "right");

            for (Element e : elementsByAttributeValue) {
                if (e.text().length() != 0)
                    label.add(e.text());
            }
        }
        Elements ticker = doc.getElementsByAttributeValue("width", "60");
        label.add(1, ticker.text());

        int size = label.size();

        if (TICKER_HEADERS.isEmpty()) {
            for (int i = 0; i < size; i++) {
                TICKER_HEADERS.put(i, label.get(i));
            }
        }

        List<Vector> tickerAndRoe = new ArrayList<>();

        int i1 = label.size();
        for (int i = 0; i < content.size(); i = i + i1) {
            ArrayList<String> strings = new ArrayList<>();
            for (int adx = 0; adx < i1; adx++) {
                if (i + adx == content.size()) break;
                String text = content.get(adx + i).text();
                strings.add(text);
            }
            String code = strings.get(getIdxOfPropertyFromSector(MA_CO_PHIEU, TICKER));
            String roe = strings.get(getIdxOfPropertyFromSector(ROE, TICKER));

            double roeInNumber = convertROEFromStringToPercentage(roe);

            Vector v = new Vector();
            v.add(0, code);
            v.add(1, roeInNumber);
            tickerAndRoe.add(v);
        }
        return tickerAndRoe;
    }

    public TickerFigure extractFromVietStock(String ticker) throws IOException {
        Document doc = Jsoup.connect(HTTP_FINANCE_VIETSTOCK_VN + ticker + TAI_CHINH).get();
        Elements elementsByClass = doc.getElementsByClass("col-xs-12 col-sm-4 col-md-4 col-c-last");
        int movingIndex = 0;
        TickerFigure tickerFigure = new TickerFigure();
        for (Element e : elementsByClass) {
            Elements value = e.getElementsByClass("pull-right");
            Elements key = e.getElementsByClass("p8");

            for (Element figure : value) {
                String label = key.text().substring(movingIndex, key.text().indexOf(figure.text(), movingIndex));
                movingIndex = movingIndex + label.length() + figure.text().length();
                createTickerFigure(label.trim(), figure.text(), tickerFigure);
            }
        }
        return tickerFigure;
    }

    private int getIdxOfPropertyFromSector(String propertyName, String sectorOrTicker) {
        if (sectorOrTicker.equalsIgnoreCase(SECTOR))
            return SECTOR_HEADERS.entrySet().stream().filter(integerStringEntry -> integerStringEntry.getValue().equalsIgnoreCase(propertyName)).findFirst().get().getKey();
        return TICKER_HEADERS.entrySet().stream().filter(integerStringEntry -> integerStringEntry.getValue().equalsIgnoreCase(propertyName)).findFirst().get().getKey();
    }

    private double convertROEFromStringToPercentage(String roe) {
        String number = roe.substring(0, roe.indexOf("%")).replaceAll(",", "");
        return Double.parseDouble(number) / 100;
    }

    private void createTickerFigure(String key, String value, TickerFigure tickerFigure) {
        switch (key) {
            case "EPS*":
                tickerFigure.setEps(handleNumber(value));
                break;

            case "P/E":
                tickerFigure.setPe(handleNumber(value));
                break;

            case "F P/E":
                tickerFigure.setFpe(handleNumber(value));
                break;

            case "BVPS":
                tickerFigure.setBvps(handleNumber(value));
                break;

            case "P/B":
                tickerFigure.setPb(handleNumber(value));
                break;

            default:
                log.error("Not found property [{}] for value [{}]", key, value);

        }
    }

    private TickerFigureDocument createFromTickerFigure(TickerFigure tickerFigure) {
        TickerFigureDocument tickerFigureDocument = new TickerFigureDocument();
        tickerFigureDocument.setBvps(tickerFigure.getBvps());
        tickerFigureDocument.setCode(tickerFigure.getCode());
        tickerFigureDocument.setEps(tickerFigure.getEps());
        tickerFigureDocument.setPe(tickerFigure.getPe());
        tickerFigureDocument.setFpe(tickerFigure.getFpe());
        tickerFigureDocument.setPb(tickerFigure.getPb());
        tickerFigureDocument.setRoe(tickerFigure.getRoe());
        tickerFigureDocument.setCreated(LocalDateTime.now());
        tickerFigureDocument.setSector(tickerFigure.getSector());
        return tickerFigureDocument;
    }

    private Double handleNumber(String value) {
        Number figure = -1.0;
        try {
            figure = NumberFormat.getNumberInstance(Locale.US).parse(value);
        } catch (ParseException e) {
            log.error("Error parsing figure [{}] for value [{}]", figure, value);
        }
        return figure.doubleValue();
    }
}

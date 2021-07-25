package danny.stock.calculate.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class Parser {

    private static final Map<Integer, String> SECTOR_HEADERS = new HashMap<>(15);
    private static final Map<Integer, String> TICKER_HEADERS = new HashMap<>(15);
    public  List<ArrayList<String>> retrieveSectorData() throws IOException {
        // [{0=STT, 1=Nhóm Ngành, 2=+/-, 3=EPS, 4=PE, 5=ROA, 6=ROE, 7=Giá TB, 8=Giá SS, 9=P/B, 10=Beta, 11=Tổng KL, 12=NN SởHữu, 13=Vốn TT (Tỷ)}]
        List<String> label = new ArrayList<>();
        Document doc = Jsoup.connect("http://www.cophieu68.vn/categorylist.php").get();
        Elements newsHeadlines = doc.getElementsByClass("td_bottom3");
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
        log.info("Header's size [{}]", size);

        if (SECTOR_HEADERS.isEmpty()) {
            for (int i =0; i< size;i++) {
                SECTOR_HEADERS.put(i, label.get(i));
            }
        }
        log.info("Label map [{}]", SECTOR_HEADERS);

        List<ArrayList<String>> veryFinalResult = new ArrayList<>();

        for (int i = 0; i < newsHeadlines.size(); i = i + size) {
            ArrayList<String> strings = new ArrayList<>();
            for (int adx = 0; adx < size; adx++) {
                if (i + adx == newsHeadlines.size()) break;
                strings.add(newsHeadlines.get(adx + i).text());
            }
            String eps = strings.get(getIdxOfPropertyFromSector("EPS", "sector"));
            String code = strings.get(getIdxOfPropertyFromSector("Nhóm Ngành", "sector"));
            log.info("Ngành - EPS ::: [{}] - [{}]", code.substring(code.indexOf("^")+1), eps);
            veryFinalResult.add(strings);
        }
        return veryFinalResult;
    }

    public  List<ArrayList<String>> retrieveTickerDataBasedOnSector(String sector) throws IOException {
        // [{0=STT, 1=Mã CK, 2=+/-, 3=EPS, 4=PE, 5=ROA, 6=ROE, 7=Giá SS, 8=P/B, 9=Beta, 10=Tổng KL, 11=NN SởHữu, 12=Vốn TT (Tỷ)}]
        List<String> label = new ArrayList<>();
        String url = "http://www.cophieu68.vn/categorylist_detail.php?category=%5E" + "bds";
        Document doc = Jsoup.connect(url).get();
        Elements newsHeadlines = doc.getElementsByClass("td_bottom");
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
        log.info("Header's size [{}]", size);


        if (TICKER_HEADERS.isEmpty()) {
            for (int i =0; i< size;i++) {
                TICKER_HEADERS.put(i, label.get(i));
            }
        }
        log.info("Label map [{}]", TICKER_HEADERS);

        List<ArrayList<String>> veryFinalResult = new ArrayList<>();

        int i1 = label.size();
        for (int i = 0; i < newsHeadlines.size(); i = i + i1) {
            ArrayList<String> strings = new ArrayList<>();
            for (int adx = 0; adx < i1; adx++) {
                if (i + adx == newsHeadlines.size()) break;
                String text = newsHeadlines.get(adx + i).text();
                strings.add(text);
            }
            String eps = strings.get(getIdxOfPropertyFromSector("EPS", "ticker"));
            String code = strings.get(getIdxOfPropertyFromSector("Mã CK", "ticker"));
            String roe = strings.get(getIdxOfPropertyFromSector("ROE", "ticker"));
            log.info("ticker - EPS - ROE ::: [{}] - [{}] - [{}]", code, eps, roe);
            veryFinalResult.add(strings);
        }
        return veryFinalResult;
    }

    private int getIdxOfPropertyFromSector(String propertyName, String sectorOrTicker) {
        if (sectorOrTicker.equalsIgnoreCase("sector"))
            return SECTOR_HEADERS.entrySet().stream().filter(integerStringEntry -> integerStringEntry.getValue().equalsIgnoreCase(propertyName)).findFirst().get().getKey();
        return TICKER_HEADERS.entrySet().stream().filter(integerStringEntry -> integerStringEntry.getValue().equalsIgnoreCase(propertyName)).findFirst().get().getKey();
    }
}

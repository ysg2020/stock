package zerobase.stock.scraper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.stock.model.Company;
import zerobase.stock.model.Dividend;
import zerobase.stock.model.ScrapedResult;
import zerobase.stock.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class YahooFinanceScraper implements Scraper{

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    public static final long START_TIME = 86400; // 60 * 60 * 24 => 1일

    @Override
    public ScrapedResult scrap(Company company){
        log.info("YahooFinanceScraper scrap -> "+company.getName());
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);
        try{
            long now = System.currentTimeMillis() / 1000; //초 단위로 바꾸기 위해 1000으로 나누어줌
            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); //table 전체

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for(Element e : tbody.children()){
                String txt = e.text();
                if(!txt.endsWith("Dividend")){
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if(month < 0){
                    throw new RuntimeException("Unexpected Month enum value -> "+ splits[0]);
                }
                dividends.add(Dividend.builder()
                        .date(LocalDateTime.of(year,month,day,0,0))
                        .dividend(dividend)
                        .build());

//                System.out.println(year +"/"+month+"/"+day+" -> "+dividend);
            }
            scrapResult.setDividends(dividends);

        }catch (IOException e){
            e.printStackTrace();
        }


        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker){
        String url = String.format(SUMMARY_URL, ticker, ticker);
        try{
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            //String title = titleEle.text().split("-")[1].trim();
            String title = titleEle.text();
            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();
        } catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }
}

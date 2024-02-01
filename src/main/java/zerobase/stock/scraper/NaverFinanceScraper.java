package zerobase.stock.scraper;

import org.springframework.stereotype.Component;
import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapedResult;
//@Component
public class NaverFinanceScraper implements Scraper{
    @Override
    public ScrapedResult scrap(Company company) {
        System.out.println(" NAVER scrap!!");
        return null;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        System.out.println(" NAVER scrapCompanyByTicker!!");
        return null;
    }
}

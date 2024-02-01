package zerobase.stock.scraper;

import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapedResult;

public interface Scraper {

    ScrapedResult scrap(Company company);

    Company scrapCompanyByTicker(String ticker);
}

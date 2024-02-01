package zerobase.stock.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapedResult;
import zerobase.stock.model.constants.CacheKey;
import zerobase.stock.persist.CompanyRepository;
import zerobase.stock.persist.DividendRepository;
import zerobase.stock.persist.entity.CompanyEntity;
import zerobase.stock.persist.entity.DividendEntity;
import zerobase.stock.scraper.Scraper;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper scraper;

    //@Scheduled(fixedDelay = 1000)
    public void test1() throws InterruptedException{
        Thread.sleep(10000); // 10초간 일시정지
        System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : "+ LocalDateTime.now());
    }
    //@Scheduled(fixedDelay = 1000)
    public void test2() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : "+ LocalDateTime.now());
    }


    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = companyRepository.findAll();
        // 회사마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            log.info("scraping scheduler is started -> "+company.getName());
            ScrapedResult scrapedResult = scraper.scrap(Company.builder()
                    .name(company.getName())
                    .ticker(company.getTicker()).build());
            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            //디비든 모델을 디비든 엔티티로 매핑
            scrapedResult.getDividends().stream()
                    .map(e-> new DividendEntity(company.getId(),e))
                    //엘리먼트를 하나씩 디비든 레파지토리에 삽입 (존재하지 않는 경우에만)
                    .forEach(e ->{
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(),e.getDate());
                        if(!exists){
                            dividendRepository.save(e);
                        }
                    });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }

    }
}

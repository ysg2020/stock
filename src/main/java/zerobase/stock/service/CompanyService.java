package zerobase.stock.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.stock.exception.impl.NoCompanyException;
import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapedResult;
import zerobase.stock.persist.CompanyRepository;
import zerobase.stock.persist.DividendRepository;
import zerobase.stock.persist.entity.CompanyEntity;
import zerobase.stock.persist.entity.DividendEntity;
import zerobase.stock.scraper.Scraper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper scraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    public Company save(String ticker){
        log.info("[CompanyService] save -> "+ticker);
        boolean exists = companyRepository.existsByTicker(ticker);
        if(exists){
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        log.info("[CompanyService] getAllCompany");
        return companyRepository.findAll(pageable);
    }
    private Company storeCompanyAndDividend(String ticker){
        log.info("[CompanyService] storeCompanyAndDividend -> "+ticker);
        //ticker 를 기준으로 회사를 스크래핑
        Company company = scraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker ->"+ticker);
        }
        //해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = scraper.scrap(company);
        //스크래핑 결과

        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());
        dividendRepository.saveAll(dividendEntityList);
        return company;
    }

    public void addAutocompleteKeyword(String keyword){
        log.info("[CompanyService] addAutocompleteKeyword -> "+keyword);
        trie.put(keyword, null);
    }
    public List<String> autocomplete(String keyword){
        log.info("[CompanyService] autocomplete -> "+keyword);
        return (List<String>) trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    public void deleteAutocompleteKeyword(String keyword){
        log.info("[CompanyService] deleteAutocompleteKeyword -> "+keyword);
        trie.remove(keyword);
    }
    public List<String> getCompanyNamesByKeyword(String keyword){
        log.info("[CompanyService] getCompanyNamesByKeyword -> "+keyword);
        Pageable limit = PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(keyword,limit);
        return companyEntities.stream()
                .map(e->e.getName())
                .collect(Collectors.toList());
    }

    public String deleteCompany(String ticker) {
        log.info("[CompanyService] deleteCompany -> "+ticker);
        CompanyEntity company = companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        dividendRepository.deleteAllByCompanyId(company.getId());
        companyRepository.delete(company);
        deleteAutocompleteKeyword(company.getName());
        return company.getName();

    }
}

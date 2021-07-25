package danny.stock.calculate.repository;

import danny.stock.calculate.document.TickerForCalculation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TickerDetailRepository extends MongoRepository<TickerForCalculation, String> {

    List<TickerForCalculation> findByTradingDateBetweenAndPeriodEqualsAndTickerEquals(LocalDateTime from, LocalDateTime to, String period, String ticker);
}

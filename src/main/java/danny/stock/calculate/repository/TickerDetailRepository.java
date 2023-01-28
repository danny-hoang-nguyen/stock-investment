package danny.stock.calculate.repository;

import danny.stock.calculate.document.TickerForCalculation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TickerDetailRepository extends MongoRepository<TickerForCalculation, String> {

    List<TickerForCalculation> findByTradingDateBetweenAndPeriodEqualsAndTickerEquals(LocalDateTime from, LocalDateTime to, String period, String ticker);
    @Aggregation(pipeline = { "{ '$group': { '_id' : '$ticker' } }" })
    List<String> findDistinctTickers();
}

package danny.stock.calculate.repository;

import danny.stock.calculate.document.TickerDetailDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TickerDetailRepository extends MongoRepository<TickerDetailDocument, String> {

    List<TickerDetailDocument> findByTradingDateBetweenAndPeriodEqualsAndTickerEquals(LocalDateTime from, LocalDateTime to, String period, String ticker);
}

package danny.stock.calculate.repository;

import danny.stock.calculate.document.TickerFigureDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TickerFigureRepository extends MongoRepository<TickerFigureDocument, String> {
}

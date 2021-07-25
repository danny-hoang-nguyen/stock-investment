package danny.stock.calculate.repository;

import danny.stock.calculate.document.SectorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorRepository extends MongoRepository<SectorDocument, String> {

}

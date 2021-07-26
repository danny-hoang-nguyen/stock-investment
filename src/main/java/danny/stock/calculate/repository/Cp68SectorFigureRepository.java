package danny.stock.calculate.repository;

import danny.stock.calculate.model.cp68.SectorFigure;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Cp68SectorFigureRepository extends MongoRepository<SectorFigure, String> {
}

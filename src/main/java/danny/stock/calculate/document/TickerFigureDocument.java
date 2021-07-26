package danny.stock.calculate.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
public class TickerFigureDocument {

    @Id
    private String id;
    private Double eps;
    private Double pe;
    private Double fpe;
    private Double bvps;
    private Double pb;
    private Double roe;
    private String code;
    private String sector;
    private LocalDateTime created;
}

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
    private String eps;
    private String pOverE;
    private String fAndPOverE;
    private String bvps;
    private String pOverB;
    private Double roe;
    private String code;
    private LocalDateTime created;
}

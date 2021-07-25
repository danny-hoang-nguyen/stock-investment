package danny.stock.calculate.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Document
@Data
public class TickerForCalculation {
    @Id
    private String id;
    private String group;
    private String ticker;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private BigInteger volume;
    private LocalDateTime tradingDate;
    private String period;
}

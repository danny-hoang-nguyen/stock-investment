package danny.stock.calculate.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class SectorDocument {
    @Id
    private String id;

    private String ticker;
    private String group;
    private Double capital;
}

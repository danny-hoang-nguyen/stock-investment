package danny.stock.calculate.model.cp68;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class SectorFigure {
    @Id
    private String id;
    private String stt;
    private String tennganh;
    private String tanggiam;
    private String eps;
    private String pe;
    private String roa;
    private String roe;
    private String giatb;
    private String giasosach;
    private String pb;
    private String beta;
    private String tongkl;
    private String nnsohuu;
    private String vonthitruong;
}

package danny.stock.calculate.model.vietstock;

import lombok.Data;

@Data
public class TickerFigure {

    private Double eps;
    private Double pe;
    private Double fpe;
    private Double bvps;
    private Double pb;
    private Double roe;
    private String sector;
    private String code;
}

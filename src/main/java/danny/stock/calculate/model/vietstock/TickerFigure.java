package danny.stock.calculate.model.vietstock;

import lombok.Data;

@Data
public class TickerFigure {

    private String eps;
    private String pOverE;
    private String fAndPOverE;
    private String bvps;
    private String pOverB;
    private Double roe;
    private String code;
}

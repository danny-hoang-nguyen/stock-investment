package danny.stock.calculate.client;

import feign.Logger;
import feign.Logger.Level;
import feign.Request.Options;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TcbsClientConfig {

  @Bean
  public Options options() {
    return new Options(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Level.FULL;
  }

  @Bean
  Retryer retryer() {
    return Retryer.NEVER_RETRY;
  }
}

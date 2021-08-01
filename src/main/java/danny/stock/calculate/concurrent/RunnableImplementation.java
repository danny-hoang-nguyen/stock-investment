package danny.stock.calculate.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableImplementation implements Runnable {
    private Logger logger
            = LoggerFactory.getLogger(RunnableImplementation.class);

    @Override
    public void run() {
        logger.info("Message - [{}]", Thread.currentThread().getName());
    }
}

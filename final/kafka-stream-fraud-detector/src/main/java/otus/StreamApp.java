package otus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class StreamApp {

    private static final Logger logger = LoggerFactory.getLogger(StreamApp.class);


    public static void main(String[] args) {

        KafkaStream myKafkaStream = new KafkaStream(getPropsValues());

        logger.info("Kafka Streams Started");
        myKafkaStream.maliciousUserNotFound().start();
        myKafkaStream.maliciousInvalidUserCredentials().start();
        myKafkaStream.newUserLocationTopology().start();
    }

    static Properties getPropsValues() {
        Properties prop = new Properties();
        try (InputStream input = StreamApp.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
                return prop;
            }
            logger.error("unable to find config.properties");
        } catch (IOException ex) {
            logger.error("error when reading properties: {}", ex.getMessage());
        }
        return prop;
    }
}



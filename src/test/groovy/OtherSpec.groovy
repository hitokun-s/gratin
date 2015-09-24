import groovy.util.logging.Log4j
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
@Log4j
class OtherSpec extends Specification {

    def "can refer data file in resource dir"(){
        given:
            ClassLoader classLoader = getClass().getClassLoader();
        when:
            File file = new File(classLoader.getResource("data/iris.data.txt").getFile());
        then:
            file.exists()
    }
}
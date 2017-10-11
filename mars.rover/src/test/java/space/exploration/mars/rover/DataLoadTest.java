package space.exploration.mars.rover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DataLoadTest {

    public static void main(String[] args) {
        try (Stream<Path> paths = Files.walk(Paths.get("/OtherData"))) {
            paths.filter(Files::isRegularFile).forEach(System.out::println);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}

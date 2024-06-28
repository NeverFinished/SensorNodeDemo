package sensornodedemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StdInReader {
    public static void pass(Consumer<String> c) {
        new Thread(() -> {
            try {
                var bis = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = bis.readLine()) != null) {
                    System.out.println(line);
                    c.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}

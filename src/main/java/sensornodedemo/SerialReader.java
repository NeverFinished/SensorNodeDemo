package sensornodedemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import com.fazecast.jSerialComm.SerialPort;

public class SerialReader {

    private final boolean debug;

    public SerialReader(boolean debug) {
        this.debug = debug;
    }

    public void connect(Consumer<String> c) {

        SerialPort[] ports = SerialPort.getCommPorts();
        Optional<SerialPort> port = Arrays.stream(ports).filter(s -> s.getSystemPortPath().contains("cu.usbmodem")).findFirst();
        if (port.isEmpty()) {
            System.err.println("No port found, exiting. list=" + Arrays.asList(ports));
            System.exit(1);
        }
        SerialPort comPort = port.get();
        comPort.setBaudRate(115200);
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        new Thread(() -> {
            try {
                var bis = new BufferedReader(new InputStreamReader(comPort.getInputStream()));
                String line;
                while ((line = bis.readLine()) != null) {
                    if (debug) {
                        System.out.println(line);
                    }
                    c.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                comPort.closePort();
            }
        }).start();
    }
}

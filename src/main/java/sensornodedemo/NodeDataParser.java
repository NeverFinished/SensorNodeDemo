package sensornodedemo;

import java.awt.geom.Point2D;

public class NodeDataParser {

    static NodeData parse(String sensorLine) {
        String[] tokens = sensorLine.split(" ");
        if (tokens.length > 9) {
            System.err.println("OLD version, cannot parse: " + sensorLine);
            return null;
        }
        if (tokens.length < 8) { // with GW its 9, 8 otherwise
            System.err.println("cannot parse: " + sensorLine);
            return null;
        }
        try {
            int accX = Integer.parseInt(tokens[1]);
            int accY = Integer.parseInt(tokens[2]);
            int accZ = Integer.parseInt(tokens[3]);
            int temp = Integer.parseInt(tokens[4]) - 5;
            int buttonA = Integer.parseInt(tokens[5]);
            int buttonB = Integer.parseInt(tokens[6]);
            int touchLogo = Integer.parseInt(tokens[7]);
            int rssi = -17;
            if (tokens.length > 8) {
                rssi = Integer.parseInt(tokens[8]);
            }

            var nd = new NodeData(accX, accY, accZ, temp, buttonA, buttonB, touchLogo, rssi);
            nd.enrich();
            return nd;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing: line='" + sensorLine + "', err=" + e.getMessage());
            return null;
        }
    }

    static class NodeData {

        public NodeData(int accX, int accY, int accZ, int temp, int buttonA, int buttonB, int touchLogo, int rssi) {
            this.accX = accX;
            this.accY = accY;
            this.accZ = accZ;
            this.temp = temp;
            this.buttonA = buttonA;
            this.buttonB = buttonB;
            this.touchLogo = touchLogo;
            this.rssi = rssi;
        }

        long timeStamp = System.currentTimeMillis();

        // direct values
        int accX, accY, accZ;
        int temp;
        int buttonA, buttonB;
        int touchLogo, rssi = - 70;

        // indirect values
        double pitch, roll;

        void enrich() {
            double x = accX / 100d;
            double y = accY / 100d;
            double z = accZ / 100d;
            double roll = Math.atan2(y, Math.sqrt(x * x + z * z));
            double pitch = Math.atan2(-x, Math.sqrt(y * y + z * z));

            // Convert from radians to degrees and swap values(!)
            this.pitch = Math.toDegrees(roll);
            this.roll = Math.toDegrees(pitch);
        }

        public boolean anyButtonPressed() {
            return buttonA + buttonB > 0; // 20240625 - touch excluded, some nodes have a stuck touch
        }

        // rssi is in range -70 to -30. more is better
        public float rssiScaled() {
            return (rssi+70)*5+90;
        }

        public Point2D.Float maybeApply(Point2D.Float moveVec) {
            if (anyButtonPressed() || moveVec == null) {
                float dx = (float) (-1 * roll / 8);
                float dy = (float) (pitch / 8);
                return new Point2D.Float(dx, dy);
            }
            return moveVec;
        }
    }

}

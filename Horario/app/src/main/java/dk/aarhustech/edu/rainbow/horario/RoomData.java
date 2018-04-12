package dk.aarhustech.edu.rainbow.horario;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
class RoomData {
    String name;
    private ArrayList<ArrayList<AP>> dataPoints;

    RoomData(String name) {
        this.name = name;
        dataPoints = new ArrayList<>();
    }

    void add(ArrayList<AP> aps) {
        if (!dataPoints.contains(aps)) dataPoints.add(aps);
    }

    int size() {
        return dataPoints.size();
    }

    static class AP {
        private String mac;
        private int rssi;

        AP(String mac, int rssi) {
            this.mac = mac;
            this.rssi = rssi;
        }

        public boolean equals(Object o) {
            return (o instanceof AP) && Objects.equals(((AP) o).mac, this.mac) && (((AP) o).rssi == this.rssi);
        }

        public int hashCode() {
            return Objects.hash(this.mac, this.rssi);
        }
    }
}

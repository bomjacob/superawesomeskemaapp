package dk.aarhustech.edu.rainbow.horario;

public interface ShowFor {
    void showForRoom(String room, boolean force);
    void showForTeacher(String teacher, boolean force);
    void showForGroup(String group, boolean force);
}

package ca.wd4.sg.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Time {
    private int day;
    private int startHour;
    private int startMinu;
    private int endHour;
    private int endMinu;

    public Time(String day, String time) {
        this.day = day2Int(day);
        String startTime = time.split("-")[0];
        String endTime = time.split("-")[1];
        this.startHour = Integer.valueOf(startTime.split(":")[0]);
        this.startMinu = Integer.valueOf(startTime.split(":")[1]);
        this.endHour = Integer.valueOf(endTime.split(":")[0]);
        this.endMinu = Integer.valueOf(endTime.split(":")[1]);
    }

    public boolean hasConflict(Time that) {
        if (that.getDay()!=this.day) {
            return false;
        }
        if (this.endHour<that.getStartHour()) {
            return false;
        }
        if (this.endHour==that.getStartHour() && this.endMinu<that.getStartMinu()) {
            return false;
        }
        if (that.getEndHour()<this.startHour) {
            return false;
        }
        if (that.getEndHour()==this.startHour && that.getEndMinu()<this.startMinu) {
            return false;
        }
        return true;
    }

    public static int day2Int(String day) {
        switch (day) {
            case "Mo": return 1;
            case "Tu": return 2;
            case "We": return 3;
            case "Th": return 4;
            case "Fr": return 5;
            default: return -1;
        }
    }
}

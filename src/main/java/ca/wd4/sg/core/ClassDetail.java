package ca.wd4.sg.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassDetail {
    private int id;
    private String name;
    private int day;
    private String time;
    private String section;
}

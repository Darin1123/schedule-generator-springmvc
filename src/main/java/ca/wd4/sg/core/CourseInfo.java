package ca.wd4.sg.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseInfo {
    private long id;
    private String name;
    private int lecNum;
    private int tutNum;
    private int labNum;
}

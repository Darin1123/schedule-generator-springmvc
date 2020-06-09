package ca.wd4.sg.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CourseBasic {
    private long id;
    private String name;
    private List<String> lecs;
    private List<String> tuts;
    private List<String> labs;
}

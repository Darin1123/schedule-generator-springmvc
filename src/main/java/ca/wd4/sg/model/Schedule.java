package ca.wd4.sg.model;

import ca.wd4.sg.core.ClassDetail;
import ca.wd4.sg.core.CourseInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Schedule {
    List<CourseInfo> courseInfos;
    List<ClassDetail> table;
}

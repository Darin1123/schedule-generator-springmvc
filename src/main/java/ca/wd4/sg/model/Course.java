package ca.wd4.sg.model;


import ca.wd4.sg.core.CourseBasic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Course {
    private long id;
    private long uid;
    private String name;
    private String term;
    private String authorName;
    private long authorId;
    private int star;
    private List<String> lecs;
    private List<String> tuts;
    private List<String> labs;

    public CourseBasic getBasic() {
        return new CourseBasic(id, name, lecs, tuts, labs);
    }
}

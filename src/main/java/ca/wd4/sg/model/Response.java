package ca.wd4.sg.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Response<T> {
    private String message;
    private List<T> data;
}

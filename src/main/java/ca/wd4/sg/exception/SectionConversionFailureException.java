package ca.wd4.sg.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SectionConversionFailureException extends Exception {
    public SectionConversionFailureException(String message) {
        super(message);
    }
}

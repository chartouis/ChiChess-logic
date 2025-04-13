package com.chitas.jobquest.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Career {
    private String title;
    private String description;
    private List<String> responsibilities;
    private List<String> skills;
    private List<Task> tasks;
    private List<Quiz> quiz;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Task {
        private String description;
        private String difficulty;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quiz {
        private String question;
        private List<String> options;
        private String answer;
    }
}

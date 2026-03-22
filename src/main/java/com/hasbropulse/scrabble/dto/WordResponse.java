package com.hasbropulse.scrabble.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

// null fields are skipped in the JSON output — so "message" only shows up when there's no word
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response containing the best Scrabble word found")
public class WordResponse {

    @Schema(description = "The highest-scoring valid word found", example = "WIZARD")
    private String word;

    @Schema(description = "Scrabble score of the word", example = "19")
    private Integer score;

    @Schema(description = "Message when no word could be found", example = "No valid word found with the given tiles")
    private String message;

    public WordResponse() {}

    // use this when we found a valid word
    public WordResponse(String word, int score) {
        this.word = word;
        this.score = score;
    }

    // use this when nothing could be formed
    public WordResponse(String message) {
        this.message = message;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

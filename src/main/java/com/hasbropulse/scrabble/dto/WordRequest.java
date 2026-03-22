package com.hasbropulse.scrabble.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// request body for POST /api/scrabble/best-word
@Schema(description = "Request payload for finding the best Scrabble word")
public class WordRequest {

    @NotBlank(message = "rack is required and must not be blank")
    @Size(min = 1, max = 7, message = "rack must contain between 1 and 7 letters")
    @Pattern(regexp = "[A-Za-z]+", message = "rack must contain only alphabetic letters")
    @Schema(description = "Letters on the player's rack (1-7 characters, A-Z only)", example = "AIDOORW")
    private String rack;

    @Pattern(regexp = "[A-Za-z]*", message = "word must contain only alphabetic letters")
    @Schema(description = "Optional word already on the board", example = "WIZ")
    private String word;

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return "WordRequest{rack='" + rack + "', word='" + word + "'}";
    }
}

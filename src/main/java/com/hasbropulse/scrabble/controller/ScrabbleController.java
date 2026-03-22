package com.hasbropulse.scrabble.controller;

import com.hasbropulse.scrabble.dto.WordRequest;
import com.hasbropulse.scrabble.dto.WordResponse;
import com.hasbropulse.scrabble.service.ScrabbleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

// two endpoints:
//   POST /api/scrabble/best-word  — main word-finding logic
//   GET  /api/scrabble/health     — quick check that the service is up
@RestController
@RequestMapping("/api/scrabble")
@Tag(name = "Scrabble Word Builder", description = "Endpoints for the Hasbro Pulse Scrabble challenge")
public class ScrabbleController {

    private static final Logger log = LoggerFactory.getLogger(ScrabbleController.class);

    private final ScrabbleService scrabbleService;

    public ScrabbleController(ScrabbleService scrabbleService) {
        this.scrabbleService = scrabbleService;
    }

    @Operation(
        summary = "Find the highest-scoring Scrabble word",
        description = "Given a rack of up to 7 letters and an optional board word, returns the best " +
                      "valid Scrabble word that can be formed. Ties in score are broken alphabetically."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Best word found or no word could be formed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = WordResponse.class),
                examples = {
                    @ExampleObject(name = "Word found", value = "{\"word\":\"WIZARD\",\"score\":19}"),
                    @ExampleObject(name = "No word found", value = "{\"message\":\"No valid word found with the given tiles\"}")
                })),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"error\":\"Invalid input\",\"message\":\"rack must contain between 1 and 7 letters, but got 8\"}")))
    })
    @PostMapping(value = "/best-word", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WordResponse> findBestWord(@Valid @RequestBody WordRequest request) {
        log.info("Received request - rack='{}', boardWord='{}'", request.getRack(), request.getWord());

        Optional<Map.Entry<String, Integer>> result = scrabbleService.findBestWord(request.getRack(), request.getWord());

        if (result.isPresent()) {
            String word = result.get().getKey();
            int score = result.get().getValue();
            log.info("Best word found: '{}' (score={})", word, score);
            return ResponseEntity.ok(new WordResponse(word, score));
        }

        log.info("No valid word found for rack='{}' boardWord='{}'", request.getRack(), request.getWord());
        return ResponseEntity.ok(new WordResponse("No valid word found with the given tiles"));
    }

    @Operation(summary = "Health check", description = "Returns a simple status message to confirm the service is running")
    @ApiResponse(responseCode = "200", description = "Service is up")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Scrabble Word Builder",
                "dictionarySize", String.valueOf(scrabbleService.getDictionary().size())
        ));
    }
}

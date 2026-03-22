package com.hasbropulse.scrabble.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasbropulse.scrabble.exception.InvalidInputException;
import com.hasbropulse.scrabble.model.LetterInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles the core word-finding logic for the Scrabble Word Builder.
 *
 * The approach: combine rack letters with any board word into a pool,
 * check each dictionary word against that pool, score the valid ones,
 * and return the highest scorer. Ties go alphabetically.
 *
 * Dictionary and letter data are loaded once at startup to keep lookups fast.
 * No blank tiles — keeping it simple per the challenge requirements.
 */
@Service
public class ScrabbleService {

    private static final Logger log = LoggerFactory.getLogger(ScrabbleService.class);

    private static final int MIN_RACK_SIZE  = 1;
    private static final int MAX_RACK_SIZE  = 7;
    private static final int MIN_WORD_LEN   = 2;
    private static final int MAX_WORD_LEN   = 15;

    @Value("${scrabble.dictionary.path}")
    private String dictionaryPath;

    @Value("${scrabble.letter-data.path}")
    private String letterDataPath;

    // words loaded from dictionary.txt at startup
    private List<String> dictionary;

    // letter scores and tile counts from letter_data.json
    private Map<Character, LetterInfo> letterData;

    @PostConstruct
    public void init() throws IOException {
        loadDictionary();
        loadLetterData();
        log.info("Loaded {} words from dictionary and {} letter definitions.",
                dictionary.size(), letterData.size());
    }

    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(dictionaryPath);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toUpperCase();
                if (!word.isEmpty() && word.matches("[A-Z]+")) {
                    dictionary.add(word);
                }
            }
        }
    }

    private void loadLetterData() throws IOException {
        letterData = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource(letterDataPath);
        Map<String, LetterInfo> raw = mapper.readValue(
                resource.getInputStream(),
                new TypeReference<Map<String, LetterInfo>>() {});
        raw.forEach((key, info) -> letterData.put(key.toUpperCase().charAt(0), info));
    }

    /**
     * Returns the highest-scoring word that can be built from the rack
     * plus any letters from the board word.
     *
     * Returns empty if no valid word can be formed.
     */
    public Optional<Map.Entry<String, Integer>> findBestWord(String rack, String boardWord) {

        // validate rack
        if (rack == null || rack.isBlank()) {
            throw new InvalidInputException("rack is required and must not be blank");
        }
        String normRack = rack.trim().toUpperCase();
        if (!normRack.matches("[A-Z]+")) {
            throw new InvalidInputException("rack must contain only alphabetic letters");
        }
        if (normRack.length() < MIN_RACK_SIZE || normRack.length() > MAX_RACK_SIZE) {
            throw new InvalidInputException(
                    "rack must contain between 1 and 7 letters, but got " + normRack.length());
        }

        // validate board word if provided
        String normBoard = "";
        if (boardWord != null && !boardWord.isBlank()) {
            normBoard = boardWord.trim().toUpperCase();
            if (!normBoard.matches("[A-Z]+")) {
                throw new InvalidInputException("board word must contain only alphabetic letters");
            }
        }

        // pool = rack + board word letters
        String pool = normRack + normBoard;
        Map<Character, Integer> poolFreq = letterFrequency(pool);

        // make sure we're not using more tiles than actually exist in a real set
        for (Map.Entry<Character, Integer> entry : poolFreq.entrySet()) {
            char letter = entry.getKey();
            int count = entry.getValue();
            LetterInfo info = letterData.get(letter);
            if (info == null) {
                throw new InvalidInputException("Letter '" + letter + "' is not a valid Scrabble tile");
            }
            if (count > info.getTiles()) {
                throw new InvalidInputException(
                        "Invalid input: letter '" + letter + "' appears " + count +
                        " time(s) but only " + info.getTiles() + " tile(s) exist in the game");
            }
        }

        // scan dictionary for the best word
        String bestWord = null;
        int bestScore = -1;

        for (String candidate : dictionary) {
            int len = candidate.length();
            if (len < MIN_WORD_LEN || len > MAX_WORD_LEN) {
                continue;
            }
            if (!canForm(candidate, poolFreq)) {
                continue;
            }
            int score = scoreWord(candidate);
            if (score > bestScore || (score == bestScore && candidate.compareTo(bestWord) < 0)) {
                bestScore = score;
                bestWord = candidate;
            }
        }

        if (bestWord == null) {
            return Optional.empty();
        }
        return Optional.of(new AbstractMap.SimpleEntry<>(bestWord, bestScore));
    }

    // checks if a word can be formed from the available letter pool
    public boolean canForm(String word, Map<Character, Integer> availableFreq) {
        Map<Character, Integer> needed = letterFrequency(word);
        for (Map.Entry<Character, Integer> entry : needed.entrySet()) {
            if (availableFreq.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    // sums up letter values to get the word's Scrabble score
    public int scoreWord(String word) {
        int total = 0;
        for (char c : word.toCharArray()) {
            LetterInfo info = letterData.get(c);
            if (info != null) {
                total += info.getScore();
            }
        }
        return total;
    }

    // builds a map of letter -> count for a given string
    public Map<Character, Integer> letterFrequency(String s) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    public List<String> getDictionary() {
        return Collections.unmodifiableList(dictionary);
    }

    public Map<Character, LetterInfo> getLetterData() {
        return Collections.unmodifiableMap(letterData);
    }
}

package com.hasbropulse.scrabble;

import com.hasbropulse.scrabble.exception.InvalidInputException;
import com.hasbropulse.scrabble.model.LetterInfo;
import com.hasbropulse.scrabble.service.ScrabbleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// boots the full Spring context so dictionary and the letter data load exactly as in the prod 
@SpringBootTest
class ScrabbleServiceTest {

    @Autowired
    private ScrabbleService service;

    // challenge examples from the document 

    @Test
    void example1_rackAIDOORW_boardWIZ_shouldReturnWizard() {
        Optional<Map.Entry<String, Integer>> result = service.findBestWord("AIDOORW", "WIZ");

        assertTrue(result.isPresent());
        assertEquals("WIZARD", result.get().getKey());
        assertEquals(19, result.get().getValue());
    }

    @Test
    void example2_rackAIDOORW_noBoard_shouldReturnDraw() {
        Optional<Map.Entry<String, Integer>> result = service.findBestWord("AIDOORW", null);

        assertTrue(result.isPresent());
        // DRAW, WARD, WORD all score 8 — DRAW wins alphabetically (handling in alphabetical order)
        assertEquals("DRAW", result.get().getKey());
        assertEquals(8, result.get().getValue());
    }

    @Test
    void example3_rackAIDOORZ_boardQUIZ_shouldFailTileLimit() {
        // pool has 2 Z's but only 1 Z tile exists in Scrabble
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> service.findBestWord("AIDOORZ", "QUIZ")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("z") ||
                   ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void example4_rackEightLetters_shouldFailRackSize() {
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> service.findBestWord("AIDOORWZ", null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("7") ||
                   ex.getMessage().toLowerCase().contains("rack"));
    }

    //  input validation 

    @Test
    void nullRack_shouldThrow() {
        assertThrows(InvalidInputException.class, () -> service.findBestWord(null, null));
    }

    @Test
    void blankRack_shouldThrow() {
        assertThrows(InvalidInputException.class, () -> service.findBestWord("   ", null));
    }

    @Test
    void rackWithNumbers_shouldThrow() {
        assertThrows(InvalidInputException.class, () -> service.findBestWord("A1B2", null));
    }

    @Test
    void boardWordWithNumbers_shouldThrow() {
        assertThrows(InvalidInputException.class, () -> service.findBestWord("ABCDE", "W1Z"));
    }

    @Test
    void emptyBoardWord_shouldBehaveSameAsNull() {
        Optional<Map.Entry<String, Integer>> withNull  = service.findBestWord("AIDOORW", null);
        Optional<Map.Entry<String, Integer>> withEmpty = service.findBestWord("AIDOORW", "");

        assertEquals(withNull.isPresent(), withEmpty.isPresent());
        if (withNull.isPresent()) {
            assertEquals(withNull.get().getKey(), withEmpty.get().getKey());
            assertEquals(withNull.get().getValue(), withEmpty.get().getValue());
        }
    }

    @Test
    void lowercaseInput_shouldWorkSameAsUppercase() {
        Optional<Map.Entry<String, Integer>> lower = service.findBestWord("aidoorw", "wiz");
        Optional<Map.Entry<String, Integer>> upper = service.findBestWord("AIDOORW", "WIZ");

        assertEquals(upper.isPresent(), lower.isPresent());
        if (upper.isPresent()) {
            assertEquals(upper.get().getKey(), lower.get().getKey());
        }
    }

    @Test
    void singleLetterRack_shouldNotThrow() {
        assertDoesNotThrow(() -> service.findBestWord("A", null));
    }

    @Test
    void sevenLetterRack_shouldBeAccepted() {
        assertDoesNotThrow(() -> service.findBestWord("ABCDEFG", null));
    }

    @Test
    void eightLetterRack_shouldBeRejected() {
        assertThrows(InvalidInputException.class, () -> service.findBestWord("ABCDEFGH", null));
    }

    // Scoring and helpers 

    @Test
    void scoreWord_wizardShouldBe19() {
        assertEquals(19, service.scoreWord("WIZARD"));
    }

    @Test
    void scoreWord_drawShouldBe8() {
        assertEquals(8, service.scoreWord("DRAW"));
    }

    @Test
    void canForm_wizardFromFullPool() {
        Map<Character, Integer> pool = service.letterFrequency("AIDOORW" + "WIZ");
        assertTrue(service.canForm("WIZARD", pool));
    }

    @Test
    void canForm_wizardShouldFailWithoutZ() {
        Map<Character, Integer> pool = service.letterFrequency("AIDOORW");
        assertFalse(service.canForm("WIZARD", pool));
    }

    @Test
    void tieBreaking_shouldReturnAlphabeticallyFirst() {
        // DRAW and WARD both score 8 from AIDOORW, DRAW should win
        Optional<Map.Entry<String, Integer>> result = service.findBestWord("AIDOORW", null);
        assertTrue(result.isPresent());
        assertTrue(result.get().getKey().compareTo("WARD") <= 0);
    }

    @Test
    void letterData_shouldHave26Entries() {
        assertEquals(26, service.getLetterData().size());
    }

    @Test
    void dictionary_shouldHaveMoreThan100Words() {
        assertTrue(service.getDictionary().size() > 100);
    }

    @Test
    void letterZ_shouldHaveScore10AndOneTile() {
        LetterInfo z = service.getLetterData().get('Z');
        assertNotNull(z);
        assertEquals(10, z.getScore());
        assertEquals(1, z.getTiles());
    }
}

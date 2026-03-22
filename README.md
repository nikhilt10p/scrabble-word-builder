# Scrabble Word Builder

My solution to the Hasbro Pulse Engineering coding challenge. You give it a rack of up to 7 letters and optionally a word already on the board, and it finds the highest possible scoring valid Scrabble word you can form from those tiles.

I built it as a Spring Boot REST API with a browser UI. The challenge was open-ended about format so I went with REST since it fits the problem naturally, and added a UI on top so it's easy to try without touching the terminal. Swagger is also wired up if you prefer that.


# What you need to run 

Java 17
Maven 3.8+


# Project structure

scrabble-word-builder/
- pom.xml                              Maven's build file, lists all dependencies and Java version
- README.md                            Current file
- src/
  - main/
    - java/com/hasbropulse/scrabble/
      - ScrabbleWordBuilderApplication.java   Starts only the Spring Boot app
      - controller/
        - ScrabbleController.java             Takes HTTP requests, calls the service, sends back the response
      - service/
        - ScrabbleService.java                Actual logic - validates input, scans the dictionary, picks the best word
      - dto/
        - WordRequest.java                    Defines what the request body looks like (rack + optional word)
        - WordResponse.java                   Defines what the response looks like (word + score, or a message)
      - model/
        - LetterInfo.java                     Simple object that holds a letter's point value and how many tiles exist
      - exception/
        - InvalidInputException.java          Custom error thrown when the input breaks the rules
        - GlobalExceptionHandler.java         Catches all errors and turns them into clean JSON instead of ugly stack traces
    - resources/
      - application.properties               Config file, sets the port to 8080 and points to the data files
      - dictionary.txt                        List of valid English words, one per line, loaded when the app starts
      - letter_data.json                      Each letter's Scrabble score and how many tiles of it exist in the game
      - static/
        - index.html                          The browser UI - open localhost:8080 to use it
  - test/
    - java/com/hasbropulse/scrabble/
      - ScrabbleServiceTest.java              16 tests - covers all four challenge examples and edge cases
## Setup and installation

Make sure you have Java 17 and Maven 3.8+ installed. Then:

bash command 
git clone <repository-url>
cd scrabble-word-builder
mvn clean package -DskipTests

That compiles everything and packages it into a JAR. You only need to do this once.


## Running it

bash command 
mvn spring-boot:run

Wait for this in the output:

Started ScrabbleWordBuilderApplication in x.x seconds

Or if you'd rather run the JAR directly:

bash command
java -jar target/scrabble-word-builder-1.0.0.jar


Both do the same thing. The server starts on port 8080.


# Usage

Browser UI: go to `http://localhost:8080`. Enter your rack letters, optionally a board word, and click Find Best Word. There are four example buttons at the bottom that load the challenge examples directly if you want to verify those first.

Swagger UI:go to `http://localhost:8080/swagger-ui.html` to try the endpoints interactively from the browser without any curl commands.

curl : see the Examples section below.


# API

# POST /api/scrabble/best-word

json
{
  "rack": "AIDOORW",
  "word": "WIZ"
}

`rack` is required, 1 to 7 letters, A-Z only. `word` is optional.

Word found (200):
json
{ "word": "WIZARD", "score": 19 }

No word possible (200):
json
{ "message": "No valid word found with the given tiles" }

Bad input (400):
json
{ "error": "Invalid input", "message": "rack must contain between 1 and 7 letters, but got 8" }

# GET /api/scrabble/health

Quick check that the service is up and the dictionary loaded correctly.

json
{ "status": "UP", "service": "Scrabble Word Builder", "dictionarySize": "2302" }


# Examples

All four from the challenge document, tested locally on CLI 

Example 1 - rack + board word
bash command
curl -s -X POST http://localhost:8080/api/scrabble/best-word \
  -H "Content-Type: application/json" \
  -d "{\"rack\": \"AIDOORW\", \"word\": \"WIZ\"}"
json
{"word":"WIZARD","score":19}
W(4) + I(1) + Z(10) + A(1) + R(1) + D(2) = 19.

Example 2 - rack only
bash command 
curl -s -X POST http://localhost:8080/api/scrabble/best-word \
  -H "Content-Type: application/json" \
  -d "{\"rack\": \"AIDOORW\"}"
  json
{"word":"DRAW","score":8}
DRAW, WARD and WORD all score 8. DRAW comes first alphabetically so that's what gets returned.

Example 3 - tile limit exceeded
bash command 
curl -s -X POST http://localhost:8080/api/scrabble/best-word \
  -H "Content-Type: application/json" \
  -d "{\"rack\": \"AIDOORZ\", \"word\": \"QUIZ\"}"
  json
{"error":"Invalid input","message":"Invalid input: letter 'Z' appears 2 time(s) but only 1 tile(s) exist in the game"}

Only one Z tile in a real Scrabble set. One in the rack plus one in the board word puts you over the limit.

Example 4 - rack too long
bash command 
curl -s -X POST http://localhost:8080/api/scrabble/best-word \
  -H "Content-Type: application/json" \
  -d "{\"rack\": \"AIDOORWZ\"}"
json
{"error":"Invalid input","message":"rack must contain between 1 and 7 letters, but got 8"}


# Validation rules

 Rule | Response 

 Rack is required | 400 - rack is required and must not be blank 
 Rack must be 1 to 7 letters | 400 - rack must contain between 1 and 7 letters, but got X 
 Rack letters only, no numbers or symbols | 400 - rack must contain only alphabetic letters 
 Board word letters only | 400 - board word must contain only alphabetic letters 
 Letter count can't exceed available tiles | 400 - letter X appears N times but only M tiles exist 
 Result words must be 2 to 15 letters | Words outside this range are skipped silently 
 Word must be in the dictionary | Unknown words are skipped 
 Score tie | Earlier word alphabetically wins 

Intentionally left out per the spec: blank tiles, bonus squares, board positioning.


# Running the tests

Bash Command: mvn test

Result:
16 tests covering all four challenge examples plus edge cases - null rack, blank rack, numbers in input, tile limit checks, lowercase input, empty board word, single-letter rack, max rack size, score verification for WIZARD and DRAW, canForm with and without Z, tie-breaking, and confirming the dictionary and letter data loaded correctly.


# Assumptions and design decisions

Format choice : The spec left the format open so I went with a REST API. It's the most practical format for something like this, to test with curl or Swagger, and straightforward to extend later. I added a browser UI on top so it's usable without any command-line knowledge.

Dictionary : I built a custom list of around 2300 common English words rather than bundling a full Scrabble dictionary. It covers all the challenge examples and typical inputs. If you want to swap in the full TWL06 or SOWPODS word list, just replace `dictionary.txt` nothing in the code needs to change.

Letter data : Stored in `letter_data.json` using standard Scrabble values (A=1pt/9 tiles, Z=10pts/1 tile, etc.). JSON felt cleaner than CSV for this structure since each letter has two attributes.

Algorithm : The core approach is a linear scan of the dictionary with letter frequency maps. I looked at using a trie for faster prefix pruning but the dictionary is small enough that a linear pass finishes well under 10ms per request. Simpler code felt like the right call at this scale.

Tile validation : The tile limit check happens before the dictionary scan, not after. This way invalid input is rejected immediately without wasting time scanning words that could never be valid anyway.

Startup loading : Dictionary and letter data load once at startup via `@PostConstruct`. If either file is missing the app fails on boot with a clear error rather than silently breaking on the first request.

Error handling : All exceptions go through a single `GlobalExceptionHandler` that returns consistent JSON. The API never exposes a stack trace to the caller.

Case handling : Input is uppercased before any processing so lowercase, uppercase, or mixed input all behave the same.

Blank tiles : Excluded per the spec. The tile count validation only covers the 26 standard letters.


# Data files

`dictionary.txt` One word per line, loaded at startup. Around 2300 words. Replace with any word list to extend coverage without touching code.

`letter_data.json` score and tile count for all 26 letters using standard Scrabble values:

json
{
  "A": { "score": 1, "tiles": 9 },
          to 
  "Z": { "score": 10, "tiles": 1 }
}


Quick scoring reference:

| Letters | Points |
|---------|--------|
| A E I O U L N S T R | 1 |
| D G | 2 |
| B C M P | 3 |
| F H V W Y | 4 |
| K | 5 |
| J X | 8 |
| Q Z | 10 |


# What I'd add with more time

Larger dictionary- TWL06 has around 178k words, would be a straight file swap
Controller tests- MockMvc tests to cover the full HTTP layer on top of the service tests
Docker - A Dockerfile so it runs without needing Java or Maven installed
Score endpoint - Something like `GET /api/scrabble/score?word=WIZARD` for checking individual words
Board word length cap - Right now there's no limit on board word length, but in a real game the board is 15 squares so it should probably be bounded

package server;

import client.ClientMain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A filter to check if a string contains any illegal words.
 * Filters out both whole words, and words who has a substring matching a word in the profanityfilter.txt file
 * Works with normal text, all kinds of CaPiTaLiZiNg, s p a c e s, l337sp3@K
 */
public class ProfanityFilter {

    static ArrayList<String> wordsInFilter = new ArrayList<>();
    static int largestWordLength = 0;
    private int counter = 0;

    public ProfanityFilter() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClientMain.class.getResourceAsStream("/profanityfilter.txt"), "Cp1252"))) { // TODO: Change path, it should not be in "resources"
                String line = reader.readLine();
                while (line != null) {                      // loop through all lines
                    wordsInFilter.add(line);                        // add line as a word
                    counter++;                              // increase word count
                    if(line.length() > largestWordLength) {
                        largestWordLength = line.length();
                    }
                    line = reader.readLine();               // read next line
                }
                System.out.println("Profanity filter loaded " + counter + " words");
            } catch (IOException e) {
                System.out.println("Unable to load profanityfilter.txt file, no profanity filter will be used");
            }
    }


    /**
     * Iterates over a String input and checks whether a cuss word was found in a list, then checks if the word should be ignored (e.g. bass contains the word *ss).
     * @param input String that should be checked for bad words
     * @return true if any bad words were found.
     */

    public boolean checkForBadWords(String input) {
        if(input == null) {
            return false;
        }

        String alphabetInput = input.toLowerCase().replaceAll("[^a-zA-Z]", ""); // ignore non-alphabet characters

        ArrayList<String> badWords = new ArrayList<>();

        // iterate over each letter in the word
        for(int start = 0; start < alphabetInput.length(); start++) {
            // from each letter, keep going to find bad words until either the end or the max word length is reached
            for(int offset = 1; offset < (alphabetInput.length()+1 - start) && offset < largestWordLength+1; offset++)  {
                String wordToCheck = alphabetInput.substring(start, start + offset);
                System.out.println(wordToCheck);
                if(wordsInFilter.contains(wordToCheck)) {
                        badWords.add(wordToCheck);
                }
            }
        }

        String allCharsInput = input.toLowerCase();

        // Check for leet speak
        allCharsInput = allCharsInput.replaceAll("1","i");
        allCharsInput = allCharsInput.replaceAll("!","i");
        allCharsInput = allCharsInput.replaceAll("3","e");
        allCharsInput = allCharsInput.replaceAll("4","a");
        allCharsInput = allCharsInput.replaceAll("@","a");
        allCharsInput = allCharsInput.replaceAll("5","s");
        allCharsInput = allCharsInput.replaceAll("7","t");
        allCharsInput = allCharsInput.replaceAll("0","o");
        allCharsInput = allCharsInput.replaceAll("9","g");

        // check again, now with leet speak enabled in filter
        for(int start = 0; start < allCharsInput.length(); start++) {
            for(int offset = 1; offset < (allCharsInput.length()+1 - start) && offset < largestWordLength+1; offset++)  {
                String wordToCheck = allCharsInput.substring(start, start + offset);
                System.out.println(wordToCheck);
                if(wordsInFilter.contains(wordToCheck)) {
                    badWords.add(wordToCheck);
                }
            }
        }

//        // print the bad words found
//        for(String s: badWords) {
//            System.out.println(s + " qualified as a bad word in a username");
//        }

        return badWords.size() > 0;
    }
}
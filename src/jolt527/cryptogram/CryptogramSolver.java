package jolt527.cryptogram;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is the entry point for determining the solutions for a cryptogram.
 *
 * Created by Keith on 5/14/2016.
 */
public class CryptogramSolver {

    private Dictionary dictionary = new Dictionary();

    /**
     * This is a simple wrapper class that allows strings to be sorted by their lengths
     * (via the java collections sorting method).
     */
    private class StringSortableByLength implements Comparable<StringSortableByLength> {
        private String string;

        public StringSortableByLength(String string) {
            this.string = string;
        }

        @Override
        public int compareTo(StringSortableByLength other) {
            // sort strings by ascending order of their lengths
            return this.string.length() - other.string.length();
        }
    }

    /**
     * The public interface to have a cryptogram solved.
     *
     * @param cryptogram    The cryptogram to have solved.
     * @return  A list of derived solutions.
     */
    public List<String> solve(String cryptogram) {
        // extract the unique words from the cryptogram and sort them by length
        List<String> uniqueWords = extractWordsAndSortByLength(cryptogram);

        // determine the characters needed to be translated
        Set<Character> cipherChars = extractCipherCharacters(uniqueWords);

        // determine the ciphers that create dictionary-based solutions
        List<Map<Character, Character>> solutionCipherList = new LinkedList<>();
        determineSolutionCiphers(cryptogram, uniqueWords, cipherChars, new TreeMap<>(), solutionCipherList);

        // translate the cryptogram with all possible solution ciphers, and return those translations
        return decryptCryptogramWithCiphers(cryptogram, solutionCipherList);
    }

    /**
     * Extracts the encrypted words from a cryptogram string and returns them in a list, sorted
     * by their lengths (ascending).
     *
     * @param cryptogram    The input cryptogram.
     * @return  A list of the encrypted words from the cryptogram, sorted by their lengths (ascending).
     */
    private List<String> extractWordsAndSortByLength(String cryptogram) {
        // get each word from the cryptogram and sort them by length (smallest first)
        List<StringSortableByLength> wordList = new LinkedList<>();
        Scanner wordScanner = new Scanner(cryptogram);
        while (wordScanner.hasNext()) {
            wordList.add(new StringSortableByLength(wordScanner.next()));
        }
        Collections.sort(wordList);

        // convert the list into a list of plain strings and return it
        List<String> sortedWordsList = new LinkedList<>();
        for (StringSortableByLength word : wordList) {
            sortedWordsList.add(word.string);
        }
        return sortedWordsList;
    }

    /**
     * Extracts the unique encrypted characters from the cryptogram words list.
     *
     * @param wordsList    The words extracted from the cryptogram.
     * @return  A set containing the unique encrypted characters from the cryptogram words list.
     *          Being a set, this ensures uniqueness.
     */
    private Set<Character> extractCipherCharacters(List<String> wordsList) {
        Set<Character> cipherChars = new TreeSet<>();
        for (String word : wordsList) {
            for (char currentChar : word.toCharArray()) {
                cipherChars.add(currentChar);
            }
        }

        return cipherChars;
    }

    /**
     * Determines the solution ciphers that cause all words in a cryptogram, when decrypted with such ciphers,
     * to be found in the dictionary.  This is all done recursively with this method.
     *
     * TODO: This should really be refactored some!  It's a messy beast, but it works for now!  :P
     *
     * @param cryptogram            The input cryptogram.
     * @param uniqueWords           A list of the words in the cryptogram, sorted by length (ascending).
     * @param cipherChars           The set of unique characters found in the original cryptogram.
     * @param currentCipher         The mappings between encrypted and decrypted characters so far.
     * @param solutionCipherList    The list of ciphers found so far that have been proven to transform
     *                              the input cryptogram into a list of English language words.
     */
    private void determineSolutionCiphers(
            String cryptogram,
            List<String> uniqueWords,
            Set<Character> cipherChars,
            Map<Character, Character> currentCipher,
            List<Map<Character, Character>> solutionCipherList) {

        // are we at a solution now?  if so, add it to the list of solutions and quit
        if (cipherChars.size() == currentCipher.size()) {

            // get the possible solution string
            List<Map<Character, Character>> theCipher = new LinkedList<>();
            theCipher.add(currentCipher);
            String possibleSolution = decryptCryptogramWithCiphers(cryptogram, theCipher).get(0);

            // check each of the words in the possible solution against the dictionary, and
            // exit out if any word isn't found in the dictionary
            Scanner finalWords = new Scanner(possibleSolution);
            while (finalWords.hasNext()) {
                String solutionWord = finalWords.next();
                if (!dictionary.doesWordExist(solutionWord)) {
                    return;
                }
            }

            // if the solution cipher already exists in the list of solutions, there's no need to add it
            if (solutionCipherList.contains(currentCipher)) {
                return;
            }

            // add the solution and report it real-time
            solutionCipherList.add(new TreeMap<>(currentCipher));
            System.out.println(String.format("SOLUTION FOUND: %s", possibleSolution));
            return;
        }

        // are there any more words to process?  if not, we're done
        if (uniqueWords.isEmpty()) {
            return;
        }

        // copy the words left to process so recursive calls don't change them
        // (AND DON'T USE THE ORIGINAL ANYMORE!)
        List<String> uniqueWordsCopy = new LinkedList<>(uniqueWords);

        // get the next word that hasn't been completely decrypted yet
        String wordNotCompletelyTranslated = null;
        String pattern = null;
        while (!uniqueWordsCopy.isEmpty()) {
            wordNotCompletelyTranslated = uniqueWordsCopy.remove(0);
            pattern = determinePattern(wordNotCompletelyTranslated, currentCipher);
            if (pattern.contains("*")) {
                break;
            }

            // the word can be completely translated if this point is reached, but if its translation
            // isn't a real word, then the cipher must be wrong - quit this iteration and go no further!
            List<Map<Character, Character>> theCipher = new LinkedList<>();
            theCipher.add(currentCipher);
            String translatedWord = decryptCryptogramWithCiphers(wordNotCompletelyTranslated, theCipher).get(0);
            if (!dictionary.doesWordExist(translatedWord)) {
                return;
            }
        }

        // use the created pattern to find dictionary words matching the pattern to make guesses
        List<String> matchingWordsList = dictionary.findWordsByPattern(pattern);

        // try to use each matching word to add to the cipher
        for (String matchingWord : matchingWordsList) {

            // add the translated characters to a copy of the cipher
            Map<Character, Character> currentCipherCopy = new TreeMap<>(currentCipher);

            // determine the new mappings for the cipher
            boolean shouldDiscardMatchingWord = false;
            Map<Character, Character> tempAdditionalCipher = new TreeMap<>();
            for (int i = 0 ; i < matchingWord.length(); i++) {

                // ignore already determined characters
                if ('*' != pattern.charAt(i)) {
                    continue;
                }

                char encryptedChar = wordNotCompletelyTranslated.charAt(i);
                char decryptedChar = matchingWord.charAt(i);

                // a mapping from a character to itself is not useful, so the matching word should be discarded
                if (encryptedChar == decryptedChar) {
                    shouldDiscardMatchingWord = true;
                    break;
                }

                // make sure the new mapping wouldn't cause value collisions in the current cipher
                for (Map.Entry<Character, Character> cipherEntry : currentCipherCopy.entrySet()) {
                    if (decryptedChar == cipherEntry.getValue()) {
                        shouldDiscardMatchingWord = true;
                        break;
                    }
                }
                if (shouldDiscardMatchingWord) {
                    break;
                }

                tempAdditionalCipher.put(encryptedChar, decryptedChar);
            }

            // if the matching word is good to use, copy in the new cipher mappings in and recursively move on
            if (!shouldDiscardMatchingWord) {
                for (Map.Entry<Character, Character> tempEntry : tempAdditionalCipher.entrySet()) {
                    currentCipherCopy.put(tempEntry.getKey(), tempEntry.getValue());
                }

                determineSolutionCiphers(
                        cryptogram,
                        uniqueWordsCopy,
                        cipherChars,
                        currentCipherCopy,
                        solutionCipherList);
            }
        }
    }

    /**
     * Transforms a word into a pattern, replacing characters known in the cipher with their decrypted
     * versions, and unknown characters into the wildcard character ('*').
     *
     * For use in finding all possible words in the dictionary after making guesses at
     * encrypted-decrypted character pairs.
     *
     * @param word      The word to transform into a pattern.
     * @param cipher    The cipher that consists of known mappings between encrypted and decrypted characters.
     * @return  The pattern representing the known and unknown characters of the input word.
     */
    private String determinePattern(String word, Map<Character, Character> cipher) {
        final char WILDCARD_CHAR = '*';

        StringBuilder stringBuilder = new StringBuilder(word.length());

        // transform each character in the word into its decrypted version
        for (char currentChar : word.toCharArray()) {
            Character replacementChar = cipher.get(currentChar);

            // if the decrypted version isn't known yet, just replace it with a wildcard character
            stringBuilder.append(null != replacementChar ? replacementChar : WILDCARD_CHAR);
        }

        return stringBuilder.toString();
    }

    /**
     * Decrypts a cryptogram using each solution cipher from a list, and returns a list of the decrypted results.
     *
     * @param cryptogram            The input cryptogram.
     * @param solutionCipherList    The list of solution ciphers (map of encrypted characters to their respective
     *                              decrypted characters).
     * @return  A list of strings, each one being a solution derived from the input cipher solutions.
     */
    private List<String> decryptCryptogramWithCiphers(
            String cryptogram,
            List<Map<Character, Character>> solutionCipherList) {

        List<String> solutions = new LinkedList<>();

        // apply each solution cipher to the cryptogram
        for (Map<Character, Character> currentCipher : solutionCipherList) {
            StringBuilder stringBuilder = new StringBuilder(cryptogram.length());

            // transform each encrypted character to its decrypted version using the current solution cipher
            for (char currentChar : cryptogram.toCharArray()) {
                Character replacementChar = currentCipher.get(currentChar);

                // if the character isn't in the solution cipher, it's just a space character, so preserve
                // the spaces
                stringBuilder.append(null != replacementChar ? replacementChar : currentChar);
            }

            // add the translation to the output list
            solutions.add(stringBuilder.toString());
        }

        return solutions;
    }
}

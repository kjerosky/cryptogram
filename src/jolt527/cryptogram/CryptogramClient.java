package jolt527.cryptogram;

import java.util.List;
import java.util.Scanner;

/**
 * A simple client that asks the user for a cryptogram to have solved.
 *
 * Expected input should consist of alphabetical characters and spaces only.
 * Behavior on input not conforming to this is undefined at this point.
 *
 * Go to this site to get some samples to test against this program:
 * http://www.cryptograms.org/
 *
 * Created by Keith on 5/15/2016.
 */
public class CryptogramClient {

    public static void main(String[] args) {
        CryptogramSolver cryptogramSolver = new CryptogramSolver();

        // get a cryptogram from the user
        Scanner userInput = new Scanner(System.in);
        userInput.useDelimiter("\n");
        System.out.println("Please enter a cryptogram to solve (alphabetical characters and spaces only):");
        String cryptogram = userInput.next().toLowerCase();

        //todo really should do input validation  :)

        // derive solutions from the input cryptogram
        System.out.println("\nSolutions derived (listed as soon as they're found):");
        long startTime = System.currentTimeMillis();
        List<String> solutions = cryptogramSolver.solve(cryptogram);
        long endTime = System.currentTimeMillis();

        // report the solutions and timing metrics
        System.out.println("\n-----------------------------------");
        System.out.println("Compiled solution list:");
        for (String currentSolution : solutions) {
            System.out.println(currentSolution);
        }
        System.out.println(String.format("%s solutions derived in %s ms", solutions.size(), endTime - startTime));
    }

}

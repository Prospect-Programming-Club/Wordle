import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

public class Wordle
{
  private static final int MAX_GUESSES = 6;
  private static final int WORD_LENGTH = 5;
  private static final String CORRECT_IN_WORD = "🟩";
  private static final String NOT_CORRECT_IN_WORD = "🟨";
  private static final String NOT_IN_WORD = "⬛";
  private static final String UNUSED_LETTER = "⬜";
  private static final int NUM_CORRECT_IN_WORD = 2; // green
  private static final int NUM_NOT_CORRECT_IN_WORD = 1; // yellow
  private static final int NUM_NOT_IN_WORD = 0; // black
  private static final String[] SHOW_SQUARES = {NOT_IN_WORD, NOT_CORRECT_IN_WORD, CORRECT_IN_WORD, UNUSED_LETTER};
  private static final int NUM_LETTERS = 26;
  private static final char FIRST_CHAR = 'a';

  private String[] usedGuesses = new String[MAX_GUESSES];
  private int[][] showSquare = new int[MAX_GUESSES][WORD_LENGTH];
  private int[] showLetters = new int[NUM_LETTERS];

  private String wordAnswer = "";
  private ArrayList<String> validWords = new ArrayList<String>();
  private String wordGuess = "";
  private int numGuesses = 0;

  public Wordle()
  {
    // Nothing
  }
  
  public Wordle(String word)
  {
    // Debug purposes
    if (word.length() == WORD_LENGTH)
      this.wordAnswer = word.toLowerCase();
  }

  private void load()
  {
    // Show white squares if guess is not used up yet
    for (int[] row : showSquare)
      Arrays.fill(row, 3);
    // Only outputted letters will be used letters
    Arrays.fill(this.showLetters, -1);
    // Adds all X-letter words to array
    try
    {
      File file = new File("words.txt");
      Scanner scanner = new Scanner(file);

      while (scanner.hasNextLine()) 
      {
        String word = scanner.nextLine().toLowerCase();
        // Do not add words with apostrophes
        if (word.length() == WORD_LENGTH && word.indexOf("'") < 0)
        {
          this.validWords.add(word);
        }
      }
      scanner.close();
    }
    catch (FileNotFoundException ex)
    {
      ex.printStackTrace();
    }
  }

  private void intro()
  {
    String showAsTries = "tries";
    if (MAX_GUESSES == 1)
      showAsTries = "try";

    System.out.println("Guess the Wordle in " + MAX_GUESSES + " " + showAsTries + ".");
    System.out.println("Each guess must be a valid " + WORD_LENGTH + "-letter word. Hit the enter button to submit.");
    System.out.println("After each guess, the color of the tiles will change to show how close your guess was to the word.");
    System.out.println("A green box means that the letter is in the word and at the correct spot.");
    System.out.println("A yellow box means that the letter is in the word but NOT at the correct spot.");
    System.out.println("A gray/black box means that the letter is not in the word at all.");
    System.out.println();
  }

  private int findWord(ArrayList<String> words, String word)
  {
    word = word.toLowerCase();
    int left = 0;
    int right = words.size();

    // binary search for words
    while (left <= right)
    {
      int middle = (left + right) / 2;
      if (word.compareTo(words.get(middle)) < 0)
      {
        right = middle - 1;
      }
      else if (word.compareTo(words.get(middle)) > 0)
      {
        left = middle + 1;
      }
      else
      {
        return middle;
      }
    }

    return -1; 
  }

  private void displayWordle()
  {
    // Show the black, yellow, green squares with the word at the end
    for (int row = 0; row < MAX_GUESSES; row++)
    {
      // Display squares
      for (int col = 0; col < WORD_LENGTH; col++)
      {
        System.out.print(SHOW_SQUARES[this.showSquare[row][col]]);
      }

      // Display word
      if (this.usedGuesses[row] != null)
      {
        System.out.print("\t" + this.usedGuesses[row].toUpperCase().replaceAll("", " ").trim());
      }

      System.out.println();
    }

    System.out.println();

    // Show used letters and whether they're in the word or correct position or not
    String[] showText = {"Unused letters", "Letters not in word", "Letters in word but not at correct spot", "Letters at correct spot"};
    for (int i = showText.length - 1; i >= 0; i--)
    {
      System.out.print(showText[i] + ": "); 
      String showAllLetters = "";

      for (int j = 0; j < NUM_LETTERS; j++)
      {
        if (showLetters[j] == i - 1)
        {
          showAllLetters += Character.toUpperCase((char)(FIRST_CHAR + j)) + ", ";
        }
      }

      if (showAllLetters.length() > 2)
      {
        showAllLetters = showAllLetters.substring(0, showAllLetters.length() - 2);
      }
      else
      {
        showAllLetters = "none right now";
      }

      System.out.print(showAllLetters);
      System.out.println();
    }

    System.out.println();
  }

  private void updateWordle()
  {
    boolean[] lockedSquare = new boolean[WORD_LENGTH];
    int[] sequence = {NUM_CORRECT_IN_WORD, NUM_NOT_CORRECT_IN_WORD, NUM_NOT_IN_WORD};
    String tmpAnswer = this.wordAnswer;

    for (int check : sequence)
    {
      for (int i = 0; i < WORD_LENGTH; i++)
      {
        boolean condition = false;
        switch (check)
        {
          case NUM_CORRECT_IN_WORD:
            condition = this.wordGuess.charAt(i) == tmpAnswer.charAt(i);
            break;
          case NUM_NOT_CORRECT_IN_WORD:
            condition = tmpAnswer.indexOf(this.wordGuess.charAt(i)) >= 0;
            break;
          case NUM_NOT_IN_WORD:
            condition = tmpAnswer.indexOf(this.wordGuess.charAt(i)) < 0;
            break;
        }

        if (condition)
        {
          if (!lockedSquare[i])
          {
            lockedSquare[i] = true;
            this.showSquare[this.numGuesses][i] = check;
          }

          tmpAnswer = tmpAnswer.replaceFirst("" + this.wordGuess.charAt(i), " ");

          if (this.showLetters[this.wordGuess.charAt(i) - FIRST_CHAR] < check)
          {
            this.showLetters[this.wordGuess.charAt(i) - FIRST_CHAR] = check;
          }
        }
      }
    }

    this.usedGuesses[this.numGuesses] = this.wordGuess;
    this.numGuesses++;
  }

  private void displayResults()
  {
    if (this.wordGuess.equals(this.wordAnswer))
    {
      String showAsTries = "tries";
      if (this.numGuesses == 1)
        showAsTries = "try";

      System.out.println("Nice! You found the word " + this.wordAnswer.toUpperCase() + " in " + this.numGuesses + " " + showAsTries + "!");
    }
    else
    {
      System.out.println("Game over. The word was " + this.wordAnswer.toUpperCase());
    }
  }

  public void play()
  {
    this.load();
    this.intro();

    // if the answer is not initalized yet
    if (this.wordAnswer == null || this.wordAnswer.equals(""))
      this.wordAnswer = this.validWords.get((int)(Math.random() * this.validWords.size()));

    Scanner scanner = new Scanner(System.in);

    // Play game until guess limit is reached or word is found
    while (!(this.numGuesses >= MAX_GUESSES || this.wordGuess.equals(this.wordAnswer)))
    {
      // inputs user for guess and checks if guess is a valid word
      System.out.print("Enter a " + WORD_LENGTH + "-letter word: ");
      this.wordGuess = scanner.nextLine().toLowerCase();

      if (this.wordGuess.length() == WORD_LENGTH)
      {
        if (this.findWord(this.validWords, this.wordGuess) >= 0)
        {
          this.updateWordle();
          this.displayWordle();
        }
        else
        {
          System.out.println(this.wordGuess.toUpperCase() + " not in word list");
        }
      }
      else
      {
        System.out.println("Not a " + WORD_LENGTH + "-letter word");
      }
    }

    scanner.close();
    this.displayResults();
  }
}

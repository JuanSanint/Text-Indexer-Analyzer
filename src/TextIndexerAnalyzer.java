import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.JOptionPane;

public class TextIndexerAnalyzer
{
    private static CustomHashTable hashTable = new CustomHashTable(777); // Hash table with customizable size
    public static void main(String[] args) 
    {
        try {
            readFile("path of the file");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while reading the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return; // If loading fails, end the program
        }

        boolean option = true;
        while (option) {
            String input = JOptionPane.showInputDialog(null, "Select an option:\n"
                                                                            + "1. Search for a word\n"
                                                                            + "2. Show file statistics\n"
                                                                            + "3. Exit");
            if (input == null) break;

            switch (input.trim()) { // Use trim() to remove whitespace
                case "1":
                    String word = JOptionPane.showInputDialog("Enter the word you want to search:");
                    if (word != null && !word.trim().isEmpty()) {
                        searchWord(word.toLowerCase());
                    } else {
                        JOptionPane.showMessageDialog(null, "Word not found", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                    break;
                case "2":
                    displayFileStatistics();
                    break;
                case "3":
                    option = false;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid option", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    // Function to read the file and populate the table
    private static void readFile(String fileName) throws IOException 
    {
        long startTime = System.nanoTime(); // Start the timer for the function
        // UTF-8 charset to support all characters. It can even read in Russian *fire emoji* *fire emoji* *fire emoji*
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String line;
        int lineNumber = 1;

        // while -> Reads each line and readLine() iterates until all rows are finished
        while ((line = reader.readLine()) != null) 
        {  
            // Split by non-word characters turns them into words in an array "Hello, bro?" -> ["hello", "bro"]
            String[] words = line.toLowerCase().split("[^\\p{L}\\p{N}]+");
            for (String word : words) 
            {
                // If the array is not empty
                if (!word.isEmpty()) 
                { 
                    WordData data = hashTable.get(word);
                    if (data == null) 
                    {
                        data = new WordData();
                        hashTable.put(word, data);
                    }
                    data.incrementCount();
                    data.addLine(lineNumber);
                }
            }
            lineNumber++;
        }
        reader.close();
        long endTime = System.nanoTime(); // End the timer
        System.out.printf("Time taken to read the file: %.3f seconds%n", (endTime - startTime) / 1_000_000_000.0);
    }

    // Function to search for a word in the hash table
    private static void searchWord(String word) 
    {
        long startTime = System.nanoTime();  
        WordData data = hashTable.get(word);
        long endTime = System.nanoTime(); 
        System.out.printf("Time taken to search for the word: %.3f seconds%n", (endTime - startTime) / 1_000_000_000.0);
        if (data != null) 
        {
            JOptionPane.showMessageDialog(null, 
                "Word found\nOccurrences: " + data.getCount() + "\nLines: " + data.getLines(),
                "Search result", JOptionPane.INFORMATION_MESSAGE);
        } 
        else 
        {
            JOptionPane.showMessageDialog(null, "Word not found", "Search result", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Function to show file data or statistics
    private static void displayFileStatistics() 
    {
        long startTime = System.nanoTime(); 
        int totalWords = hashTable.getTotalWordCount();
        long endTime = System.nanoTime(); 
        System.out.printf("Time to show the total number of words: %.3f seconds%n", (endTime - startTime) / 1_000_000_000.0);
        JOptionPane.showMessageDialog(null, "Total words in the file: " + totalWords, "File statistics", JOptionPane.INFORMATION_MESSAGE);

        String letterInput = JOptionPane.showInputDialog("Enter a letter to count the words that start with it:");
        if (letterInput != null && !letterInput.isEmpty()) 
        {
            char letter = letterInput.toLowerCase().charAt(0); // in Java, char works like an array of letters
            startTime = System.nanoTime(); 
            long count = hashTable.countWordsStartingWith(letter);
            endTime = System.nanoTime(); 
            System.out.printf("Time to count words with '%c': %.3f seconds%n", letter, (endTime - startTime) / 1_000_000_000.0);
            JOptionPane.showMessageDialog(null, 
                "Words that start with '" + letter + "': " + count, 
                "File statistics", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

// Custom hash table implementation
class CustomHashTable 
{
    private Node[] table;
    private int size;

    // Node class for the linked lists
    private static class Node 
    {
        String key;
        WordData value;
        Node next;

        Node(String key, WordData value) 
        {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    public CustomHashTable(int size) 
    {
        this.size = size;
        this.table = new Node[size];
    }

    // Hash function
    private int hash(String key) 
    {
        return Math.abs(key.hashCode()) % size; // Get the hash value, which is the absolute value of the key's hashCode function mod the size
    }

    // Insert or update a key-value pair
    public void put(String key, WordData value) 
    {
        int index = hash(key);
        Node current = table[index];

        while (current != null) 
        {
            if (current.key.equals(key)) 
            {
                current.value = value;
                return;
            }
            current = current.next;
        }

        Node newNode = new Node(key, value);
        newNode.next = table[index];
        table[index] = newNode;
    }

    // Retrieve a value by key
    public WordData get(String key) 
    {
        int index = hash(key);
        Node current = table[index];

        while (current != null) 
        {
            if (current.key.equals(key)) 
            {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    // Count the total number of words in the hash table
    public int getTotalWordCount() 
    {
        int total = 0;
        for (Node i : table) 
        {
            Node current = i;
            while (current != null) 
            {
                total += current.value.getCount();
                current = current.next;
            }
        }
        return total;
    }

    // Count words that start with a specific letter (including repeated occurrences)
    public long countWordsStartingWith(char letter) 
    {
        long count = 0;
        for (Node node : table) 
        {
            Node current = node;
            while (current != null) 
            {
                char startLetter = current.key.charAt(0);
                if (startLetter == letter) 
                {
                    count += current.value.getCount(); // Add the count of the words
                }
                current = current.next;
            }
        }
        return count;
    }
}

// Class to store all metadata of the word
class WordData 
{
    private int count; // Number of occurrences of the word
    private int[] lines; // Array that stores the line numbers where the word appears
    private int lineCount; // Current number of stored lines

    // Constructor to initialize WordData
    public WordData() 
    {
        this.count = 0;
        this.lines = new int[10]; 
        this.lineCount = 0;
    }

    // Increment the word count
    public void incrementCount() 
    {
        this.count++;
    }

    // Add the line number where the word appears
    public void addLine(int lineNumber) 
    {
        // Check if the array size needs to be changed
        if (lineCount == lines.length) 
        {
            resizeLinesArray();
        }
        lines[lineCount] = lineNumber;
        lineCount++;
    }

    // Get the word count
    public int getCount() 
    {
        return this.count;
    }

    // Get the line numbers as a formatted string
    public String getLines() 
    {
        if (lineCount == 0) 
        {
            return "None";
        }

        StringBuilder stringArrayLines = new StringBuilder();
        for (int i = 0; i < lineCount; i++) 
        {
            stringArrayLines.append(lines[i]);
            if (i < lineCount - 1) 
            {
                stringArrayLines.append(", ");
            }
        }
        return stringArrayLines.toString();
    }

    // Resize the lines array when it is full
    private void resizeLinesArray() 
    {
        int newSize = lines.length * 2; // Double the size of the array
        int[] newLines = new int[newSize];
        for (int i = 0; i < lines.length; i++) 
        {
            newLines[i] = lines[i];
        }
        this.lines = newLines;
    }
}
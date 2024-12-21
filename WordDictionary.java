
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


public class WordDictionary {
    
    private ArrayList<String> words = new ArrayList();
    private Set<String> usedWords = new HashSet(); // we will use sets for the duplicated words that is already return to server
    Scanner scan;
    private String WORD_DICTIONARY_PATH = null;
    Random random;
    int size;
    String randomWord;
    
    
    public WordDictionary() {
        // Get the URL of the resource 
        URL resource = getClass().getResource("word_list.txt");
        // check if the path file exist
        if(resource != null){
            // convert url to a file path
            WORD_DICTIONARY_PATH = new File(resource.getFile()).getAbsolutePath();
        }
        initWordDictionary();
    }
    
    
    // initialize wordList to array
    private void initWordDictionary(){
        // check if the file path is valid
        if(WORD_DICTIONARY_PATH == null){
            System.err.println("File path is null, unable to locate the word dictionary file");
            return; // exit from the method
        }
        try{
            scan = new Scanner(new File(WORD_DICTIONARY_PATH));
            // Read each line and add it to the words list
            while(scan.hasNext()){
                words.add(scan.nextLine().trim()); // add the scanned until nextLine
            
            } // scan until newline
            
            // close the scanner
            scan.close();
        
        }catch(Exception e){
            System.err.println(e);
        }
        
    }
    
    
   
    public String serverGetRandomWord(){
        random = new Random();
        size = words.size();
        // continue to loop if the randomWord is in the sets
        do{
            int index = random.nextInt(size);
            randomWord = words.get(index);
        }while(usedWords.contains(randomWord));
        
        usedWords.add(randomWord); 
        return randomWord;
    }
    
    
    
    // for debugging purposes
    public void displayWord(){
        System.out.println(words);
    }
    
    public void displayPath(){
        System.out.println("File path: " + WORD_DICTIONARY_PATH);
    }
    
  
    
  
    
    
    
    
    
}

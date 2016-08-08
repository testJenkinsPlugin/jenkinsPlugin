package jenkins_test_java;

import java.io.IOException;

public class Jenkins_TEST_Java {
    public static void main(String[] args) {
        try{
            System.out.println("Hallo World!");
            System.in.read();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    
}

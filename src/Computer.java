import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Computer {

    final int instStart = 0;
    final int instEnd = 1023;
    final int dataStart = 1024;
    final int dataEnd = 2047;
    final int memSize = 2048;
    int dataSize;
    int instructionSize;
    int stages[];
    int memory[];
    int registers[];
    final int zeroRegister = 0;
    int pc;

    // TODO stages methods
    public Computer() {
        memory = new int[memSize];
        registers = new int[32];
        stages = new int[4];
         pc = 0;
        dataSize = 0;
        instructionSize = 0;
    }


    public void run(String fileName) {

        File program = new File(fileName);
        Scanner myReader = null;
        try {
            myReader = new Scanner(program);
        } catch (FileNotFoundException e) {
            System.out.println("File named " + fileName + " is not found");
            return;
        }
        int curMemoryAddress = 0;
        while (myReader.hasNextLine()) {
            String instruction = myReader.nextLine();
            int word = Parser.convertAssemblyToBinary(instruction);
            memory[curMemoryAddress++] = word;

        }
    }



    public static void main(String[] args) {
        Computer c = new Computer();
        c.run("program.txt");

    }


}

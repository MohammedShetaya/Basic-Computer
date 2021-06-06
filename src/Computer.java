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
            int word = convertAssemblyToBinary(instruction);
            memory[curMemoryAddress++] = word;

        }
    }


    public int convertAssemblyToBinary(String instruction) {
        String term[] = instruction.trim().split("\\s+");
        String binaryInstruction = "";
        int instType = typeOfInstruction(term[0]);
        if (instType == -1) {
            System.out.println(instruction + " is invalid instruction");
            return 0;
        }
        int opCode = 0;
        if (instType == 0) {
            switch (term[0]) {
                case "ADD":
                    opCode = 0;
                    break;
                case "SUB":
                    opCode = 1;
                    break;
                case "MUL":
                    opCode = 2;
                    break;
                case "AND":
                    opCode = 5;
                    break;
                case "LSL":
                    opCode = 8;
                    break;
                case "LSR":
                    opCode = 9;
                    break;
            }
            binaryInstruction += firstNBits(opCode, 4);
            for (int i = 1; i <= 2; i++) {
                int regIdx = Integer.parseInt(term[i].substring(1));
                binaryInstruction += firstNBits(regIdx, 5);
            }
            int r3 = 0;
            int shAmt = 0;
            if (term[0].equals("LSL") || term[0].equals("LSR")) {
                shAmt = Integer.parseInt(term[3]);
            } else {
                r3 = Integer.parseInt(term[3].substring(1));
            }
            binaryInstruction += firstNBits(r3, 5);
            binaryInstruction += firstNBits(shAmt, 13);

        } else if (instType == 1) {
            switch (term[0]) {
                case "MOVI":
                    opCode = 3;
                    break;
                case "JEQ":
                    opCode = 4;
                    break;
                case "XORI":
                    opCode = 6;
                    break;
                case "MOVR":
                    opCode = 10;
                    break;
                case "MOVM":
                    opCode = 11;
                    break;
            }
            binaryInstruction += firstNBits(opCode, 4);
            int regIdx = Integer.parseInt(term[1].substring(1));
            binaryInstruction += firstNBits(regIdx, 5);
            int r2 = 0;
            int imm = 0;
            if (term[0].equals("MOVR") || term[0].equals("MOVM")) {
                for (int i = 0; i < term[2].length(); i++) {
                    if (term[2].charAt(i) == '(') {
                        imm = Integer.parseInt(term[2].substring(0, i));
                        String s = term[2].substring(i + 1, term[2].length() - 1).trim() ;
                        r2 = Integer.parseInt(s);
                        break;
                    }
                }
            }
            else if (term[0].equals("MOVI")){
                imm = Integer.parseInt(term[1]) ;
            }
            else {
                r2 = Integer.parseInt(term[2].substring(1)) ;
                imm = Integer.parseInt(term[3]) ;
            }
            binaryInstruction += firstNBits(r2 , 5 ) ;
            binaryInstruction += firstNBits(imm , 18) ;

        } else {
            opCode = 7;
            binaryInstruction += firstNBits(opCode, 4);
            int address = Integer.parseInt(term[1]);
            binaryInstruction += firstNBits(address, 2);
        }
        return Integer.parseInt(binaryInstruction, 2);
    }


    // This method returns 0 for RType instructions, 1 for IType, 2 for JType, -1 for invalid input
    private static int typeOfInstruction(String instruction) {
        switch (instruction) {
            case "ADD":
            case "SUB":
            case "MUL":
            case "AND":
            case "LSL":
            case "LSR":
                return 0;
            case "MOVI":
            case "JEQ":
            case "XORI":
            case "MOVR":
            case "MOVM":
                return 1;
            case "JMP":
                return 2;
        }
        return -1;
    }

    public static String firstNBits(int num, int n) {
        StringBuilder sb = new StringBuilder();
        while (n-- > 0) {
            sb.append(num % 2);
            num /= 2;
        }
        return sb.reverse().toString();
    }

    public static void main(String[] args) {
        Computer c = new Computer();
        c.run("program.txt");
    }


}

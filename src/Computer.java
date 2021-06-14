import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Computer {
    private Parser parser ;
    final int instStart = 0;
    final int instEnd = 1023;
    final int dataStart = 1024;
    final int dataEnd = 2047;
    final int memSize = 2048;
    int dataSize;
    int instructionSize;
    Instruction stages[];
    int memory[];
    int registers[];
    final int zeroRegister = 0;
    int pc;

    // TODO stages methods
    public Computer() {
        parser = new Parser() ;
        memory = new int[memSize];
        registers = new int[32];
        stages = new int[4];
         pc = 0;
        dataSize = 0;
        instructionSize = 0;
    }

    public void fetch () {
        int inst = memory[pc] ;
        stages[0].fetch(inst);
    }
    public void decode(){
        stages[1].decode(registers);
        pc++ ;
    }
    public void excecute () {
        stages[2].execute(pc);

    }

    public void memoryAccess(){
        stages[3].memoryAccess(memory);
    }

    public void writeBack () {
        Instruction curInstruction = stages[4] ;
        int opcode = curInstruction.opcode;
        int aluResult = curInstruction.aluResult;
        switch (opcode){
            case 0 :
            case 1 :
            case 2 :
            case 3 :
            case 5 :
            case 6 :
            case 8 :
            case 9 :
            case 10 :
                registers[curInstruction.r1] = aluResult ;
                curInstruction.valueR1 = aluResult ;break;
            case 4 :
            case 7 :
                pc = aluResult ;break;

        }
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
            int word = parser.convertAssemblyToBinary(instruction);
            memory[curMemoryAddress++] = word;

        }
    }



    public static void main(String[] args) {
        Computer c = new Computer();
        c.run("program.txt");

    }


}

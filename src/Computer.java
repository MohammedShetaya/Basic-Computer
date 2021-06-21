import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Computer {
    private Parser parser;
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
    int pc;
    int cycles;

    public Computer() {
        parser = new Parser();
        memory = new int[memSize];
        registers = new int[32];
        stages = new Instruction[5];
        pc = 0;
        dataSize = 0;
        instructionSize = 0;
    }

    public void fetch() {
        int inst = memory[pc];
        stages[0].fetch(inst);
        pc++;
    }

    public void decode() {
        if (stages[1] == null)
            return;
        stages[1].decode(registers);
        pc++;
    }

    public void excecute() {
        if (stages[2] == null)
            return;
        Instruction i = stages[2];
        stages[2].execute(pc);
         if(i.jumpFlag){
            pc = i.aluResult;
        }

    }

    public void memoryAccess() {
        if (stages[3] == null)
            return;

        stages[3].memoryAccess(memory);
    }

    public void writeBack() {
        if (stages[4] == null)
            return;

        Instruction curInstruction = stages[4];
        // R0 cannot be overwritten
        if(curInstruction.r1 == 0)
            return;
        int opcode = curInstruction.opcode;
        int aluResult = curInstruction.aluResult;
        switch (opcode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
                registers[curInstruction.r1] = aluResult;
                curInstruction.valueR1 = aluResult;
                break;
//            case 4:
//            case 7:
//                pc = aluResult;
//                break;

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
        instructionSize = curMemoryAddress;
        cycles = 1;
        while (true) {
            if (pc >= instructionSize && allNull())
                break;
            if (cycles % 2 == 1) {

                stages[0] = new Instruction();
                fetch();
                excecute();
                writeBack();

            } else {
                decode();
                memoryAccess();
            }

            // printings
            System.out.println("cycle number: " + cycles);
            // print changes in registers and memory
            printStages();

            // prepare stages for next cycle
            checkJump();
            shiftStages(cycles);
        }

        printAllRegisters();
        printAllMemory();

    }

    private boolean checkJump() {
        // If a jump instruction has finished the memory stage,
        // drop the instructions in previous stages
        if (stages[3] == null )
            return false;

        if(stages[3].jumpFlag){
            stages[0] = null;
            stages[1]= null;
            stages[2] = null;
            return true;
        }
        return false;

    }

    public void printStages () {
        System.out.println("Cycle : " + cycles);
        if (stages[0] == null)
            System.out.println("Fetching : " + "no instruction");
        else {
            System.out.println("Fetching : " + stages[0].stringInstruction);
            System.out.println("Inputs : ");
            System.out.println("PC = " + pc);
            System.out.println("Outputs : ");
            System.out.println("BinaryInstruction = " + stages[0].binInstruction);
        }
        System.out.println("\n----------------------------\n");
        if (stages[1] == null)
            System.out.println("Decoding : " + "no instruction");
        else {
            System.out.println("Decoding : " + stages[1].stringInstruction);
            System.out.println("Inputs : ");
            System.out.println("BinaryInstruction = " + stages[1].binInstruction);
            System.out.println("Outputs : ");
            System.out.println("opcode = " + stages[1].opcode);
            System.out.println("R1 = " + stages[1].r1);
            System.out.println("R2 = " + stages[1].r2);
            System.out.println("R3 = " + stages[1].r3);
            System.out.println("shift amount = " + stages[1].shamt);
            System.out.println("immediate = " + stages[1].imm);
            System.out.println("address = " + stages[1].address);
            System.out.println("value[R1] = " + stages[1].valueR1);
            System.out.println("value[R2] = " + stages[1].valueR2);
            System.out.println("value[R3] = " + stages[1].valueR3);
        }
        System.out.println("\n----------------------------\n");
        if (stages[2] == null)
            System.out.println("Executing : " + "no instruction");
        else {
            System.out.println("Executing : " + stages[2].stringInstruction);
            switch (stages[2].opcode) {
                //add: R1 = R2 + R3
                case 0:
                    // sub: R1 = R2 - R3
                case 1:
                    // mul: R1 = R2 * R3
                case 2:
                    System.out.println("Inputs : ");
                    System.out.println("value[R2] = " + stages[2].valueR2);
                    System.out.println("value[R3] = " + stages[2].valueR3);
                    System.out.println("Outputs : ");
                    // TODO the line 202 is hashed and it is under using
                   // System.out.println("ALU Result = " + stages[1].alu);
                    break;
                // MOVI: R1 = IMM
                case 3:
                    System.out.println("Inputs : ");
                    System.out.println("value[R1] = " + stages[2].valueR1);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[1].aluResult);
                    break;
                // JEQ R1 R2 IMM
                // jump to  PC+1+IMM no registers needed
                // pc is already pointing to next instruction,add imm to it

            }
        }
    }

    private void shiftStages(int cycles) {
        // prepare stages for the next clock cycle
         if(cycles%2 == 1){
            // after the odd cycles, shift all stages to the right
            // shift in place, so start from the right side
            for(int i=4;i>0;i--)
                stages[i] = stages[i-1];
            stages[0] = null;
        }else{
            // after the even cycles, shift memory to writeBack
            // and set memory to null
            stages[4] = stages[3];
            stages[3] = null;
        }
    }

    private void printAllRegisters() {
        System.out.println("Register File content:");
        int i = 0;
        for (int x : registers) {
            System.out.println("Register: $R" + (i++) + " -> " + x);
        }
        System.out.println("Register: $pc -> " + pc);
        System.out.println("\n-----------------------------------------------------------------------\n");
    }

    private void printAllMemory() {
        System.out.println("Memory content:");
        System.out.println("Memory Instructions Part:");
        int i = 0;
        while (i <= 1023) {
            System.out.println("Memory Instruction Cell" + (++i) + " -> " + memory[i]);
        }
        System.out.println("\nMemory Data Part:");
        while (i <= 2048) {
            System.out.println("Memory Data Cell" + (++i) + " -> " + memory[i]);
        }
        System.out.println("\n-----------------------------------------------------------------------\n");

    }

    private boolean allNull() {
        for (Instruction i : stages) {
            if (i != null)
                return false;
        }
        return true;
    }


    public static void main(String[] args) {
        Computer c = new Computer();
        c.run("program.txt");

    }


}

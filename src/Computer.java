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
        if (pc >= instructionSize)
            return;
        int inst = memory[pc];
        stages[0] = new Instruction();
        stages[0].fetch(inst);
        pc++;
    }

    public void decode() {
        if (stages[1] == null)
            return;
        stages[1].decode(registers);
    }

    public void execute() {
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
                registers[curInstruction.r1] = aluResult;
                curInstruction.valueR1 = aluResult;
                break;
            case 10:
                registers[curInstruction.r1] = curInstruction.valueR1;
                break;

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
        while (pc < instructionSize || !allNull()) {
            if (cycles % 2 == 1) {

                stages[0] = null;
                fetch();
                execute();
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
            cycles++;
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
        if (stages[0] == null)
            System.out.println("Fetching : " + "no instruction");
        else {
            System.out.println("Fetching : " + stages[0].stringInstruction);
            System.out.println("Inputs : ");
            System.out.println("PC = " + (pc-1));
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
            System.out.println("Registers[ " + stages[1].r1 + " ] = " + stages[1].valueR1);
            System.out.println("Registers[ " + stages[1].r2 + " ] = " + stages[1].valueR2);
            System.out.println("Registers[ " + stages[1].r3 + " ] = " + stages[1].valueR3);
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
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("Registers[ " + stages[2].r3 + " ] = " + stages[2].valueR3);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // MOVI: R1 = IMM
                case 3:
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r1 + " ] = " + stages[2].valueR1);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // JEQ R1 R2 IMM
                // jump to  PC+1+IMM no registers needed
                // pc is already pointing to next instruction,add imm to it
                case 4 :
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r1 + " ] = " + stages[2].valueR1);
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("Outputs : ");
                    System.out.println("Jump Address = " + stages[2].aluResult);
                    break;
                // R1 = R2 & R3
                case 5 :
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("Registers[ " + stages[2].r3 + " ] = " + stages[2].valueR3);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // R1 = R2 ⊕ IMM
                case 6 :
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("Registers[ " + stages[2].r3 + " ] = " + stages[2].valueR3);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // jump to address no registers needed
                case 7 :
                    System.out.println("Inputs : ");
                    System.out.println("No inputs needed");
                    System.out.println("Outputs : ");
                    System.out.println("Jump address = " + stages[2].aluResult);
                    break;
                // logical shift left:R1 = R2<<<shamt
                case 8 :
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("shamt = " + stages[2].shamt);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // logical shift right: R1 = R2>>>shamt
                case 9:
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("shamt = " + stages[2].shamt);
                    System.out.println("Outputs : ");
                    System.out.println("ALU Result = " + stages[2].aluResult);
                    break;
                // MOVR = R1= MEM[R2+IMM]
                case 10:
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("imm = " + stages[2].imm);
                    System.out.println("Outputs : ");
                    System.out.println("Memory Address = " + stages[2].aluResult);
                    break;
                //MOVM = MEM[R2+IMM]=R1
                case 11:
                    System.out.println("Inputs : ");
                    System.out.println("Registers[ " + stages[2].r2 + " ] = " + stages[2].valueR2);
                    System.out.println("imm = " + stages[2].imm);
                    System.out.println("Outputs : ");
                    System.out.println("Memory Address = " + stages[2].aluResult);
                    break;
            }
        }
        System.out.println("\n----------------------------\n");
        if (stages[3] == null)
            System.out.println("Memory Accessing : " + "no instruction");
        else {
            System.out.println("Memory Accessing : " + stages[3].stringInstruction);
            switch (stages[3].opcode){
                // MOVR = R1= MEM[R2+IMM]
                case 10:
                    System.out.println("Inputs : ");
                    System.out.println("Memory Address = " + stages[3].aluResult);
                    System.out.println("Outputs : ");
                    System.out.println("Registers[ " + stages[3].r1 + " ] = " + stages[3].valueR1);
                    break;
                    //MOVM = MEM[R2+IMM]=R1
                case 11:
                    System.out.println("Inputs : ");
                    System.out.println("Memory Address = " + stages[3].aluResult);
                    System.out.println("Registers[ " + stages[3].r1 + " ] = " + stages[3].valueR1);
                    System.out.println("Outputs : ");
                    System.out.println("Memory [ " + stages[3].aluResult + " ] = " + memory[stages[3].aluResult]);
                    break;
                default:
                    System.out.println("No memory accessing is needed");
                    break;
            }
        }
        System.out.println("\n----------------------------\n");
        if (stages[4] == null)
            System.out.println("Writing Back : " + "no instruction");
        else {
            System.out.println("Writing Back : " + stages[4].stringInstruction);
            System.out.println("Inputs : ");
            System.out.println("No inputs needed");
            System.out.println("Outputs : ");
            System.out.println("Registers[ " + stages[4].r1 + " ] = " + stages[4].valueR1);
        }
        System.out.println("\n-----------------------------------------------------------------------\n");
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
        int i = 0 ;
        System.out.println("-------------------------") ;
         while (i < 1023) {

            System.out.println("|"+ center(i+"" ,11,' ')+"|" + center(memory[i]+"" ,11,' ')+"|");
            if (i!=1023)
            System.out.println("-------------------------") ;

            i++;
        }
        System.out.println("-------------------------") ;
        System.out.println();
        System.out.println("Memory Data Part:");
        while (i < 2048) {
            System.out.println("|"+ center(i+"" ,11,' ')+"|" + center(memory[i]+"" ,11,' ')+"|");
             if (i!=1023)
            System.out.println("-------------------------") ;

            i++;
        }

//        System.out.println("-----------------------") ;


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
        c.registers[1] = 3;
        c.registers[2] = 5;
        c.run("program.txt");

    }



}

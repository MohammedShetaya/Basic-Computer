public class Instruction {
    int binInstruction;

    int opcode ; // bits31:26
    int r1 ; // bits25:21
    int r2 ; // bit20:16
    int r3 ; // bits15:11
    int shamt ; // bits10:6
    int imm ; // bits15:0
    int address ; // bits25:0

    int valueR1 ;
    int valueR2 ;
    int valueR3 ;

    int aluResult ;

    public Instruction(){


    }
    public void fetch(int binInstruction){
        this.binInstruction = binInstruction;
    }

    public void decode(int[] registerFile){
        String inst = numberTobinaryString(binInstruction);
//		System.out.println(instruction+" "+inst);

        // Rtype
        Integer.parseInt(inst.substring(0, 4), 2);
        r1 = Integer.parseInt(inst.substring(4, 9), 2);
        r2 = Integer.parseInt(inst.substring(9, 14), 2);
        r3 = Integer.parseInt(inst.substring(14, 19), 2);
        shamt = Integer.parseInt(inst.substring(19, 32), 2);

        // Immediate
        imm = Integer.parseInt(inst.substring(14, 32), 2);

        // Jtype
        address = Integer.parseInt(inst.substring(4, 32), 2);

        // read values of registers
        valueR1 = registerFile[r1];
        valueR2 = registerFile[r2];
        valueR3 = registerFile[r3];
    }

    public void execute(int pc){
        switch (opcode){
            //add: R1 = R2 + R3
            case 0: aluResult = valueR2 + valueR3; break;
            // sub: R1 = R2 - R3
            case 1: aluResult = valueR2 - valueR3; break;
            // mul: R1 = R2 * R3
            case 2: aluResult = valueR2 * valueR3; break;
            // MOVI: R1 = IMM
            case 3: aluResult = imm; break;

            // jump to  PC+1+IMM no registers needed
            // pc is already pointing to next instruction,add imm to it
            case 4 : if( valueR1 == valueR2 ) aluResult= pc+imm; else aluResult = pc ;break ;
            // R1 = R2 & R3
            case 5 : aluResult = valueR2 & valueR3; break;
            // R1 = R2 ⊕ IMM
            case 6 : aluResult = valueR2 ^ imm ; break ;
            // jump to address no registers needed
            case 7 : aluResult = (pc & (15<<28)) + address ; break;
           // logical shift left:R1 = R2<<<shamt
            case 8 :aluResult = valueR2<<shamt; break;
            // logical shift right: R1 = R2>>>shamt
            case 9: aluResult=  valueR2>>>shamt; break;
            // MOVR = R1= MEM[R2+IMM]
            case 10: aluResult = valueR2 + imm; break;
            //MOVM = MEM[R2+IMM]=R1
            case 11: aluResult = valueR2 + imm; break;
        }
    }
    public void memoryAccess(int[] memory) {
        switch (opcode){
            // MOVR = R1= MEM[R2+IMM]
            case 10: valueR1 = memory[aluResult]; break;
            //MOVM = MEM[R2+IMM]=R1
            case 11: memory[aluResult] = valueR1; break;
        }
    }

    public static String numberTobinaryString(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(n & 1);
            n >>=1;
        }
        // 1101
        // 0001
        return sb.reverse().toString();
    }



}

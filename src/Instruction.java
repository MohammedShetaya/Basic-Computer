import java.util.Scanner;

public class Instruction {
    int binInstruction;
    String stringInstruction;

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

    boolean jumpFlag;

    public Instruction(){


    }
    public void fetch(int binInstruction){

        this.binInstruction = binInstruction;
        this.stringInstruction = convertBinaryToAssembly(binInstruction);
    }

    public void decode(int[] registerFile){
        String inst = numberTobinaryString(binInstruction);
//		System.out.println(instruction+" "+inst);

        // Rtype
        opcode = Integer.parseInt(inst.substring(0, 4), 2);
        r1 = Integer.parseInt(inst.substring(4, 9), 2);
        r2 = Integer.parseInt(inst.substring(9, 14), 2);
        r3 = Integer.parseInt(inst.substring(14, 19), 2);
        shamt = Integer.parseInt(inst.substring(19, 32), 2);

        // Immediate
        // The immediate field is a "signed" value EXCEPT in MOVR and MOVM
        // the parseInt expects the string to have a sign '-'  before the number
        imm = parseSignedInteger(inst.substring(14, 32));

        // Jtype
        address = Integer.parseInt(inst.substring(4, 32), 2);

        // read values of registers
        valueR1 = registerFile[r1];
        valueR2 = registerFile[r2];
        valueR3 = registerFile[r3];

        // string instruction
        stringInstruction = convertBinaryToAssembly(binInstruction);
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
            // JEQ R1 R2 IMM
            // jump to  PC+1+IMM no registers needed
            // pc is already pointing to next instruction,add imm to it
            case 4 : if( valueR1 == valueR2 ){
                aluResult= pc+imm;
                jumpFlag = true;
            }
            else aluResult = pc ;break ;
            // R1 = R2 & R3
            case 5 : aluResult = valueR2 & valueR3; break;
            // R1 = R2 ⊕ IMM
            case 6 : aluResult = valueR2 ^ imm ; break ;
            // jump to address no registers needed
            case 7 : aluResult = (pc & (15<<28)) + address ; jumpFlag = true; break;
           // logical shift left:R1 = R2<<<shamt
            case 8 :aluResult = valueR2<<shamt; break;
            // logical shift right: R1 = R2>>>shamt
            case 9: aluResult=  valueR2>>>shamt; break;
            // MOVR = R1= MEM[R2+IMM]
            case 10: aluResult = valueR2 + Math.abs(imm); break;
            //MOVM = MEM[R2+IMM]=R1
            case 11: aluResult = valueR2 + Math.abs(imm); break;
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

    public String convertBinaryToAssembly(int instruction) {
        String inst = numberTobinaryString(instruction);
        // Rtype
        int opcode = Integer.parseInt(inst.substring(0, 4), 2);
        int r1 = Integer.parseInt(inst.substring(4, 9), 2);
        int r2 = Integer.parseInt(inst.substring(9, 14), 2);
        int r3 = Integer.parseInt(inst.substring(14, 19), 2);
        int shamt = Integer.parseInt(inst.substring(19, 32), 2);

        // Immediate
        int imm = Integer.parseInt(inst.substring(14, 32), 2);

        // Jtype
        int address = Integer.parseInt(inst.substring(4, 32), 2);
        switch (opcode){
            //add: R1 = R2 + R3
            case 0: return ("ADD " + "R" + r1 + " R" + r2 + " R" + r3);
            // sub: R1 = R2 - R3
            case 1: return ("SUB " + "R" + r1 + " R" + r2 + " R" + r3);
            // mul: R1 = R2 * R3
            case 2: return ("MUL " + "R" + r1 + " R" + r2 + " R" + r3);
            // MOVI: R1 = IMM
            case 3: return ("MOVI " + "R" + r1 + " " + imm);
            // jump to  PC+1+IMM no registers needed
            // pc is already pointing to next instruction,add imm to it
            case 4: return ("JEQ " + "R" + r1 + " R" + r2 + " " + imm);
            // R1 = R2 & R3
            case 5: return ("AND " + "R" + r1 + " R" + r2 + " R" + r3);
            // R1 = R2 ⊕ IMM
            case 6: return ("XORI " + "R" + r1 + " R" + r2 + " " + imm);
            // jump to address no registers needed
            case 7: return ("JMP " + address);
            // logical shift left:R1 = R2<<<shamt
            case 8: return ("LSL " + "R" + r1 + " R" + r2 + " " + shamt);
            // logical shift right: R1 = R2>>>shamt
            case 9: return ("LSR " + "R" + r1 + " R" + r2 + " " + shamt);
            // MOVR = R1= MEM[R2+IMM]
            case 10: return("MOVR" + "R" + r1 + " R" + r2 + " " + imm);
            //MOVM = MEM[R2+IMM]=R1
            case 11: return("MOVM " + "R" + r1 + " R" + r2 + " " + imm);
            default: return "";
        }
    }

    // this method returns the 32 bit binary string of the integer n
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

    public static int parseSignedInteger(String binString){
        // if it's positive, just parse
        if(binString.charAt(0)=='0')
            return Integer.parseInt(binString, 2);

        // it's a negative number, flip bits and add one
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<binString.length();i++)
            sb.append(binString.charAt(i)=='0'?'1':'0');
        int val = Integer.parseInt(sb.toString(), 2) + 1;
        return -val;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int x =sc.nextInt();
        String s = numberTobinaryString(x);
//        s = s.substring(1);
        int parsed = parseSignedInteger(s);
        System.out.println(s);
        System.out.println(parsed);
    }

}

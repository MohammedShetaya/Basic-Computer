import java.util.Locale;
import java.util.Scanner;

public class Parser {


    public int convertAssemblyToBinary(String instruction) {
        instruction = instruction.toUpperCase();
        String term[] = instruction.trim().split("\\s+");
        String binaryInstruction = "";
        int instType = typeOfInstruction(term[0]);
        if (instType == -1) {
            System.out.println(instruction + " is invalid instruction");
            return 0;
        }
        int opCode = 0;
        if (instType == 0) {
            // R-type instruction
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
            // R-format
            // opcode(4) + R1(5) + R2(5) + R3(5) + SHAMT(13)

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
            // I-type instruction
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
            // I-format
            // opcode(4) + R1(5) + R2(5) + IMM(18)

            binaryInstruction += firstNBits(opCode, 4);
            int r1 = Integer.parseInt(term[1].substring(1));
            binaryInstruction += firstNBits(r1, 5);
            int r2 = 0;
            int imm = 0;
            // MOVR and MOVM format are changed to MOVR R1 R2 IMM

            /*
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
            */

            if (term[0].equals("MOVI")){
                imm = Integer.parseInt(term[2]) ;
            }
            else {
                r2 = Integer.parseInt(term[2].substring(1)) ;
                imm = Integer.parseInt(term[3]) ;
            }

            binaryInstruction += firstNBits(r2 , 5 ) ;
            binaryInstruction += firstNBits(imm , 18) ;

        } else {
            // J-Type instruction
            // opcode(4) + address(28)
            opCode = 7;
            binaryInstruction += firstNBits(opCode, 4);
            int address = Integer.parseInt(term[1]);
            binaryInstruction += firstNBits(address, 28);
        }

        return Instruction.parseSignedInteger(binaryInstruction);
    }


    // This method returns 0 for RType instructions, 1 for IType, 2 for JType, -1 for invalid input
    private  int typeOfInstruction(String instruction) {
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


    // returns the right-most n-bits from the number num
    private String firstNBits(int num, int n) {
        StringBuilder sb = new StringBuilder();
        while (n-- > 0) {
            sb.append(num & 1);
            num >>= 1;
        }
        return sb.reverse().toString();
    }


}

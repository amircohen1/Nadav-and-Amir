// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)


//instantiate R2 to 0
@R2
M=0
//check if one of the given vals is 0
    @R0
    D=M
    @END
    D;JEQ
    @R1
    D=M
    @END
    D;JEQ
(LOOP)
    //put the value of R[0] in D
    @R0
    D=M

    //increase the val of R[2] by R[0]
    @R2
    M=M+D

    //Decrease the "counter"
    @R1
    M=M-1
    D=M

    //check the loop condition
    @LOOP
    D;JNE
(END)
    @END
    0;JMP //infinite loop



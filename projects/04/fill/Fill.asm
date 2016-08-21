
// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.


//blacken the screen



@curpixel
M=0
D=M

(LOOP)
    @KBD
    D=M
    @BLACKSCREEN
    D;JNE
    @WHITESCREEN
    D;JEQ

(BLACKSCREEN)
    @curpixel
    D=M // save the cur position in screen

    @SCREEN
    A=A+D
    M=-1 // blacken the pixel

    @curpixel
    D=M
    @8191
    D=D-A

    // check if the pointer reached the end of the screen
    @LOOP
    D;JEQ
    //else
    @curpixel
    M=M+1
    @LOOP
    0;JMP

(WHITESCREEN)
    //check if the cur pixel is the first pixel
    @curpixel
    D=M

    //whiten the pixel
    @SCREEN
    A=A+D
    M=0

    @LOOP //check if it's the first pix
    D;JEQ

    // else - decrement the pix by 1 and go back to loop
    @curpixel
    M= M-1

    @LOOP
    0;JMP







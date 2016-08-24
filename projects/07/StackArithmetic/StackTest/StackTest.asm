@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@EQ_LABEL_0
D; JEQ
D = 0
@END_COND_LABEL_0
0; JEQ
(EQ_LABEL_0)
D = -1
(END_COND_LABEL_0)
@SP
M = M - 1
A = M - 1
M = D
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@EQ_LABEL_1
D; JEQ
D = 0
@END_COND_LABEL_1
0; JEQ
(EQ_LABEL_1)
D = -1
(END_COND_LABEL_1)
@SP
M = M - 1
A = M - 1
M = D
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@EQ_LABEL_2
D; JEQ
D = 0
@END_COND_LABEL_2
0; JEQ
(EQ_LABEL_2)
D = -1
(END_COND_LABEL_2)
@SP
M = M - 1
A = M - 1
M = D
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@LT_LABEL_3
D; JLT
D = 0
@END_COND_LABEL_3
0; JEQ
(LT_LABEL_3)
D = -1
(END_COND_LABEL_3)
@SP
M = M - 1
A = M - 1
M = D
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@LT_LABEL_4
D; JLT
D = 0
@END_COND_LABEL_4
0; JEQ
(LT_LABEL_4)
D = -1
(END_COND_LABEL_4)
@SP
M = M - 1
A = M - 1
M = D
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@LT_LABEL_5
D; JLT
D = 0
@END_COND_LABEL_5
0; JEQ
(LT_LABEL_5)
D = -1
(END_COND_LABEL_5)
@SP
M = M - 1
A = M - 1
M = D
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@GT_LABEL_6
D; JGT
D = 0
@END_COND_LABEL_6
0; JEQ
(GT_LABEL_6)
D = -1
(END_COND_LABEL_6)
@SP
M = M - 1
A = M - 1
M = D
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@GT_LABEL_7
D; JGT
D = 0
@END_COND_LABEL_7
0; JEQ
(GT_LABEL_7)
D = -1
(END_COND_LABEL_7)
@SP
M = M - 1
A = M - 1
M = D
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A = M - 1
D = M
A = A - 1
D = M - D
@GT_LABEL_8
D; JGT
D = 0
@END_COND_LABEL_8
0; JEQ
(GT_LABEL_8)
D = -1
(END_COND_LABEL_8)
@SP
M = M - 1
A = M - 1
M = D
@57
D=A
@SP
A=M
M=D
@SP
M=M+1
@31
D=A
@SP
A=M
M=D
@SP
M=M+1
@53
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
A=A-1
D=M
A=A-1
M=M+D
@SP
M=M-1
@112
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
A=A-1
D=M
A=A-1
M=M-D
@SP
M=M-1
@SP
A=M-1
M=-M
@SP
A=M
A=A-1
D=M
A=A-1
M=M&D
@SP
M=M-1
@82
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
A=A-1
D=M
A=A-1
M=M|D
@SP
M=M-1
@SP
A=M-1
M=!M

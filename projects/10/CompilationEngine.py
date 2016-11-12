import re

#-------------------------------constants---------------------------------

CLASS_VARDEC_REGEX = "<keyword>\s*(field|static)\s*<\/keyword>"
SUBROUTINE_DEC_REGEX = "<keyword>\s*(function|method|constructor)\s*<\/keyword>"
RETURN_REGEX = "<keyword>\s*return\s*<\/keyword>"
IF_REGEX = "<keyword>\s*if\s*<\/keyword>"
ELSE_REGEX = "<keyword>\s*else\s*<\/keyword>"
WHILE_REGEX = "<keyword>\s*while\s*<\/keyword>"
DO_REGEX = "<keyword>\s*do\s*<\/keyword>"
LET_REGEX = "<keyword>\s*let\s*<\/keyword>"
OPERATORS_REGEX = "<symbol>\s*(\+|\-|\*|\/|&amp;|&lt;|&gt;|~|\||=)\s*<\/symbol>"
SEMICOLON = "<symbol>\s*;\s*<\/symbol>"
OPEN_BRACKET = "<symbol>\s*\(\s*<\/symbol>"
CLOSING_BRACKET = "<symbol>\s*\)\s*<\/symbol>"
OPEN_SQUARE = "<symbol>\s*\[\s*<\/symbol>"
CLOSING_SQUARE = "<symbol>\s*\]\s*<\/symbol>"
CLOSING_CURL = "<symbol>\s*\}\s*<\/symbol>"
OPEN_CURL = "<symbol>\s*\{\s*<\/symbol>"
COMMA = "<symbol>\s*,\s*<\/symbol>"
DOT = "<symbol>\s*\.\s*<\/symbol>"
IDENTIFIER_REGEX = "<identifier>.*<\/identifier>"
INDENTATION_JUMP = 2  # constant number of white spaces in each indentation

#-----------------------------------------------------------------------


class CompilationEngine():
    #keep the number of indentations in the output file
    _indentations = 0

    def __init__(self, inputDir):
        self._inputDir = inputDir

        #create an output file
        # TODO remove "temp' below before submitting
        outPath = inputDir[:inputDir.rfind(".", ) - 2] + "temp.xml" # get's the MainTT.xml (tokenizer's output) and creates Main.xml# TODO to change this to "T.xml
        self._outputFile = open(outPath, 'w') # TODO maybe to open the file with 'a'???

        # open the input file and go iver it's lines
        with open(inputDir, 'r') as self._tokenFile:  # open the file
            self._compileClass()
        self._outputFile.close()


    # compiles a complete class
    def _compileClass(self):
        self._outputFile.write("<class>" + "\n")
        self._indentations += INDENTATION_JUMP

        # skip the first line because it's "<tokens>"
        self._tokenFile.readline()
        # the first 3 lines of the token file stay the same in the output file
        for i in range(3):  # print "class" "classname" "{"
            line = self._tokenFile.readline()
            self._outputFile.write((self._indentations * " ") + line)

        # go over variable declarations
        line = self._tokenFile.readline()
        while re.match(CLASS_VARDEC_REGEX, line):
            self._compileClassVarDec(line)
            line = self._tokenFile.readline()

        # go over the subroutines
        while re.match(SUBROUTINE_DEC_REGEX, line):
            self._compileSubroutine(line)
            line = self._tokenFile.readline()

        self._outputFile.write((self._indentations * " ") + line)
        self._outputFile.write("</class>" + "\n")


    # compiles a static declaration or a field declaration
    def _compileClassVarDec(self, line):
        self._outputFile.write((self._indentations * " ") + "<classVarDec>"
                               + "\n")
        self._indentations += INDENTATION_JUMP

        # write the variables as they are in the token file with an indentation
        while line.find(";") == -1:
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()

        # write the ";" line
        self._outputFile.write((self._indentations * " ") + line)
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</classVarDec>" + "\n")
        return line


    # compiles a complete method, function or constructor
    def _compileSubroutine(self, line):
        self._outputFile.write((self._indentations * " ") +
                               "<subroutineDec>" + "\n")
        self._indentations += INDENTATION_JUMP

        # write the token file lines until we get to the parameter list (arguments)
        while not re.match("<symbol>\s*\(\s*<\/symbol>", line):
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()

        # write the "(" line
        self._outputFile.write((self._indentations * " ") + line)

        # compiles parameter list (function's arguments) not including "()"
        self._outputFile.write((self._indentations * " ") + "<parameterList>" + "\n")
        self._indentations += INDENTATION_JUMP
        line = self._tokenFile.readline()
        while not re.match("<symbol>\s*\)\s*<\/symbol>", line):
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</parameterList>" + "\n")

        # write the ")" line
        self._outputFile.write((self._indentations * " ") + line)

        line = self._tokenFile.readline()
        line = self._compileSubroutineBody(line)

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</subroutineDec>" + "\n")
        return line


    # compiles the variables declaration and the statements of a subroutine
    def _compileSubroutineBody(self, line):
        self._outputFile.write((self._indentations * " ") + "<subroutineBody>" + "\n")
        self._indentations += INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + line) # write the "{" line

        # compile variable declarations
        line = self._tokenFile.readline()
        while "var" in line:
            self._compileSubroutineVarDec(line)
            line = self._tokenFile.readline()
        line = self._compileStatements(line)

        # write the "}" line
        self._outputFile.write((self._indentations * " ") + line)
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</subroutineBody>" + "\n")
        return line


    # compiles a subroutine not including "{}"
    def _compileSubroutineVarDec(self, line):
        self._outputFile.write((self._indentations * " ") + "<varDec>" + "\n")
        self._indentations += INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + line)  # write "var" line
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line) # write type (int/string..)
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)  # write first identifier

        # check if there are a few var declarations (identifiers) -  f.e var int i,j...
        while re.match(COMMA, self._peekNextLine()):
            line = self._tokenFile.readline()
            self._outputFile.write((self._indentations * " ") + line)  # write first identifier
            line = self._tokenFile.readline()
            self._outputFile.write((self._indentations * " ") + line)  # write next identifier

        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)  # write ";" line
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</varDec>" + "\n")
        return line


        # compiles statements (not including "{}")
    def _compileStatements(self, line):
        endOfStatments = False
        self._outputFile.write((self._indentations * " ") + "<statements>" + "\n")
        self._indentations += INDENTATION_JUMP

        while not endOfStatments:
            ReturnMatcher = re.match(RETURN_REGEX, line)
            IfMatcher = re.match(IF_REGEX, line)
            WhileMatcher = re.match(WHILE_REGEX, line)
            DoMatcher = re.match(DO_REGEX, line)
            LetMatcher = re.match(LET_REGEX, line)

            if (ReturnMatcher):
                line = self._compileReturn(line)
            elif (IfMatcher):
                line = self._compileIf(line)
            elif (WhileMatcher):
                line = self._compileWhile(line)
            elif (DoMatcher):
                line = self._compileDo(line)
            elif (LetMatcher):
                line = self._compileLet(line)
            else:
                endOfStatments = True
                break
            line = self._tokenFile.readline()

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</statements>" + "\n")
        return line

    # compiles a return statement
    def _compileReturn(self, line):
        self._outputFile.write((self._indentations * " ") + "<returnStatement>" + "\n")
        self._indentations += INDENTATION_JUMP
        # write the "return" line
        self._outputFile.write((self._indentations * " ") + line)

        # if the line isn't a semicolon, it means there is an expression in the return statement
        line = self._tokenFile.readline()
        if not re.match(SEMICOLON, line):
            self._compileExpression(line)
            line = self._tokenFile.readline()

        # write ";" to output file
        self._outputFile.write((self._indentations * " ") + line)
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</returnStatement>" + "\n")
        return line

    def _compileExpression(self, line):
        self._outputFile.write((self._indentations * " ") + "<expression>" + "\n")
        self._indentations += INDENTATION_JUMP

        # compile the terms in the expression
        line = self._compileTerm(line)
        while re.match(OPERATORS_REGEX, self._peekNextLine()):
            line = self._tokenFile.readline()
            # write the operator:
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            line = self._compileTerm(line)

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</expression>" + "\n")
        return line


    # compiles a term
    def _compileTerm(self, line):
        self._outputFile.write((self._indentations * " ") + "<term>" + "\n")
        self._indentations += INDENTATION_JUMP
        # if the expression is in brackets
        if re.match(OPEN_BRACKET, line):
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            line = self._compileExpression(line)
            line = self._tokenFile.readline()
            # write the opening bracket "("
            self._outputFile.write((self._indentations * " ") + line)
        # if it's an unary Operation + term:
        elif re.match(OPERATORS_REGEX, line):
            # write the closing bracket ")"
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            line = self._compileTerm(line)

        elif re.match(IDENTIFIER_REGEX, line):
            self._outputFile.write((self._indentations * " ") + line)
            #  check if it's an array
            if re.match(OPEN_SQUARE, self._peekNextLine()):
                line = self._tokenFile.readline()
                #   write opening squared bracket "["
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                self._compileExpression(line)
                line = self._tokenFile.readline()
                #  write closing bracket "]"
                self._outputFile.write((self._indentations * " ") + line)

            #  check if it's a subroutine call
            elif re.match(OPEN_BRACKET, self._peekNextLine()):
                line = self._tokenFile.readline()
                 # write opening bracket "("
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                line = self._compileExpressionList(line)

                if re.match(CLOSING_BRACKET, self._peekNextLine()):
                    line = self._tokenFile.readline()
                #     write closing bracket "("
                self._outputFile.write((self._indentations * " ") + line)


            # check if it is a call for an object's subroutine
            elif re.match(DOT, self._peekNextLine()):
                line = self._tokenFile.readline()
                # write the dot "."
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                # write the name of the subroutine
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                #   opening bracket "("
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                line = self._compileExpressionList(line)

                if re.match(CLOSING_BRACKET, self._peekNextLine()):
                    line = self._tokenFile.readline()
                #     closing bracket "("
                self._outputFile.write((self._indentations * " ") + line)

        # in this case it is only an identifier
        else:
            self._outputFile.write((self._indentations * " ") + line)

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</term>" + "\n")
        return line


    # compiles a list of expressions, always returns ")" line
    def _compileExpressionList(self, line):
        self._outputFile.write((self._indentations * " ") + "<expressionList>" + "\n")
        self._indentations += INDENTATION_JUMP

        # check if there's an expression
        if not re.match(CLOSING_BRACKET, line):
            line = self._compileExpression(line)

            # while there is still an expression:
            while re.match(COMMA, self._peekNextLine()):
                line = self._tokenFile.readline()
                self._outputFile.write((self._indentations * " ") + line)
                line = self._tokenFile.readline()
                line = self._compileExpression(line)

        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</expressionList>" + "\n")
        return line


    # compiles an if statement, possibly with a trailing else
    def _compileIf(self, line):
        self._outputFile.write((self._indentations * " ") + "<ifStatement>" + "\n")
        self._indentations += INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        self._compileExpression(line)
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        line = self._compileStatements(line)
        self._outputFile.write((self._indentations * " ") + line)  # write }

        # check if there's an else statement:
        while re.match(ELSE_REGEX, self._peekNextLine()):
            line = self._tokenFile.readline()
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            line = self._compileStatements(line)
            self._outputFile.write((self._indentations * " ") + line)


        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</ifStatement>" + "\n")
        return line


    # compiles a Do statement
    def _compileDo(self, line):
        self._outputFile.write((self._indentations * " ") + "<doStatement>" + "\n")
        self._indentations += INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + line) # write "do"
        # write everything until the open bracket
        line = self._tokenFile.readline()

        while not re.match(OPEN_BRACKET, line):
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()

        # write "("
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        line = self._compileExpressionList(line)

        if not re.match(CLOSING_BRACKET, line): #TODO change this shit everywhere in the code (comes after compileExpressionList)
            line = self._tokenFile.readline()

        self._outputFile.write((self._indentations * " ") + line)  # write ")"
        # write ";"
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</doStatement>" + "\n")
        return line


    # compiles a while statement
    def _compileWhile(self, line):
        self._outputFile.write((self._indentations * " ") + "<whileStatement>" + "\n")
        self._indentations += INDENTATION_JUMP
        # write the "while" line
        self._outputFile.write((self._indentations * " ") + line)
        # write opening bracket "("
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        self._compileExpression(line)
        # write closing bracket ")"
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        # write the opening curly bracket
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)

        if not re.match(OPEN_CURL, self._peekNextLine()):
            line = self._tokenFile.readline()

        # compile the statements in the while loop
        line = self._compileStatements(line)
        self._outputFile.write((self._indentations * " ") + line)
        # write the closing curly bracket
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</whileStatement>" + "\n")
        return line

    # compiles a Let statement
    def _compileLet(self, line):
        self._outputFile.write((self._indentations * " ") + "<letStatement>" + "\n")
        self._indentations += INDENTATION_JUMP
        # write "let" line
        self._outputFile.write((self._indentations * " ") + line)
        # write variable name line
        line = self._tokenFile.readline()
        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()

        if re.match(OPEN_SQUARE, line): # check if it's an array
            # write opening squared bracket "["
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()
            line = self._compileExpression(line)
            line = self._tokenFile.readline()
            # write closing squared bracket "]"
            self._outputFile.write((self._indentations * " ") + line)
            line = self._tokenFile.readline()

        self._outputFile.write((self._indentations * " ") + line)
        line = self._tokenFile.readline()
        self._compileExpression(line)
        line = self._tokenFile.readline()
        # write the semicolon ";"
        self._outputFile.write((self._indentations * " ") + line)
        self._indentations -= INDENTATION_JUMP
        self._outputFile.write((self._indentations * " ") + "</letStatement>" + "\n")
        return line

    # peek to the next line without continuing, and returns it
    def _peekNextLine(self):
        # save current position in the file
        pos = self._tokenFile.tell()
        # read the next line
        line = self._tokenFile.readline()
        # return to the former position in the file
        self._tokenFile.seek(pos)
        return line


# tokenizer = JackTokenizer("C:/Users/nraba/OneDrive/year3/NAND/nand2tetris/nand2tetris/projects/10/ExpressionlessSquare/Game.jack")







import re

KEYWORD = "class|constructor|function|method|field|static|var|int|char|" \
          "boolean|void|true|false|null|this|let|do|if|else|while|return"

STRING_CONST_REGEX = "\"[^\"\n]*\""

STRING_WITH_COMMENT_REGEX = "\"([^\"\n]*[\/\/|\/\*\*|\*/][^\"\n]*)\""

IDENTIFIER_REGEX = "[a-zA-Z_][a-zA-Z_0-9]*"

SYMBOL_REGEX = "{|}|\(|\)|\[|\]|\.|,|;|\+|\-|\*|\/|&|\||<|>|=|~"

UNIQUE_SYMBOLS = {"<": "&lt;", ">": "&gt;", "&": "&amp;", "\"": "&quot;"}

INT_CONST_REGEX = "\d+"

TOKEN_TYPES_REGEX = "(" + KEYWORD + ")|(" + SYMBOL_REGEX + ")|(" + INT_CONST_REGEX + ")|(" + STRING_CONST_REGEX + ")|(" + IDENTIFIER_REGEX + ")"

TYPES = ["keyword", "symbol", "integerConstant", "stringConstant", "identifier"]


#An object of this class recieves a jack file and outputs a token file
class JackTokenizer():
    def __init__(self, inPath):
        self._inPath = inPath

        # create an output file
        self.outPath = inPath[:inPath.rfind(".", )] + "TT.xml" # TODO to change this to "T.xml before submitting
        self._outFile = open(self.outPath, 'w') # TODO maybe to open the file with 'a'???
        self._outFile.write("<tokens>\n")

        # create array of the input file lines, cleaned up from comments
        self._reader = open(inPath, 'r')
        self._lines = [line.strip() for line in self._reader]  # remove whitespaces in the begining and end of line

        self._removeComments() # removes the line from comments
        # print("lines:")
        # print(self._lines)

        # tokanize lines
        for line in self._lines:
            # print(line)
            self._tokenizeLine(line)

        self._outFile.write("</tokens>")
        self._outFile.close()


    # this funciton get's a line from the input file, and departs it into tokens
    def _tokenizeLine(self, line):
        tokens = re.finditer(TOKEN_TYPES_REGEX, line)
        for token in tokens:
            for i in range(1,6):
                if token.group(i) is not None:
                    outputLine = "<" + TYPES[i - 1] + "> "
                    secondPartOfOutputLine = " </" + TYPES[i - 1] + ">\n"

                    # check if the token is in the unique tokens and act accordingly
                    if token.group(i) in UNIQUE_SYMBOLS:
                        outputLine += UNIQUE_SYMBOLS[token.group(i)] + secondPartOfOutputLine
                    else:
                        outputLine += token.group(i) + secondPartOfOutputLine

                    # check if the type is a constant string and remove the bracket's if it is
                    if i == 4:
                        outputLine = outputLine.replace("\"", "")

                    self._outFile.write(outputLine)
                    break


    # #TODO we still need to deal with comments like '/*....' , '/* \n.... \n...*/'....
    # # this function get's a line and returns it after erasing comments
    # def _removeComments(self, givenLine):
    #     if givenLine.startswith("//"):
    #         return ""
    #
    #     # get indexes of '//' and put them in an array
    #     commentIndexes = []
    #     for match in re.finditer("//", givenLine):
    #         commentIndexes.append(match.start())
    #
    #     # check if comment is in quotes (for every quote), if it is - delete it from the commentIndexes
    #     for idx in commentIndexes:
    #         isQuote = False
    #         # find the cases that // is between quotes (in a string)
    #         for match in re.finditer(STRING_WITH_COMMENT_REGEX, givenLine):
    #             if match.start() < idx and match.end() > idx:
    #                 isQuote = True
    #                 break
    #
    #         if not isQuote:
    #             givenLine = givenLine[:idx]
    #
    #     if (givenLine.startswith("*")) and not ("*/" in givenLine):
    #         return ""
    #     commentIndexes = []
    #     for match in re.finditer("\*\/", givenLine):
    #         commentIndexes.append(match.start())
    #
    #     # check if comment is in quotes (for every quote), if it is - delete it from the commentIndexes
    #     for idx in commentIndexes:
    #         isQuote = False
    #         # find the cases that // is between quotes (in a string)
    #         for match in re.finditer(STRING_WITH_COMMENT_REGEX, givenLine):
    #             if match.start() < idx and match.end() > idx:
    #                 isQuote = True
    #                 break
    #
    #         if not isQuote:
    #             i = givenLine.find("/**")
    #             if not i == -1:
    #                 givenLine = givenLine[:i] + givenLine[idx + 2:]
    #             else:
    #                 givenLine = givenLine[idx + 2:]
    #
    #     # get indexes of '/**' and put them in an array
    #     commentIndexes = []
    #     for match in re.finditer("\/\*\*", givenLine):
    #         commentIndexes.append(match.start())
    #
    #     # check if comment is in quotes (for every quote), if it is - delete it from the commentIndexes
    #     for idx in commentIndexes:
    #         isQuote = False
    #         # find the cases that // is between quotes (in a string)
    #         for match in re.finditer(STRING_WITH_COMMENT_REGEX, givenLine):
    #             if match.start() < idx and match.end() > idx:
    #                 isQuote = True
    #                 break
    #         if not isQuote:
    #             givenLine = givenLine[:idx]
    #
    #     return givenLine


    # replaces the lines in the line array with new lines - without comments
    def _removeComments(self):
        new_lines = []
        comment_mode = False
        for i in range(len(self._lines)):
            line = self._lines[i]
            if line.startswith('//'):
                continue

            if '//' in line and self._isntInString(line, '//'):
                new_lines.append(line[:line.index('//')].strip())
                continue

            if '*/' in line and comment_mode:
                comment_mode = False
                continue

            if comment_mode:
                continue

            if '/*' in line and self._isntInString(line, '/*'):
                if '*/' in line and self._isntInString(line, '*/'):
                    new_lines.append((line[:line.index('/*')] + line[line.index('*/') + 2:]).strip())
                    continue

                new_lines.append(line[:line.index('/*')].strip())
                comment_mode = True
                continue

            # if there isn't a comment in the line
            new_lines.append(line)

        self._lines = new_lines


    # check if comment sign is in a string
    def _isntInString(self, str, sign):
        c = str[:str.index(sign)].count('"')
        if c % 2 == 0:
            return True

        return False





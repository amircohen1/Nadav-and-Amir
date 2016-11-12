# This file contains the main function that runs the tokenizer and the
# compilation engine one after the other.

import JackTokenizer
import CompilationEngine
import sys
import os

if __name__ == "__main__":
    path = ""
    try:
        path = sys.argv[1]
    except:
        exit(0)

    # check if the given path is a file or a directory
    if os.path.isfile(path):
        jackTokenizer = JackTokenizer.JackTokenizer(path)
        compilationEngine = CompilationEngine.CompilationEngine(jackTokenizer.outPath)
    elif os.path.isdir(path): # TODO check if it works with a few files
        for file in os.listdir(path):
            if file.endswith("TT.xml"): #TODO change 'TT' to 'T' before submission
                jackTokenizer = JackTokenizer.JackTokenizer(path)
                compilationEngine = CompilationEngine.CompilationEngine(jackTokenizer.outPath)

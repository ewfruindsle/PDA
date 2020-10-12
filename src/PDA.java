import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

enum PDA_STATES {
    E("<E>"),
    E2("<E2>"),
    T("<T>"),
    T2("<T2>"),
    F("<F>");

    private String value;

    PDA_STATES(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


enum PDA_INPUT {
    A("a"),
    B("b"),
    C("c"),
    PLUS("+"),
    ASTERISK("*"),
    LEFT_BRACKET("("),
    RIGHT_BRACKET(")"),
    END("$");

    private String value;

    PDA_INPUT(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

public class PDA {
    PDA_STATES currentState = PDA_STATES.E;
    PDA_INPUT currentInputElement;
    String nextStackString;
    String convertedInputString;
    Stack<String> stackOfPDA = new Stack<>();

    public void analyseToTxt(String strForAnalysis) {
        int count = 1;
        String poppedValue;
        String formattedString;
        try (FileWriter writer = new FileWriter("result.txt",true)) {
            writer.write("INPUT STRING: " + strForAnalysis + "\n");
            writer.write("_________________________________________________________________________\n");
            writer.write("| № |                STACK                |     INPUT     |    OUTPUT    |\n");
            writer.write("_________________________________________________________________________\n");
            strForAnalysis = strForAnalysis + "$";
            formattedString = String.format("|%-3d|%-37s|%-15s|              |\n", count, stackOfPDA,strForAnalysis);
            writer.write(formattedString);
            convertToPdaInputFormat(strForAnalysis);
            currentInputElement = getNextInput();
            stackOfPDA.push("$");
            stackOfPDA.push("E");
            count++;
            while (true) {
                poppedValue = stackOfPDA.pop();
                if (poppedValue.equals("$")) break;
                if (poppedValue.equals("a") || poppedValue.equals("b") || poppedValue.equals("c") || poppedValue.equals("+") ||
                        poppedValue.equals("*") || poppedValue.equals(")") || poppedValue.equals("(") || poppedValue.equals("eps")) {
                    currentInputElement = getNextInput();
                    strForAnalysis = strForAnalysis.substring(1);
                    formattedString = String.format("|%-3d|%-37s|%-15s|              |\n", count, stackOfPDA,strForAnalysis);
                    writer.write(formattedString);

                } else if (poppedValue.equals("error")) {
                    writer.write("|___________________________ERROR OCCURRED_______________________________|");
                    break;
                } else {
                    currentState = PDA_STATES.valueOf(poppedValue);
                    getNextStackString();
                    addStatesToStack();
                    formattedString = String.format("|%-3d|%-37s|%-15s|%-2s -> %-8s|\n", count, stackOfPDA,strForAnalysis,poppedValue,getNextStackString());
                    writer.write(formattedString);
                }
                count++;
            }
            writer.write("_________________________________________________________________________\n\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private PDA_INPUT getNextInput() {
        PDA_INPUT nextInput = null;
        String subStr = null;
        int start = convertedInputString.indexOf(" ");
        if (start < 0) subStr = convertedInputString;
        else {
            subStr = convertedInputString.substring(0, start);
            convertedInputString = convertedInputString.substring(start + 1);
        }
        nextInput = PDA_INPUT.valueOf(subStr);
        return nextInput;
    }

    private String getNextStackString() {
        switch (currentState) {
            case E:
                switch (currentInputElement) {
                    case A, B, C:
                        nextStackString = "T,E2";
                        break;
                    case PLUS, ASTERISK, RIGHT_BRACKET, END:
                        nextStackString = "error";
                        break;
                    case LEFT_BRACKET:
                        nextStackString = "T,E2";
                        break;
                }
                break;
            case E2:
                switch (currentInputElement) {
                    case A, B, C, ASTERISK, LEFT_BRACKET:
                        nextStackString = "error";
                        break;
                    case PLUS:
                        nextStackString = "+,T,E2";
                        break;
                    case RIGHT_BRACKET, END:
                        nextStackString = "eps";
                        break;
                }
                break;
            case T:
                switch (currentInputElement) {
                    case A, B, C:
                    case LEFT_BRACKET:
                        nextStackString = "F,T2";
                        break;
                    case PLUS, ASTERISK, RIGHT_BRACKET, END:
                        nextStackString = "error";
                        break;
                }
                break;
            case T2:
                switch (currentInputElement) {
                    case A, B, C, LEFT_BRACKET:
                        nextStackString = "error";
                        break;
                    case ASTERISK:
                        nextStackString = "*,F,T2";
                        break;
                    case PLUS, RIGHT_BRACKET, END:
                        nextStackString = "eps";
                        break;

                }
                break;
            case F:
                switch (currentInputElement) {
                    case A:
                        nextStackString = "a";
                        break;
                    case B:
                        nextStackString = "b";
                        break;
                    case C:
                        nextStackString = "c";
                        break;
                    case PLUS, ASTERISK, RIGHT_BRACKET, END:
                        nextStackString = "error";
                        break;
                    case LEFT_BRACKET:
                        nextStackString = "(,E,)";
                        break;
                }
                break;
        }
        return nextStackString;
    }

    private void addStatesToStack() {
        int start = 0;
        String elementToStack;
        while (start > -1) {
            start = nextStackString.lastIndexOf(",");
            if (start == -1) {
                if (nextStackString.equals("eps"))
                    break;
                else
                    elementToStack = nextStackString;
            } else {
                elementToStack = nextStackString.substring(start + 1);
                if (elementToStack.equals("eps")) break;
                nextStackString = nextStackString.substring(0, start);
            }
            stackOfPDA.push(elementToStack);
        }
    }

    private void convertToPdaInputFormat(String str) {
        char charToConvert;
        convertedInputString = " ";
        for (int i = 0; i < str.length(); i++) {
            charToConvert = str.charAt(i);
            convertedInputString += getPdaInputElementFromChar(charToConvert);

        }
        convertedInputString = convertedInputString.trim();
    }

    private String getPdaInputElementFromChar(char c) {
        String convertedString = null;
        switch (c) {
            case 'a':
                convertedString = "A ";
                break;
            case 'b':
                convertedString = "B ";
                break;
            case 'c':
                convertedString = "C ";
                break;
            case '+':
                convertedString = "PLUS ";
                break;
            case '*':
                convertedString = "ASTERISK ";
                break;
            case '(':
                convertedString = "LEFT_BRACKET ";
                break;
            case ')':
                convertedString = "RIGHT_BRACKET ";
                break;
            case '$':
                convertedString = "END ";
                break;
        }
        return convertedString;
    }

}
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Lexer implements ILexer {

    private final char[] chars;
    private int pos = 0;
    private int startPos;
    private int line = 1;
    private int columnPos = 1;



    private boolean reachedEOF = false;
    private boolean eofReached = false;


    private enum State {
        START, IN_STRING, IN_IDENT, HAVE_ZERO, IN_NUM,
        HAVE_EQ, HAVE_MINUS, HAVE_LT, HAVE_GT, HAVE_COLON,
        HAVE_AMP,
        HAVE_TIMES,
        HAVE_BLOCK_OPEN,
        HAVE_BLOCK_CLOSE,
        HAVE_RARROW,
        HAVE_LSQUARE,
    }

    private State state = State.START;

    private static final Map<String, Kind> RESERVED_WORDS;

    static {
        Map<String, Kind> map = new HashMap<>();
        map.put("image", Kind.RES_image);
        map.put("pixel", Kind.RES_pixel);
        map.put("int", Kind.RES_int);
        map.put("string", Kind.RES_string);
        map.put("void", Kind.RES_void);
        map.put("boolean", Kind.RES_boolean);
        map.put("nil", Kind.RES_nil);
        map.put("write", Kind.RES_write);
        map.put("height", Kind.RES_height);
        map.put("width", Kind.RES_width);
        map.put("if", Kind.RES_if);
        map.put("fi", Kind.RES_fi);
        map.put("do", Kind.RES_do);
        map.put("od", Kind.RES_od);
        map.put("red", Kind.RES_red);
        map.put("green", Kind.RES_green);
        map.put("blue", Kind.RES_blue);
        map.put("Z", Kind.CONST);
        map.put("BLACK", Kind.CONST);
        map.put("BLUE", Kind.CONST);
        map.put("CYAN", Kind.CONST);
        map.put("LIGHT_GRAY", Kind.CONST);
        map.put("DARK_GRAY", Kind.CONST);
        map.put("GRAY", Kind.CONST);
        map.put("GREEN", Kind.CONST);
        map.put("MAGENTA", Kind.CONST);
        map.put("ORANGE", Kind.CONST);
        map.put("PINK", Kind.CONST);
        map.put("RED", Kind.CONST);
        map.put("WHITE", Kind.CONST);
        map.put("YELLOW", Kind.CONST);
        map.put("TRUE", Kind.BOOLEAN_LIT);
        map.put("FALSE", Kind.BOOLEAN_LIT);

        RESERVED_WORDS = Collections.unmodifiableMap(map);
    }


    public Lexer(String input) {
        this.chars = (input + "\0").toCharArray();
    }

    @Override
    public IToken next() throws LexicalException {
        if (reachedEOF) {
            return new Token(Kind.EOF, startPos, 1, null, new SourceLocation(line, startPos)); // Return an EOF token
        }
        if (eofReached) {
            throw new LexicalException(new SourceLocation(line, pos), "End of file reached");
        }


        IToken resultToken = null;

        while (resultToken == null && !reachedEOF) {
            char ch = chars[pos];
            switch (state) {
                case START -> resultToken = handleStart(ch);
                case IN_IDENT -> resultToken = handleIdentifier(ch);
                case IN_STRING -> resultToken = handleString(ch);
                case HAVE_ZERO -> resultToken = handleZero(ch);
                case IN_NUM -> resultToken = handleNumber(ch);
                case HAVE_EQ -> resultToken = handleEqual(ch);
                case HAVE_MINUS -> resultToken = handleMinus(ch);
                case HAVE_LT ->  resultToken = handleLT(ch);
                case HAVE_GT ->  resultToken = handleGT(ch);
                case HAVE_LSQUARE -> resultToken = handleLSquare(ch);
                case HAVE_AMP -> resultToken = handleBitAnd(ch);
                default -> throw new IllegalStateException("Lexer bug");
            }
        }

        return resultToken;
    }


    private IToken handleStart(char ch) throws LexicalException {
        skipWhitespace();
        startPos = pos;

        switch (ch) {
            case ' ', '\t', '\r' -> {
                pos++;
                columnPos++;
                return null;
            }
            case '\n' -> {
                line++;
                columnPos = 1;
                pos++;
                return null;
            }
            case '#' -> {
                if (chars[pos + 1] == '#') {
                    pos += 2;
                    while (chars[pos] != '\n' && chars[pos] != '\0') {
                        pos++;
                    }
                    return null;
                } else {
                    SourceLocation errorLocation = new SourceLocation(line, startPos);
                    throw new LexicalException(errorLocation, "Unrecognized token at position: " + startPos);
                }
            }
            case '+' -> {
                pos++;
                state = State.START;
                return createToken(Kind.PLUS, startPos, 1);
            }
            case '-' -> {
                pos++;
                state = State.HAVE_MINUS;
                return null;
            }
            case ',' -> {
                pos++;
                state = State.START;
                return createToken(Kind.COMMA, startPos, 1, chars);
            }
            case ';' -> {
                pos++;
                state = State.START;
                return createToken(Kind.SEMI, startPos, 1);
            }
            case '?' -> {
                pos++;
                state = State.START;
                return createToken(Kind.QUESTION, startPos, 1);
            }
            case '_' -> {
                state = State.IN_IDENT;
                pos++;
                return null;
            }
            case ':' -> {
                if (chars[pos + 1] == '>') {
                    pos += 2;
                    state = State.START;
                    return createToken(Kind.BLOCK_CLOSE, startPos, 2);
                } else {
                    pos++;
                    state = State.START;
                    return createToken(Kind.COLON, startPos, 1);
                }
            }
            case '(' -> {
                pos++;
                state = State.START;
                return createToken(Kind.LPAREN, startPos, 1);
            }
            case ')' -> {
                pos++;
                state = State.START;
                return createToken(Kind.RPAREN, startPos, 1);
            }
            case '<' -> {
                pos++;
                state = State.HAVE_LT;
                return null;

            }
            case '>' -> {
                pos++;
                state = State.HAVE_GT;
//                return createToken(Kind.GT, startPos, 1);
                return null;
            }
            case '[' -> {
                pos++;
                state = State.HAVE_LSQUARE;
                return null;
            }
            case ']' -> {
                pos++;
                state = State.START;
                return createToken(Kind.RSQUARE, startPos, 1);
            }
            case '=' -> {
                pos++;
                state = State.HAVE_EQ;
                return null;
            }
            case '!' -> {
                pos++;
                state = State.START;
                return createToken(Kind.BANG, startPos, 1);
            }
            case '*' -> {
                if (chars[pos + 1] == '*') {
                    pos += 2;
                    state = State.START;
                    return createToken(Kind.EXP, startPos, 2);
                } else {
                    pos++;
                    state = State.START;
                    return createToken(Kind.TIMES, startPos, 1);
                }
            }
            case '/' -> {
                if (chars[pos + 1] == '*') {
                    pos += 2;
                    while (!(chars[pos] == '*' && chars[pos + 1] == '/') && chars[pos] != '\0') {
                        pos++;
                    }
                    if (chars[pos] == '*' && chars[pos + 1] == '/') {
                        pos += 2;
                    } else {
                        SourceLocation errorLocation = new SourceLocation(line, startPos);
                        throw new LexicalException(errorLocation, "Unterminated comment starting at position: " + startPos);
                    }
                    return null;
                } else {
                    pos++;
                    state = State.START;
                    return createToken(Kind.DIV, startPos, 1);
                }
            }
            case '%' -> {
                pos++;
                state = State.START;
                return createToken(Kind.MOD, startPos, 1);
            }
            case '|' -> {
                if (chars[pos + 1] == '|') {
                    pos += 2;
                    state = State.START;
                    return createToken(Kind.OR, startPos, 2);
                } else {
                    pos++;
                    state = State.START;
                    return createToken(Kind.BITOR, startPos, 1);
                }
            }
            case '&' -> {
                pos++;
                state = State.HAVE_AMP;
                return null;
            }
            case '^' -> {
                pos++;
                state = State.START;
                return createToken(Kind.RETURN, startPos, 1);
            }
            case '"' -> {
                pos++;
                state = State.IN_STRING;
                return null;
            }
            case '0' -> {
                pos++;
                state = State.HAVE_ZERO;
                return null;
            }

            case '\0' -> {
                reachedEOF = true;
                eofReached = true;
                return createToken(Kind.EOF, startPos, 1);
            }

            default -> {
                if (Character.isLetter(ch)) {
                    state = State.IN_IDENT;
                    pos++;
                    return null;
                } else if (Character.isDigit(ch)) {
                    state = State.IN_NUM;
                    pos++;
                    return null;
                } else {
                    SourceLocation errorLocation = new SourceLocation(line, startPos);
                    throw new LexicalException(errorLocation, "Unreacognized token at position: " + startPos);
                }
            }
        }
    }

    private IToken handleIdentifier(char ch) {
        if (!Character.isLetterOrDigit(ch) && ch != '_') {
            String identifier = new String(chars, startPos, pos - startPos);

            Kind kind;
//            if (RESERVED_WORDS.containsKey(identifier)) {
//                kind = RESERVED_WORDS.get(identifier);
//            } else {
//                kind = Kind.IDENT;
//            }
            kind = RESERVED_WORDS.getOrDefault(identifier, Kind.IDENT);

            state = State.START;
            return createToken(kind, startPos, identifier.length(), chars);
        } else {
            pos++;
            return null;
        }
    }



    // SOLVED
    private IToken handleString(char ch) throws LexicalException {
        if (ch == '"') {
            String stringValue = new String(chars, startPos + 1, pos - startPos - 1);
            pos++;
            state = State.START;
            return createToken(STRING_LIT, startPos, stringValue.length() + 2, chars);
        } else if (ch == '\0' || ch == '\n') {
            throw new LexicalException(new SourceLocation(line, pos), "unclosed string starting at position: " + startPos);
        }  else {
            pos++;
            return null;
        }
    }


//    getSourceLocation method
    private SourceLocation getSourceLocation() {
        return new SourceLocation(line, columnPos);
    }






    // EXTRA 1
//    private IToken handleString(char ch) throws LexicalException {
//        if (ch == '"') {
//            String stringValue = new String(chars, startPos + 1, pos - startPos - 1);
//            pos++;
//            state = State.START;
//            return createToken(STRING_LIT, startPos, stringValue.length() + 2, chars);
//        } else if (ch == '\0') {
//            throw new LexicalException(new SourceLocation(line, pos), "Unclosed string starting at position: " + startPos);
//        } else if (ch == '\n') {
//            line++;
//            pos++;
//            throw new LexicalException(new SourceLocation(line, pos), "Unclosed string starting at position: " + startPos);
//        } else {
//            pos++;
//            return null;
//        }
//    }



    private IToken handleNumber(char ch) throws LexicalException {
        if (Character.isDigit(ch)) {
            pos++;
            return null;
        } else {
            String numString = new String(chars, startPos, pos - startPos);
            if (numString.length() > 10) {
                SourceLocation errorLocation = new SourceLocation(line, startPos);
                throw new LexicalException(errorLocation, "Number is too large at position: " + startPos);
            }

            state = State.START;
            return createToken(NUM_LIT, startPos, numString.length(), chars);
        }
    }


    private IToken handleZero(char ch) {
        if (Character.isDigit(ch) && ch != '0') {
            state = State.START;
            return createToken(NUM_LIT, startPos, 1, chars);
        } else {
            pos++;
            state = State.START;
            return createToken(NUM_LIT, startPos, 1, chars);
        }
    }




    private IToken handleEqual(char ch) {
        if (ch == '=') {
            pos++;
            state = State.START;
            return createToken(Kind.EQ, startPos, 2);  // ==
        } else {
            state = State.START;
            return createToken(Kind.ASSIGN, startPos, 1);  // =
        }
    }

    private IToken handleMinus(char ch) {
        if (ch == '>') {
            pos++;
            state = State.START;
            return createToken(Kind.RARROW, startPos, 2);
        } else {
            state = State.START;
            return createToken(Kind.MINUS, startPos, 1);
        }
    }

    private IToken handleLT(char ch) {
        if (ch == '=') {
            pos++;
            state = State.START;
            return createToken(Kind.LE, startPos, 2);
        } else if (ch == ':') {
            pos++;
            state = State.START;
            return createToken(Kind.BLOCK_OPEN, startPos, 2);
        } else {
            state = State.START;
            return createToken(Kind.LT, startPos, 1);
        }
    }

    private IToken handleGT(char ch) {
        if (ch == '=') {
            pos++;
            state = State.START;
            return createToken(Kind.GE, startPos, 2);
        } else if (ch == ':') {
            pos++;
            state = State.START;
            return createToken(Kind.BLOCK_CLOSE, startPos, 2);
        } else {
            state = State.START;
            return createToken(Kind.GT, startPos, 1);
        }
    }

    private IToken handleLSquare(char ch) {
        if (ch == ']') {
            pos++;
            state = State.START;
            return createToken(Kind.BOX, startPos, 2);
        } else {
            state = State.START;
            return createToken(Kind.LSQUARE, startPos, 1);
        }
    }

    private IToken handleBitAnd(char ch) {
        IToken token;
        if (ch == '&') {
//            pos += 2;
            pos++;
            token = createToken(Kind.AND, startPos, 2);
        } else {
            pos++;
            token = createToken(Kind.BITAND, startPos, 1);
        }
        startPos = pos;
        state = State.START;
        return token;
    }


    private void skipWhitespace() {
        int currentPos = pos;
        while (currentPos < chars.length) {
            char ch = chars[currentPos];
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                if (ch == '\n') line++;
                currentPos++;
            } else {
                break;
            }
        }
    }


//    private IToken createToken(Kind kind, int startPos, int length) {
//        IToken token = new Token(kind, startPos, length, null, new SourceLocation(line, startPos));
//        System.out.println("Token: " + kind + " at position " + startPos);
//        return token;
//    }

    private IToken createToken(Kind kind, int startPos, int length) {
        return getiToken(kind, startPos, length, chars);
    }


//    private IToken getiToken(Kind kind, int startPos, int length, char[] chars) {
//        char[] value = Arrays.copyOfRange(chars, startPos, startPos + length);
//        IToken token = new Token(kind, startPos, length, value, new SourceLocation(line, startPos));
//        System.out.println("Token: " + kind + " and value: " + Arrays.toString(value) + " at position " + startPos);
//        return token;
//    }

    private IToken getiToken(Kind kind, int startPos, int length, char[] chars) {
        char[] value = Arrays.copyOfRange(chars, startPos, startPos + length);
        IToken token = new Token(kind, startPos, length, value, new SourceLocation(line, columnPos));
//        System.out.println("Token: " + kind + " and value: " + Arrays.toString(value) + " at position " + startPos);
        columnPos += length;
        return token;
    }


//    private IToken createToken(Kind kind, int startPos, int length, char[] value) {
//        IToken token = new Token(kind, startPos, length, value, new SourceLocation(line, startPos));
//        System.out.println("Token: " + kind + " and value: " + Arrays.toString(value) + " at position " + startPos);
//        return token;
//    }

    private IToken createToken(Kind kind, int startPos, int length, char[] source) {
        return getiToken(kind, startPos, length, source);
    }


}
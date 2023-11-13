/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.BOOLEAN_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;

import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

/*

Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z 
    ExpandedPixel  
ChannelSelector ::= : red | : green | : blue
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken token;


	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		token = lexer.next();
	}


	@Override
	public AST parse() throws  PLCCompilerException {
		Expr e = expr();
		return e;
	}

	/* *****************************  MOKSH ***************************** */

	// match the expected kind and move to the next token
//	private void match(Kind expectedKind) throws SyntaxException {
//		if (t.kind() == expectedKind) {
//			try {
//				t = lexer.next();  // Move to the next token
//			} catch (LexicalException e) {
//				throw new SyntaxException(t.sourceLocation(), "Lexical error while trying to match " + expectedKind);
//			}
//		} else {
//			throw new SyntaxException(t.sourceLocation(), "Expected " + expectedKind + " but found " + t.kind());
//		}
//	}


	// match the expected kind and move to the next token
	private void match(Kind expectedKind) throws LexicalException, SyntaxException {
		if (token.kind() == expectedKind) {
			try {
				token = lexer.next();
			} catch (LexicalException e) {
				throw new LexicalException(token.sourceLocation(), "Lexical error while trying to match " + expectedKind);
			}
		} else {
			throw new SyntaxException(token.sourceLocation(), "Expected " + expectedKind + " but found " + token.kind());
		}
	}

	// Expr ::=  ConditionalExpr | LogicalOrExpr
	private Expr expr() throws  PLCCompilerException {
		if (token.kind() == Kind.QUESTION) {
			return conditionalExpr();
		} else {
			return logicalOrExpr();
		}
	}



	// ConditionalExpr ::=  ?  Expr  : -> Expr  : , Expr
//	private ConditionalExpr conditionalExpr() throws PLCCompilerException {
//		match(Kind.QUESTION);
//		Expr condition = expr();
//		match(Kind.RARROW);
//		Expr trueExpr = expr();
//		match(Kind.COMMA);
//		Expr falseExpr = expr();
//		return new ConditionalExpr(token, condition, trueExpr, falseExpr);
//	}

	private ConditionalExpr conditionalExpr() throws PLCCompilerException {
		match(Kind.QUESTION);
		Expr condition = expr();
		match(Kind.RARROW);
		Expr trueExpr;
		if (Kind.QUESTION == token.kind()) {
			trueExpr = conditionalExpr();
		} else {
			trueExpr = expr();
		}
		match(Kind.COMMA);
		Expr falseExpr = expr();
		return new ConditionalExpr(token, condition, trueExpr, falseExpr);
	}



	// LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
	private Expr logicalAndExpr() throws PLCCompilerException {
		Expr left = comparisonExpr();
		while (token.kind() == Kind.BITAND || token.kind() == Kind.AND) {
			IToken opToken = token;
			match(token.kind());
			Expr right = comparisonExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}


	// LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
//	private Expr logicalOrExpr() throws PLCCompilerException {
//		Expr left = logicalAndExpr();
//		while (token.kind() == Kind.OR || token.kind() == Kind.OR) {
//			IToken opToken = token;
//			match(token.kind());
//			Expr right = logicalAndExpr();
//			left = new BinaryExpr(token, left, opToken, right);
//		}
//		return left;
//	}

	private Expr logicalOrExpr() throws PLCCompilerException {
		Expr left = logicalAndExpr();

		while (token.kind() == Kind.BITOR || token.kind() == Kind.OR) {
			IToken opToken = token;
			match(token.kind());
			Expr right = logicalAndExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}


	/* *****************************  Daniel  ***************************** */

	// ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
	private Expr comparisonExpr() throws PLCCompilerException {
		Expr left = powExpr();
		while (Arrays.asList(Kind.LT, Kind.GT, Kind.EQ, Kind.LE, Kind.GE).contains(token.kind())) {
			IToken opToken = token;
			match(token.kind());
			Expr right = powExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}

	// PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
	private Expr powExpr() throws PLCCompilerException {
		Expr left = additiveExpr();
		if (token.kind() == Kind.EXP) {
			IToken opToken = token;
			match(Kind.EXP);
			Expr right = powExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}

	// AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
	private Expr additiveExpr() throws PLCCompilerException {
		Expr left = multiplicativeExpr();
		while (token.kind() == Kind.PLUS || token.kind() == Kind.MINUS) {
			IToken opToken = token;
			match(token.kind());
			Expr right = multiplicativeExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}

	// MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
	private Expr multiplicativeExpr() throws PLCCompilerException {
		Expr left = unaryExpr();
		while (token.kind() == Kind.TIMES || token.kind() == Kind.DIV || token.kind() == Kind.MOD) {
			IToken opToken = token;
			match(token.kind());
			Expr right = unaryExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}

	// UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
	private Expr unaryExpr() throws PLCCompilerException {
		if (token.kind() == Kind.BANG || token.kind() == Kind.MINUS ||
				token.kind() == Kind.RES_width || token.kind() == Kind.RES_height) {
			IToken opToken = token;
			match(token.kind());
			Expr expression = unaryExpr();
			return new UnaryExpr(token, opToken, expression);
		} else {
			return postfixExpr();
		}
	}

	/* *****************************  Moksh  ***************************** */

	// UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
//	private Expr postfixExpr() throws PLCCompilerException {
//		Expr expression = primaryExpr();
//		PixelSelector pixelSelector = null;
//		ChannelSelector channelSelector = null;
//
//		if (token.kind() == Kind.LSQUARE) {
//			pixelSelector = pixelSelector();
//		}
//		if (token.kind() == Kind.COLON) {
//			channelSelector = channelSelector();
//		}
//		if (pixelSelector != null || channelSelector != null) {
//			return new PostfixExpr(token, expression, pixelSelector, channelSelector);
//		}
//		return expression;
//	}

	private Expr postfixExpr() throws PLCCompilerException {
		Expr expression = primaryExpr();
		PixelSelector pixelSelector = null;
		ChannelSelector channelSelector = null;

		// Check for PixelSelector
		if (token.kind() == Kind.LSQUARE) {
			pixelSelector = pixelSelector();
		}

		// Check for ChannelSelector
		if (token.kind() == Kind.COLON) {
			channelSelector = channelSelector();
		}

		// If we encountered either a PixelSelector or ChannelSelector or both,
		// wrap the original expression in a PostfixExpr.
		if (pixelSelector != null || channelSelector != null) {
			return new PostfixExpr(token, expression, pixelSelector, channelSelector);
		}

		// If no PixelSelector or ChannelSelector was found, just return the primary expression.
		return expression;
	}


	// PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z
	private Expr primaryExpr() throws PLCCompilerException {
        switch (token.kind()) {
            case STRING_LIT -> {
                StringLitExpr stringLit = new StringLitExpr(token);
                match(STRING_LIT);
                return stringLit;
            }
            case NUM_LIT -> {
                NumLitExpr numLit = new NumLitExpr(token);
                match(NUM_LIT);
                return numLit;
            }
			case BOOLEAN_LIT -> {
				BooleanLitExpr booleanLit = new BooleanLitExpr(token);
				match(BOOLEAN_LIT);
				return booleanLit;
			}
			case IDENT -> {
				if ("true".equals(token.text()) || "false".equals(token.text())) {
					BooleanLitExpr booleanLit = new BooleanLitExpr(token);
					match(IDENT);
					return booleanLit;
				} else {
					IdentExpr ident = new IdentExpr(token);
					match(IDENT);
					return ident;
				}
			}
            case LPAREN -> {
                match(LPAREN);
                Expr expression = expr();
                match(RPAREN);
                return expression;
            }
            case CONST -> {
                ConstExpr constExpr = new ConstExpr(token);
                match(CONST);
                return constExpr;
            }
            case LSQUARE -> {
                return expandedPixelExpr();
            }
            default -> throw new SyntaxException(token.sourceLocation(), "Expected token of kind ...");
        }
	}

	/* *****************************  Daniel  ***************************** */

	// PixelSelector  ::= [ Expr , Expr ]
	private PixelSelector pixelSelector() throws PLCCompilerException {
		match(LSQUARE);
		Expr xExpr = expr();
		match(COMMA);
		Expr yExpr = expr();
		match(RSQUARE);
		return new PixelSelector(token, xExpr, yExpr);
	}

	// ChannelSelector ::= : red | : green | : blue
	private ChannelSelector channelSelector() throws PLCCompilerException {
		match(COLON);
		IToken channelToken = token;
		if (channelToken.kind() == RES_red || channelToken.kind() == RES_green || channelToken.kind() == RES_blue) {
			match(channelToken.kind());
			return new ChannelSelector(token, channelToken);
		} else {
			throw new SyntaxException(token.sourceLocation(), "Expected red, green, or blue after colon for ChannelSelector.");
		}
	}

	// ExpandedPixel ::= [ Expr , Expr , Expr ]
	private ExpandedPixelExpr expandedPixelExpr() throws PLCCompilerException {
		match(LSQUARE);
		Expr e1 = expr();
		match(COMMA);
		Expr e2 = expr();
		match(COMMA);
		Expr e3 = expr();
		match(RSQUARE);
		return new ExpandedPixelExpr(token, e1, e2, e3);
	}


}

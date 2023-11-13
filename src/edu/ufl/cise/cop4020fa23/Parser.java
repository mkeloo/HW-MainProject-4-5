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

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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




public class Parser implements IParser {

	final ILexer lexer;
	private IToken token;

	public Parser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		token = lexer.next();
	}



	/* *****************************  MOKSH ***************************** */

	@Override
	public AST parse() throws SyntaxException, PLCCompilerException {
		AST e = program();
		// if there are still tokens remaining at the end, this should result in a SyntaxException
		if (token.kind() != EOF) {
			throw new SyntaxException(token.sourceLocation(), "Expected end of file but found " + token.kind());
		}
		return e;
	}


// ************************************ START OF Expression Parser Code **************************************** //

	// match the expected kind and move to the next token
	private IToken match(Kind expectedKind) throws LexicalException, SyntaxException {
//		System.out.println("Matching " + expectedKind + ", current token is " + token.kind()); // Debug print
		if (token.kind() == expectedKind) {
			try {
				IToken currentToken = token;
				token = lexer.next();
				return currentToken;
			} catch (LexicalException e) {
				throw new LexicalException(token.sourceLocation(), "Lexical error while trying to match " + expectedKind + ": " + e.getMessage());
			}
		} else {
			throw new SyntaxException(token.sourceLocation(), "Expected " + expectedKind + " but found " + token.kind());
		}
	}



	// Expr ::=  ConditionalExpr | LogicalOrExpr
	private Expr expr() throws SyntaxException, PLCCompilerException {
		if (token.kind() == Kind.QUESTION) {
			return conditionalExpr();
		} else {
			return logicalOrExpr();
		}
	}


	//	 ConditionalExpr ::=  ?  Expr  : -> Expr  : , Expr
	private ConditionalExpr conditionalExpr() throws SyntaxException, PLCCompilerException {
		match(Kind.QUESTION);
		Expr condition = expr();
		match(Kind.RARROW);
		Expr trueExpr = expr();
		match(Kind.COMMA);
		Expr falseExpr = expr();
		return new ConditionalExpr(token, condition, trueExpr, falseExpr);
	}



	// LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
	private Expr logicalAndExpr() throws SyntaxException, PLCCompilerException {
		Expr left = comparisonExpr();
		while (token.kind() == Kind.BITAND || token.kind() == Kind.AND) {
			IToken opToken = token;
			match(token.kind());
			Expr right = comparisonExpr();
			left = new BinaryExpr(token, left, opToken, right);
		}
		return left;
	}

	// LogicalOrExpr ::=  LogicalAndExpr ( (  |  |  || ) LogicalAndExpr)*
	private Expr logicalOrExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr comparisonExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr powExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr additiveExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr multiplicativeExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr unaryExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr postfixExpr() throws SyntaxException, PLCCompilerException {
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
	private Expr primaryExpr() throws SyntaxException, PLCCompilerException {
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
	private PixelSelector pixelSelector() throws SyntaxException, PLCCompilerException {
		match(LSQUARE);
		Expr xExpr = expr();
		match(COMMA);
		Expr yExpr = expr();
		match(RSQUARE);
		return new PixelSelector(token, xExpr, yExpr);
	}

	//	 ChannelSelector ::= : red | : green | : blue
	private ChannelSelector channelSelector() throws SyntaxException, PLCCompilerException {
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
	private ExpandedPixelExpr expandedPixelExpr() throws SyntaxException, PLCCompilerException {
		match(LSQUARE);
		Expr e1 = expr();
		match(COMMA);
		Expr e2 = expr();
		match(COMMA);
		Expr e3 = expr();
		if (isKind(RSQUARE)) {
			match(RSQUARE);
		}
		return new ExpandedPixelExpr(token, e1, e2, e3);
	}

// ************************************ START OF Expression Parser Code **************************************** //


	/*	*****************************  NEW ASSIGMENT 2 CODE  *****************************  */



	/* *****************************  MOKSH  ***************************** */

	// helper kind method
	private boolean isKind(Kind kind) {
		return token.kind() == kind;
	}

	// Method to parse the Type rule ::=> Type ::= image | pixel | int | string | boolean | void
	private IToken type() throws LexicalException, SyntaxException {
		if (isKind(Kind.RES_image)) {
			IToken typeToken = token;
			match(Kind.RES_image);
			return typeToken;
		} else if (isKind(Kind.RES_pixel)) {
			IToken typeToken = token;
			match(Kind.RES_pixel);
			return typeToken;
		} else if (isKind(Kind.RES_int)) {
			IToken typeToken = token;
			match(Kind.RES_int);
			return typeToken;
		} else if (isKind(Kind.RES_string)) {
			IToken typeToken = token;
			match(Kind.RES_string);
			return typeToken;
		} else if (isKind(Kind.RES_boolean)) {
			IToken typeToken = token;
			match(Kind.RES_boolean);
			return typeToken;
		} else if (isKind(Kind.RES_void)) {
			IToken typeToken = token;
			match(Kind.RES_void);
			return typeToken;
		} else {
			throw new SyntaxException(token.sourceLocation(), "expected type but got: " + token.kind());
		}
	}


	// Method to parse the Program rule ::=> Program::= Type IDENT ( ParamList ) Block
	public AST program() throws SyntaxException, PLCCompilerException {
		if (isType(token)) {
			IToken type = type();
			IToken ident = match(Kind.IDENT);
			match(Kind.LPAREN);
			List<NameDef> paramList = paramList();
			match(Kind.RPAREN);
			Block block = block();
			return new Program(token, type, ident, paramList, block);
		}
		// token = !type, then just parse an exper
		else {
			Expr e = expr();
			return e;
		}
	}

	// helper method for program() to chcek if the token is a type
	private boolean isType(IToken token) {
		return isKind(Kind.RES_image) || isKind(Kind.RES_pixel)
				||  isKind(Kind.RES_int) ||  isKind(Kind.RES_string)
				|| isKind(Kind.RES_boolean) || isKind(Kind.RES_void);
	}


	// method to parse the Statement rule ::=>
	// Statement::=
	//       LValue = Expr |
	//       write Expr |
	//       do GuardedBlock [] GuardedBlock* od |
	//      if GuardedBlock [] GuardedBlock* if |
	//      ^ Expr |
	//     BlockStatement |
	private Statement statement() throws SyntaxException, PLCCompilerException {
		if (isKind(Kind.IDENT)) {
			LValue lvalue = lvalue();
			match(Kind.ASSIGN);
			Expr expr = expr();
			return new AssignmentStatement(token, lvalue, expr);
		}
		else if (isKind(Kind.BLOCK_OPEN)) {
			Block nestedBlock = block();
			return new StatementBlock(token, nestedBlock);
		}
		else if (isKind(Kind.RES_write)) {
			match(Kind.RES_write);
			Expr expr = expr();
			return new WriteStatement(token, expr);
		}
		else if (isKind(Kind.RETURN)) {
			match(Kind.RETURN);
			Expr expr = expr();
			return new ReturnStatement(token, expr);
		}
		else if (isKind(Kind.RES_do)) {
			return doStatement();
		}
		else if (isKind(Kind.RES_if)) {
			return ifStatement();
		}
		else {
			throw new SyntaxException("Unexpected token in statement: " + token.kind());
		}
	}


	// Method to parse the DoStatement rule ::=> do GuardedBlock [] GuardedBlock* od
	private Statement doStatement() throws SyntaxException, PLCCompilerException {
		match(Kind.RES_do);
		List<GuardedBlock> guardedBlocks = new ArrayList<>();
		guardedBlocks.add(guardedBlock());
		while (isKind(Kind.BOX)) {
			match(Kind.BOX);
			guardedBlocks.add(guardedBlock());
		}
		match(Kind.RES_od);
		return new DoStatement(token, guardedBlocks);
	}


	// Method to parse the IfStatement rule ::=> if GuardedBlock [] GuardedBlock* fi
	private IfStatement ifStatement() throws SyntaxException, PLCCompilerException {
		match(Kind.RES_if);
		List<GuardedBlock> guardedBlocks = new ArrayList<>();
		guardedBlocks.add(guardedBlock());
		while (isKind(Kind.BOX)) {
			match(Kind.BOX);
			guardedBlocks.add(guardedBlock());
		}
		match(Kind.RES_fi);
		return new IfStatement(token, guardedBlocks);
	}


	// Method to parse the GuardedBlock rule ::=> GuardedBlock := Expr -> Block
	private GuardedBlock guardedBlock() throws SyntaxException, PLCCompilerException {
		Expr expr = expr();
		match(Kind.RARROW);
		Block block = block();
		return new GuardedBlock(token, expr, block);
	}




	/* *****************************  Daniel  ***************************** */

	// Method to parse the ParamList rule ::=> ParamList ::= ε | NameDef ( , NameDef ) *
	private List<NameDef> paramList() throws  PLCCompilerException {
		List<NameDef> params = new ArrayList<>();
		if (!isKind(Kind.RPAREN)) {
			params.add(nameDef());
			while (isKind(Kind.COMMA)) {
				match(Kind.COMMA);
				params.add(nameDef());
			}
		}
		return params;
	}


	// Method to parse the NameDef rule ::=> NameDef ::= Type IDENT | Type Dimension IDENT

	private NameDef nameDef() throws PLCCompilerException {
		IToken type = type();
		Dimension dimension = null;
		if (isKind(Kind.LSQUARE)) {
			dimension = dimension();
		}
		IToken ident = match(Kind.IDENT);
		return new NameDef(token, type, dimension, ident);
	}


	// Method to parse the Dimension rule ::=> Dimension ::= [ Expr , Expr ]
	private Dimension dimension() throws SyntaxException, PLCCompilerException {
		match(LSQUARE);
		Expr width = expr();
		match(COMMA);
		Expr height = expr();
		match(RSQUARE);
		return new Dimension(token, width, height);
	}



	// method to parse the Block rule ::=> Block ::= <: (Declaration ; | Statement ;)* :>
	private Block block() throws SyntaxException, PLCCompilerException {
		IToken firstToken = match(Kind.BLOCK_OPEN);  // match <:
		List<Block.BlockElem> blockElems = new ArrayList<>();
		while (!isKind(Kind.BLOCK_CLOSE) && !isKind(Kind.EOF)) {
			if (isType()) {
				Declaration decl = declaration();
				blockElems.add((Block.BlockElem) decl);
				match(Kind.SEMI);
			} else {
				Statement stmt = statement();
				blockElems.add((Block.BlockElem) stmt);
				match(Kind.SEMI);
			}
		}
		match(Kind.BLOCK_CLOSE);  // match :>
		return new Block(firstToken, blockElems);
	}


	// another helper method (overloading) for block() of type checking
	private boolean isType() {
		return isKind(Kind.RES_image) || isKind(Kind.RES_pixel)
				||  isKind(Kind.RES_int) ||  isKind(Kind.RES_string)
				|| isKind(Kind.RES_boolean) || isKind(Kind.RES_void);
	}




	// Method to parse the Declaration rule ::=> Declaration::= NameDef | NameDef = Expr
	private Declaration declaration() throws SyntaxException, PLCCompilerException {
		NameDef name = nameDef();

		if (name.getType() == Type.IMAGE && name.getDimension() == null && isKind(Kind.LSQUARE)) {
			Dimension dimension = dimension();
			IToken typeToken = name.getTypeToken();
			IToken identToken = name.getIdentToken();
			name = new NameDef(token, typeToken, dimension, identToken);
		}

		if (isKind(Kind.ASSIGN)) {
			match(Kind.ASSIGN);
			Expr expr = expr();
			return new Declaration(name.getTypeToken(), name, expr);
		} else {
			return new Declaration(name.getTypeToken(), name, null);
		}
	}






	// method to parse the LValue rule ::=> LValue ::= IDENT (PixelSelectorIn | ε ) (ChannelSelector | ε )
	private LValue lvalue() throws LexicalException, PLCCompilerException {
		IToken ident = match(Kind.IDENT);
		PixelSelector pixelSelector = null;
		ChannelSelector channelSelector = null;
		if (isKind(Kind.LSQUARE)) {
			pixelSelector = pixelSelector();
		}
		if (isKind(Kind.COLON)) {
			channelSelector = channelSelector();
		}
		return new LValue(token, ident, pixelSelector, channelSelector);
	}


}

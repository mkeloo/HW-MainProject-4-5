package edu.ufl.cise.cop4020fa23;

import java.util.Stack;


import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.*;

public class TypeCheckVisitor implements ASTVisitor {

    private SymbolTable symbolTable;

    //    helper string for context
    private static final String IN_LVALUE_CONTEXT = "IN_LVALUE_CONTEXT";


    // creatinf stack to store return types of functions/programs
    private Stack<Type> returnTypeStack = new Stack<>();

    public TypeCheckVisitor() throws TypeCheckException {
        this.symbolTable = new SymbolTable();
    }


    /* ======================= DANIEL ======================= */


    @Override
    public Object visitProgram(Program program, Object arg) throws TypeCheckException, PLCCompilerException {
        // Convert typeToken to Type
        String returnTypeString = program.getTypeToken().text();
        Type returnType = Type.valueOf(returnTypeString.toUpperCase());
        returnTypeStack.push(returnType);
        program.setType(Type.kind2type(program.getTypeToken().kind()));
        symbolTable.enterScope();
        for (NameDef param : program.getParams()) {
            param.visit(this, arg);
        }
        program.getBlock().visit(this, arg);
        symbolTable.leaveScope();
        returnTypeStack.pop();
        return program.getType();
    }


    @Override
    public Object visitBlock(Block block, Object arg) throws TypeCheckException, PLCCompilerException {
//        System.out.println("Entering scope");
        symbolTable.enterScope();
        for (Block.BlockElem elem : block.getElems()) {
            elem.visit(this, arg);
        }
//        System.out.println("Leaving scope");
        symbolTable.leaveScope();
        return null;
    }


    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws TypeCheckException {
        Type nameDefType = nameDef.getType();
        if (nameDef.getDimension() != null) {
            if (nameDefType != Type.IMAGE) {
                throw new TypeCheckException("not valid type for NameDef with Dimension: " + nameDefType + ". Expected IMAGE.");
            }
        } else {
            if (nameDefType != Type.INT && nameDefType != Type.BOOLEAN &&
                    nameDefType != Type.STRING && nameDefType != Type.PIXEL &&
                    nameDefType != Type.IMAGE) {
                throw new TypeCheckException("not valid type for NameDef: " + nameDefType);
            }
        }
        symbolTable.insert(nameDef);
        return nameDef;
    }


    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException, TypeCheckException {
        Expr initializer = declaration.getInitializer();
        Type initType = null;
        if (initializer != null) {
            initType = (Type) initializer.visit(this, arg);
        }

        NameDef nameDef = declaration.getNameDef();
        nameDef.visit(this, arg);
        Type declaredType = nameDef.getType();

        Dimension dimension = nameDef.getDimension();
        if (dimension != null) {
            dimension.visit(this, arg);
        }
        if (initializer != null) {
            if (initializer instanceof PostfixExpr) {
                PostfixExpr postfixExpr = (PostfixExpr) initializer;
                if (postfixExpr.channel() != null) {
                    initType = Type.INT;
                }
            }
            if (initType != declaredType && !(initType == Type.STRING && declaredType == Type.IMAGE)) {
                throw new TypeCheckException("type mismatch in declaration: expected " + declaredType + ", found " + initType);
            }
        }
        symbolTable.insert(nameDef);
        return declaredType;
    }


    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type guardType = (Type) conditionalExpr.getGuardExpr().visit(this, arg);
        if (guardType != Type.BOOLEAN) {
            throw new TypeCheckException("Guard expression in a conditional must evaluate to a BOOLEAN type");
        }

        Type trueType = (Type) conditionalExpr.getTrueExpr().visit(this, arg);
        Type falseType = (Type) conditionalExpr.getFalseExpr().visit(this, arg);

        if (trueType != falseType) {
            if ((trueType == Type.PIXEL && falseType == Type.INT) || (trueType == Type.INT && falseType == Type.PIXEL)) {
                Type coercedType = Type.PIXEL;
                conditionalExpr.setType(coercedType);
                return coercedType;
            } else {
                throw new TypeCheckException("The types of the true and false expressions in a conditional must be the same or coercible");
            }
        }

        conditionalExpr.setType(trueType);
        return trueType;
    }



    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr left = binaryExpr.getLeftExpr();
        Expr right = binaryExpr.getRightExpr();

        Type leftType = (Type) left.visit(this, arg);
        Type rightType = (Type) right.visit(this, arg);

        switch (binaryExpr.getOpKind()) {
            case PLUS:
                if (leftType == rightType) {
                    binaryExpr.setType(leftType);
                    return leftType;
                }
                break;
            case MINUS:
            case TIMES:
            case DIV:
                if ((leftType == Type.INT || leftType == Type.PIXEL || leftType == Type.IMAGE) && leftType == rightType) {
                    binaryExpr.setType(leftType);
                    return leftType;
                } else if ((leftType == Type.PIXEL || leftType == Type.IMAGE) && rightType == Type.INT) {
                    binaryExpr.setType(leftType);
                    return leftType;
                }
                break;
            case BITAND:
            case BITOR:
                if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    binaryExpr.setType(Type.PIXEL);
                    return Type.PIXEL;
                }
                break;
            case AND:
            case OR:
                if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                    binaryExpr.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                }
                break;
            case LT:
            case GT:
            case LE:
            case GE:
                if (leftType == Type.INT && rightType == Type.INT) {
                    binaryExpr.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                }
                break;
            case EQ:
                if (leftType == rightType) {
                    binaryExpr.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                }
                break;
            case EXP:
                if (leftType == Type.INT && rightType == Type.INT) {
                    binaryExpr.setType(Type.INT);
                    return Type.INT;
                } else if (leftType == Type.PIXEL && rightType == Type.INT) {
                    binaryExpr.setType(Type.PIXEL);
                    return Type.PIXEL;
                }
                break;
            case MOD:
                if (leftType == Type.INT && rightType == Type.INT) {
                    binaryExpr.setType(Type.INT);
                    return Type.INT;
                }
                break;
            default:
                throw new TypeCheckException("unsupported binary operation: " + binaryExpr.getOpKind());
        }

        throw new TypeCheckException("mismatched types in binary expression: " + leftType + ", " + rightType);
    }


    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type operandType = (Type) unaryExpr.getExpr().visit(this, arg);
        Type resultType;
        switch (unaryExpr.getOp()) {
            case MINUS:
                if (operandType == Type.INT) {
                    resultType = Type.INT;
                } else {
                    throw new TypeCheckException("not valid op type for unary negation");
                }
                break;
            case BANG:
                if (operandType == Type.BOOLEAN) {
                    resultType = Type.BOOLEAN;
                } else {
                    throw new TypeCheckException("not valid op type for unary NOT");
                }
                break;
            case RES_width:
            case RES_height:
                if (operandType == Type.IMAGE) {
                    resultType = Type.INT;
                } else {
                    throw new TypeCheckException("not valid op type for unary " + unaryExpr.getOp());
                }
                break;
            default:
                throw new TypeCheckException("unrecognized unary operator");
        }
        unaryExpr.setType(resultType);
        return resultType;
    }


    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type primaryType = (Type) postfixExpr.primary().visit(this, arg);
        PixelSelector pixelSelector = postfixExpr.pixel();
        ChannelSelector channelSelector = postfixExpr.channel();

        if (primaryType == Type.IMAGE && pixelSelector != null) {
            postfixExpr.setType(Type.PIXEL);
            pixelSelector.visit(this, arg);
            if (channelSelector != null) {
                postfixExpr.setType(Type.INT);
                channelSelector.visit(this, arg);
            }
        } else if (primaryType == Type.PIXEL && pixelSelector == null && channelSelector != null) {
            postfixExpr.setType(Type.INT);
            channelSelector.visit(this, arg);
        } else if (primaryType == Type.IMAGE && channelSelector != null) {
            postfixExpr.setType(Type.IMAGE);
            channelSelector.visit(this, arg);
        } else {
            postfixExpr.setType(primaryType);
        }

        return postfixExpr.getType();
    }


    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type type = Type.STRING;
        stringLitExpr.setType(Type.STRING);
        return type;
    }


    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type type = Type.INT;
        numLitExpr.setType(type);
        return type;
    }


    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        String name = identExpr.getName();
        NameDef nameDef = symbolTable.lookup(name);

        if (nameDef == null) {

            if (IN_LVALUE_CONTEXT.equals(arg)) {
                SyntheticNameDef syntheticNameDef = new SyntheticNameDef(name);
                symbolTable.insert(syntheticNameDef);
                identExpr.setType(Type.INT);
                return Type.INT;
            } else {
                throw new TypeCheckException("variable " + name + " not declared in current scope.");
            }
        }

        identExpr.setType(nameDef.getType());
        return nameDef.getType();
    }


    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        booleanLitExpr.setType(Type.BOOLEAN);
        return Type.BOOLEAN;
    }


    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws TypeCheckException, PLCCompilerException {
        if ("Z".equals(constExpr.getName())) {
            constExpr.setType(Type.INT);
        } else {
            constExpr.setType(Type.PIXEL);
        }
        return constExpr.getType();
    }




    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws TypeCheckException, PLCCompilerException {
        Kind colorKind = channelSelector.color();
        if (colorKind != Kind.RES_red && colorKind != Kind.RES_green && colorKind != Kind.RES_blue) {
            throw new TypeCheckException("Not valid color channel: " + colorKind);
        }
        return Type.INT;
    }



    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr xExpr = pixelSelector.xExpr();
        Expr yExpr = pixelSelector.yExpr();

        Type xType = (Type) xExpr.visit(this, arg);
        Type yType = (Type) yExpr.visit(this, arg);

        if (xType != Type.INT || yType != Type.INT) {
            throw new TypeCheckException("PixelSelector coordinates should be of type INT.");
        }

        if (IN_LVALUE_CONTEXT.equals(arg)) {
            if (xExpr instanceof IdentExpr) {
                IdentExpr xIdentExpr = (IdentExpr) xExpr;
                if (symbolTable.lookup(xIdentExpr.getName()) == null) {
                    SyntheticNameDef syntheticNameDef = new SyntheticNameDef(xIdentExpr.getName());
                    symbolTable.enterScope();
                    symbolTable.insert(syntheticNameDef);
                    symbolTable.leaveScope();
                }
            }

            if (yExpr instanceof IdentExpr) {
                IdentExpr yIdentExpr = (IdentExpr) yExpr;
                if (symbolTable.lookup(yIdentExpr.getName()) == null) {
                    SyntheticNameDef syntheticNameDef = new SyntheticNameDef(yIdentExpr.getName());
                    symbolTable.enterScope();
                    symbolTable.insert(syntheticNameDef);
                    symbolTable.leaveScope();
                }
            }
        }

        return Type.IMAGE;
    }

    /* ======================= MOKSH ======================= */


    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expr, Object arg) throws TypeCheckException, PLCCompilerException {
        checkAndUpdateExprType(expr.getRed(), arg);
        checkAndUpdateExprType(expr.getGreen(), arg);
        checkAndUpdateExprType(expr.getBlue(), arg);

        expr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    private void checkAndUpdateExprType(Expr expr, Object arg) throws TypeCheckException, PLCCompilerException {
        expr.visit(this, arg);
        if (expr instanceof PostfixExpr) {
            PostfixExpr postfixExpr = (PostfixExpr) expr;
            if (postfixExpr.channel() != null) {
                expr.setType(Type.INT);
            }
        }
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws TypeCheckException, PLCCompilerException {
        Type widthType = (Type) dimension.getWidth().visit(this, arg);
        Type heightType = (Type) dimension.getHeight().visit(this, arg);

        if (widthType != Type.INT || heightType != Type.INT) {
            throw new TypeCheckException("width and height in dimension must be of type INT");
        }

        return Type.INT;
    }


    @Override
    public Object visitLValue(LValue lValue, Object arg) throws TypeCheckException, PLCCompilerException {
        NameDef nameDef = lValue.getNameDef();

        if (nameDef == null) {
            nameDef = symbolTable.lookup(lValue.getName());
            if (nameDef == null) {
                throw new TypeCheckException("LValue refers to an undefined name: " + lValue.getName());
            }
        }

        Type varType = nameDef.getType();

        PixelSelector pixelSelector = lValue.getPixelSelector();
        ChannelSelector channelSelector = lValue.getChannelSelector();

        if (pixelSelector != null && varType != Type.IMAGE) {
            throw new TypeCheckException("PixelSelector present, but LValue varType is not IMAGE. found: " + varType);
        }

        if (channelSelector != null && (varType != Type.PIXEL && varType != Type.IMAGE)) {
            throw new TypeCheckException("ChannelSelector present, but LValue varType is not PIXEL or IMAGE. found: " + varType);
        }

        if (pixelSelector == null && channelSelector == null) {
            lValue.setType(varType);
        } else if (varType == Type.IMAGE && pixelSelector != null && channelSelector == null) {
            lValue.setType(Type.PIXEL);
            if (arg instanceof LValue) {
                symbolTable.enterScope();
                symbolTable.insert(new SyntheticNameDef("x"));
                symbolTable.insert(new SyntheticNameDef("y"));
                pixelSelector.visit(this, arg);
                symbolTable.leaveScope();
            } else {
                pixelSelector.visit(this, arg);
            }
        } else if (varType == Type.IMAGE && pixelSelector != null && channelSelector != null) {
            lValue.setType(Type.INT);
            pixelSelector.visit(this, arg);
            channelSelector.visit(this, arg);
        } else if (varType == Type.IMAGE && pixelSelector == null && channelSelector != null) {
            lValue.setType(Type.INT);
            channelSelector.visit(this, arg);
        } else if (varType == Type.PIXEL && pixelSelector == null && channelSelector != null) {
            lValue.setType(Type.INT);
            channelSelector.visit(this, arg);
        } else {
            throw new TypeCheckException("Invalid combination in LValue.");
        }

        return lValue.getType();
    }



    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        LValue lValue = assignmentStatement.getlValue();
        Type lValueType;
        symbolTable.enterScope();

        if (lValue.getPixelSelector() != null) {
            lValueType = (Type) lValue.visit(this, IN_LVALUE_CONTEXT);
        } else {
            lValueType = (Type) lValue.visit(this, arg);
        }

        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);
        symbolTable.leaveScope();

        if (lValueType == Type.PIXEL) {
            if (!(exprType == Type.PIXEL || exprType == Type.INT)) {
                throw new TypeCheckException("Incompatible types in PIXEL assignment. LValue type: " + lValueType + ", Expr type: " + exprType);
            }
        } else if (lValueType == Type.IMAGE) {
            if (!(exprType == Type.IMAGE || exprType == Type.PIXEL || exprType == Type.STRING)) {
                throw new TypeCheckException("Incompatible types in IMAGE assignment. LValue type: " + lValueType + ", Expr type: " + exprType);
            }
        } else if (lValueType != exprType) {
            throw new TypeCheckException("Type mismatch in assignment. LValue type: " + lValueType + ", Expr type: " + exprType);
        }

        return null;
    }



    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr expr = writeStatement.getExpr();
        Type exprType = (Type) expr.visit(this, arg);
        if (exprType == null) {
            throw new TypeCheckException("type of the expression in WriteStatement has not been found yet.");
        }
        return exprType;
    }


    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        symbolTable.enterScope();
        try {
            for (GuardedBlock gBlock : doStatement.getGuardedBlocks()) {
                Type guardType = (Type) gBlock.getGuard().visit(this, arg);
                if (guardType != Type.BOOLEAN) {
                    throw new TypeCheckException("guard expression in DoStatement must be of type BOOLEAN");
                }
                gBlock.getBlock().visit(this, arg);
            }
        } finally {
            symbolTable.leaveScope();
        }
        return doStatement;
    }


    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        for (GuardedBlock gBlock : ifStatement.getGuardedBlocks()) {
            Type guardType = (Type) gBlock.getGuard().visit(this, arg);
            if (guardType != Type.BOOLEAN) {
                throw new TypeCheckException("guard expression in IfStatement's GuardedBlock must be of type BOOLEAN");
            }
        }
        return ifStatement;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws TypeCheckException, PLCCompilerException {
        Type guardType = (Type) guardedBlock.getGuard().visit(this, arg);
        if (guardType != Type.BOOLEAN) {
            throw new TypeCheckException("guard expression in GuardedBlock must be of type BOOLEAN");
        }
        return guardType;
    }


    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr returnedExpr = returnStatement.getE();
        Type returnedType = (Type) returnedExpr.visit(this, arg);

        if (returnedExpr instanceof PostfixExpr) {
            PostfixExpr postfixExpr = (PostfixExpr) returnedExpr;
            PixelSelector pixelSelector = postfixExpr.pixel();
            ChannelSelector channelSelector = postfixExpr.channel();

            if (postfixExpr.primary().getType() == Type.IMAGE && channelSelector != null) {
                returnedType = Type.IMAGE;
            } else if (pixelSelector != null && channelSelector == null) {
                returnedType = Type.PIXEL;
            } else if (channelSelector != null) {
                returnedType = Type.INT;
            }
        }

        if (returnTypeStack.isEmpty()) {
            throw new TypeCheckException("unexpected :( return statement outside of function or method scope.");
        }
        Type expectedReturnType = returnTypeStack.peek();
        if (returnedType != expectedReturnType) {
            throw new TypeCheckException("mismatched return type :(. Expected " + expectedReturnType + " but found " + returnedType);
        }

        return returnedType;
    }


    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws TypeCheckException, PLCCompilerException {
        statementBlock.getBlock().visit(this, arg);
        return statementBlock;
    }


}

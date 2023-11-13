package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

class SymbolTable {
    private final HashMap<String, LinkedList<Symbol>> table;
    private final Stack<Integer> scopeStack;
    private int currentScope;
    private int nextScope;



    public SymbolTable() {
        table = new HashMap<>();
        scopeStack = new Stack<>();
        currentScope = 0;
        nextScope = 1;
        scopeStack.push(currentScope);
    }

    public void enterScope() {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    public void leaveScope() {
        if (!scopeStack.isEmpty()) {
            int oldScope = scopeStack.pop();
            for (LinkedList<Symbol> list : table.values()) {
                while (!list.isEmpty() && list.getFirst().getSerialNumber() == oldScope) {
                    list.removeFirst();
                }
            }
            if (!scopeStack.isEmpty()) {
                currentScope = scopeStack.peek();
            }
        }
    }


    public void insert(NameDef nameDef) throws TypeCheckException {
//        System.out.println("Inserting to SymbolTable: " + nameDef.getName() + " in scope " + currentScope);
        String name = nameDef.getName();
        LinkedList<Symbol> list = table.get(name);
        if (list == null) {
            list = new LinkedList<>();
            table.put(name, list);
        } else {
            for (Symbol symbol : list) {
                if (symbol.getSerialNumber() == currentScope) {
                    if (symbol.getNameDef() == nameDef) {
                        // same declaration is being inserted again.
                        return;
                    } else {
                        throw new TypeCheckException("Name already defined in the current scope: " + name);
                    }
                }
            }
        }
        Symbol symbol = new Symbol(name, currentScope, nameDef);
        list.addFirst(symbol);
    }


    public NameDef lookup(String name) {
        LinkedList<Symbol> list = table.get(name);
        if (list != null) {
            for (int i = scopeStack.size() - 1; i >= 0; i--) {
                int scope = scopeStack.get(i);
                for (Symbol symbol : list) {
                    if (symbol.getSerialNumber() == scope) {
                        return symbol.getNameDef();
                    }
                }
            }
        }
        return null;
    }

    public boolean isDefinedInCurrentScope(String name) {
        LinkedList<Symbol> list = table.get(name);
        if (list != null && !list.isEmpty()) {
            return list.getFirst().getSerialNumber() == currentScope;
        }
        return false;
    }


}

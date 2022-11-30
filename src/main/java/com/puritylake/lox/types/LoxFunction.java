package com.puritylake.lox.types;

import com.puritylake.lox.Environment;
import com.puritylake.lox.exceptions.ControlFlowException;
import com.puritylake.lox.parsing.Interpreter;
import com.puritylake.lox.parsing.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) throws Exception {
        Environment environment = new Environment(interpreter.getGlobals());
        for (int i = 0; i < declaration.params.size(); ++i) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i), true);
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ControlFlowException ignored) {

        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme() + ">";
    }
}

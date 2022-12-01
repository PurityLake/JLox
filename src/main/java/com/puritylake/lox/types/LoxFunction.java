package com.puritylake.lox.types;

import com.puritylake.lox.parsing.Environment;
import com.puritylake.lox.exceptions.ControlFlowChange;
import com.puritylake.lox.parsing.Interpreter;
import com.puritylake.lox.parsing.Return;
import com.puritylake.lox.parsing.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance, true);
        return new LoxFunction(declaration, environment);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) throws Exception {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); ++i) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i), true);
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ControlFlowChange ignored) {

        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        if (declaration.name != null) {
            return "<fn " + declaration.name.lexeme() + ">";
        } else {
            return "<anonymous fn>";
        }
    }
}

package com.puritylake.lox.types;

import com.puritylake.lox.Environment;
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

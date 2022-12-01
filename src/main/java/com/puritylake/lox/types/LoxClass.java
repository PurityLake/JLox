package com.puritylake.lox.types;

import com.puritylake.lox.parsing.Interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    public final String name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) throws Exception {
        return new LoxInstance(this);
    }
}

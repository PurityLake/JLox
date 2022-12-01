package com.puritylake.lox.types;

import com.puritylake.lox.parsing.Interpreter;

import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;

    public LoxClass(String name) {
        this.name = name;
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

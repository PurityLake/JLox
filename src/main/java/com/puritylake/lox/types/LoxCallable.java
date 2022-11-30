package com.puritylake.lox.types;

import com.puritylake.lox.parsing.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments) throws Exception;
}

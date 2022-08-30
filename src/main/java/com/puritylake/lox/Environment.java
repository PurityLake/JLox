package com.puritylake.lox;

import com.puritylake.lox.parsing.RuntimeError;
import com.puritylake.lox.parsing.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        throw new RuntimeError(name,
                "Undefined variable '" +name.lexeme() + "'.");
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }
}

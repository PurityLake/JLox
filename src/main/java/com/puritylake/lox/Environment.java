package com.puritylake.lox;

import com.puritylake.lox.parsing.RuntimeError;
import com.puritylake.lox.parsing.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private record EnvironmentEntry(Object value, boolean initialized) { }
    private final Environment enclosing;
    private final Map<String, EnvironmentEntry> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment env) {
        enclosing = env;
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            EnvironmentEntry entry =  values.get(name.lexeme());
            if (entry.initialized()) {
                return entry.value();
            }
            throw new RuntimeError(name,
                    "Variable '" + name.lexeme() + "' may not have been initialized.");
        }

        if (enclosing != null) {
            Object obj = enclosing.get(name);
            if (obj != null) {
                return obj;
            }
        }

        throw new RuntimeError(name,
                "Undefined variable '" +name.lexeme() + "'.");
    }

    public void define(String name, Object value, boolean initialized) {
        values.put(name, new EnvironmentEntry(value, initialized));
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), new EnvironmentEntry(value, true));
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme() + "'.");
    }
}

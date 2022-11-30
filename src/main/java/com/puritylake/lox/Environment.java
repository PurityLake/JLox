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
                "Undefined variable '" + name.lexeme() + "'.");
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name).value();
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme(), new EnvironmentEntry(value, true));
    }

    private Environment ancestor(int distance) {
        Environment env = this;
        for (int i = 0; i < distance; ++i) {
            env = env.enclosing;
        }

        return env;
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

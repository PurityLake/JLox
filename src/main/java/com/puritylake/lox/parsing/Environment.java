package com.puritylake.lox.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    static class EnvironmentEntry{
        public Object value;
        public boolean initialized;

        public EnvironmentEntry(Object value, boolean initialized) {
            this.value = value;
            this.initialized = initialized;
        }
    }
    private final Environment enclosing;
    private final Map<String, EnvironmentEntry> namedValues = new HashMap<>();
    private final List<EnvironmentEntry> indexedValues = new ArrayList<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment env) {
        enclosing = env;
    }

    public Object get(String name) {
        return namedValues.get(name).value;
    }

    public Object tryGet(String name) {
        Environment env = this;
        while (env != null) {
            if (env.namedValues.containsKey(name)) {
                return env.namedValues.get(name).value;
            }
            env = env.enclosing;
        }
        return null;
    }

    public Object getAt(int distance, int idx) {
        return ancestor(distance).indexedValues.get(idx).value;
    }

    public void assignAt(int distance, int idx, Object value) {
        EnvironmentEntry entry = ancestor(distance).indexedValues.get(idx);
        entry.value = value;
        entry.initialized = true;
    }

    private Environment ancestor(int distance) {
        Environment env = this;
        for (int i = 0; i < distance; ++i) {
            if (env.enclosing == null) {
                return env;
            }
            env = env.enclosing;
        }

        return env;
    }

    public void define(String name, Object value, boolean initialized) {
        namedValues.put(name, new EnvironmentEntry(value, initialized));
    }

    public void defineIdx(Object value, boolean initialized) {
        indexedValues.add(new EnvironmentEntry(value, initialized));
    }

    public void assign(Token name, Object value) {
        if (namedValues.containsKey(name.lexeme())) {
            EnvironmentEntry entry = namedValues.get(name.lexeme());
            entry.value = value;
            entry.initialized = true;
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

package com.puritylake.lox.types;

public class LoxInstance {
    private LoxClass klass;

    public LoxInstance(LoxClass  klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}

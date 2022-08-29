package com.puritylake.lox.parsing;

public record Token(TokenType type, String lexeme, Object literal, int line) {
    @Override
    public String toString() {
        return type + " " + lexeme + "  " + literal;
    }
}
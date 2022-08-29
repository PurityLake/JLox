package com.puritylake.lox.parsing;

public record Token(TokenType type, String lexeme, Object literal, int line) {
    @Override
    public String toString() {
        return type + " " + lexeme + "  " + literal;
    }

    public boolean isBinaryOp() {
        return switch (type) {
            case PLUS, MINUS, SLASH, STAR, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL ->
                    true;
            default -> false;
        };
    }

    public boolean isUnaryOp() {
        return switch (type) {
            case MINUS, BANG ->
                true;
            default ->
                false;
        };
    }
}

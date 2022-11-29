package com.puritylake.lox.exceptions;

public class ControlFlowException extends Exception {
    public final boolean isBreak;
    public ControlFlowException(boolean brk) {
        isBreak = brk;
    }
}

package com.puritylake.lox.exceptions;

public class ControlFlowChange extends Exception {
    public final boolean isBreak;
    public ControlFlowChange(boolean brk) {
        isBreak = brk;
    }
}

package com.puritylake.lox.parsing;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) throws Exception {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) throws Exception {
        return null;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) throws Exception {
        return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) throws Exception {
        return parenthesize("call", expr.callee);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) throws Exception {
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) throws Exception {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) throws Exception {
        return parenthesize("logical", expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) throws Exception {
        return null;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) throws Exception {
        return parenthesize(expr.operator.lexeme(), expr.right);
    }

    @Override
    public String visitCommaGroupExpr(Expr.CommaGroup expr) throws Exception {
        return parenthesize("comma", expr.left, expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) throws Exception {
        return parenthesize("ternary", expr.cond, expr.trueVal, expr.falseVal);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public String visitAnonFunctionExpr(Expr.AnonFunction expr) throws Exception {
        return null;
    }

    private String parenthesize(String name, Expr... exprs) throws Exception {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}

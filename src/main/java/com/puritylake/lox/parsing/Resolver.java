package com.puritylake.lox.parsing;

import com.puritylake.lox.Lox;
import com.puritylake.lox.exceptions.ControlFlowChange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private enum FunctionType {
        NONE, FUNCTION
    }

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    public void resolve(List<Stmt> statements) throws Exception {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) throws Exception {
        statement.accept(this);
    }

    private void resolve(Expr expr) throws Exception {
        expr.accept(this);
    }

    private void endScope() {
        scopes.pop();
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme(), false);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expr, scopes.size() -1 - i);
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) throws Exception {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) throws Exception {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) throws Exception {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) throws Exception {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) throws Exception {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) throws Exception {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) throws Exception {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) throws Exception {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCommaGroupExpr(Expr.CommaGroup expr) throws Exception {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) throws Exception {
        resolve(expr.cond);
        resolve(expr.trueVal);
        resolve(expr.falseVal);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) throws Exception {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme()) == Boolean.FALSE) {
            Lox.error(expr.name, "Can't read local variable in its own intializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAnonFunctionExpr(Expr.AnonFunction expr) throws Exception {
        resolveFunction((Stmt.Function)expr.func, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) throws Exception {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) throws Exception {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) throws Exception {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) throws Exception {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) throws Exception {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) throws Exception {
        if (stmt.value != null)  {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) throws Exception {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) throws Exception {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) throws Exception {
        beginScope();
        resolve(stmt.init);
        resolve(stmt.cond);
        resolve(stmt.post);
        resolve(stmt.body);
        endScope();
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) throws ControlFlowChange {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) throws ControlFlowChange {
        return null;
    }
}

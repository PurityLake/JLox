package com.puritylake.lox.parsing;

import com.puritylake.lox.Lox;

import java.util.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private enum FunctionType {
        NONE, FUNCTION, METHOD
    }

    private static class ResolverEntry {
        public Boolean defined;
        public Boolean used;
        public final Token token;

        ResolverEntry(Token token, Boolean defined, Boolean used) {
            this.token = token;
            this.defined = defined;
            this.used = used;
        }
    }

    private static class StackEntry {
        public String name;
        public ResolverEntry entry;

        StackEntry(String name, ResolverEntry entry) {
            this.name = name;
            this.entry = entry;
        }
    }

    private List<StackEntry> lastPoppedScope = null;

    private final Stack<List<StackEntry>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    private void beginScope() {
        scopes.push(new ArrayList<>());
    }

    public void resolve(List<Stmt> statements) throws Exception {
        beginScope();
        for (Stmt statement : statements) {
            resolve(statement);
        }
        endScope(null);
    }

    private void resolveBlock(List<Stmt> statements) throws Exception {
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

    private void endScope(Token ignore) {
        lastPoppedScope = scopes.pop();
        checkUnusedLocals(ignore);
    }

    private void checkUnusedLocals(Token ignore) {
        if (lastPoppedScope != null) {
            List<StackEntry> scope = lastPoppedScope;
            for (StackEntry se : scope) {
                if (se.entry.token != ignore && !se.entry.used) {
                    System.err.printf("[line %d] local variable '%s' is unused.\n", se.entry.token.line(), se.entry.token.lexeme());
                }
            }
        }
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        StackEntry entry = getByName(name.lexeme());
        if (entry != null) {
            entry.entry.defined = true;
        }
    }

    private StackEntry getByName(String name) {
        List<StackEntry> top = scopes.peek();
        for (StackEntry se : top) {
            if (se.entry.token.lexeme().equals(name)) {
                return se;
            }
        }
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        if (getByName(name.lexeme()) != null) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }
        List<StackEntry> scope = scopes.peek();
        scope.add(new StackEntry(name.lexeme(), new ResolverEntry(name, false, false)));
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            List<StackEntry> scope = scopes.get(i);
            for (int idx = 0; idx < scope.size(); ++idx) {
                StackEntry se = scope.get(idx);
                if (se.name.equals(name.lexeme())) {
                    se.entry.used = true;
                    ((Expr.Variable)expr).idx = idx;
                    ((Expr.Variable)expr).depth = scopes.size() - 1 - i;
                    return;
                }
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
        resolveBlock(function.body);
        endScope(function.name);
        currentFunction = enclosingFunction;
    }

    private void resolveClass(Stmt.Class klass) throws Exception {
        beginScope();
        for (Stmt.Function func :  klass.methods) {
            resolveFunction(func, FunctionType.METHOD);
        }
        endScope(klass.name);
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
    public Void visitGetExpr(Expr.Get expr) throws Exception {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) throws Exception {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr)  {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) throws Exception {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) throws Exception {
        resolve(expr.value);
        resolve(expr.object);
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
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty()) {
            StackEntry se = getByName(expr.name.lexeme());
            if (se != null) {
                if (se.entry != null && se.entry.defined == Boolean.FALSE) {
                    Lox.error(expr.name, "Can't read local variable in its own initializer.");
                }
            }
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
        resolveBlock(stmt.statements);
        endScope(null);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) throws Exception {
        declare(stmt.name);
        define(stmt.name);

        resolveClass(stmt);

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
        endScope(null);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }
}

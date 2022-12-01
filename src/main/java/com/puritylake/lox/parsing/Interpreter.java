package com.puritylake.lox.parsing;

import com.puritylake.lox.Lox;
import com.puritylake.lox.types.LoxCallable;
import com.puritylake.lox.exceptions.ControlFlowChange;
import com.puritylake.lox.types.LoxClass;
import com.puritylake.lox.types.LoxFunction;
import com.puritylake.lox.types.LoxInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.00;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        }, true);
    }

    public void interpret(List<Stmt> statements) throws Exception {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) throws Exception {
        stmt.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr) throws Exception {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    public void executeBlock(List<Stmt> statements, Environment environment) throws Exception {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) throws Exception {
        Object value = evaluate(expr.value);

        Expr.Variable var = (Expr.Variable)expr.var;
        if (var.depth != -1) {
            environment.assignAt(var.depth, var.idx, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) throws Exception {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                double r = (double) right;
                if (r == 0) {
                    throw new RuntimeError(expr.operator,
                            "Cannot divide by zero.");
                }
                return (double) left / (double) right;
            }
            case STAR  -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case PLUS  -> {
                if (left instanceof Double) {
                    if (right instanceof Double) {
                        return (double) left + (double) right;
                    } else if (right instanceof String) {
                        return stringify(left) + right;
                    }
                }
                if (left instanceof String) {
                    return left + stringify(right);
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be convertible.");
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) throws Exception {
        Object callee = evaluate(expr.callee);

        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr argument: expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() +
                    " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) throws Exception {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance instance) {
            return instance.get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) throws Exception {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) throws Exception {
        Object left = evaluate(expr.left);

        if (expr.operator.type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left))return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) throws Exception {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance instance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        instance.set(expr.name, value);
        return null;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) throws Exception {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) throws Exception {
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }

        return null; // unreachable
    }

    @Override
    public Object visitCommaGroupExpr(Expr.CommaGroup expr) throws Exception {
        evaluate(expr.left);
        evaluate(expr.right);
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) throws Exception {
        if (isTruthy(evaluate(expr.cond))) {
            return evaluate(expr.trueVal);
        }
        return evaluate(expr.falseVal);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        if (expr instanceof Expr.Variable var) {
            var = (Expr.Variable) expr;
            if (var.depth != -1) {
                return environment.getAt(var.depth, ((Expr.Variable) expr).idx);
            }
        }
        Object obj = environment.tryGet(name.lexeme());
        if (obj != null) {
            return obj;
        }
        return globals.get(name.lexeme());
    }

    @Override
    public Object visitAnonFunctionExpr(Expr.AnonFunction expr) {
        return new LoxFunction((Stmt.Function)expr.func, environment);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) throws Exception {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) throws Exception {
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment);
            methods.put(method.name.lexeme(), function);
        }
        LoxClass klass = new LoxClass(stmt.name.lexeme(), methods);

        environment.defineIdx(klass,true);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) throws Exception {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme(), function, true);
        environment.defineIdx(function, true);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) throws Exception {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) throws Exception {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) throws Exception {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) throws Exception {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme(), value, stmt.initialized);
        environment.defineIdx(value, stmt.initialized);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) throws Exception {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (ControlFlowChange cfe) {
                if (cfe.isBreak) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) throws Exception {
        boolean hasInit = stmt.init != null;
        boolean hasCond = stmt.cond != null;
        boolean hasPost = stmt.post != null;

        if (hasInit) {
            execute(stmt.init);
        }

        while (!hasCond || isTruthy(evaluate(stmt.cond))) {
            try {
                execute(stmt.body);
            } catch (ControlFlowChange cfe) {
                if (cfe.isBreak) {
                    break;
                }
            }
            if (hasPost) {
                evaluate(stmt.post);
            }
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) throws ControlFlowChange {
        throw new ControlFlowChange(true);
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) throws ControlFlowChange {
        throw new ControlFlowChange(false);
    }
}

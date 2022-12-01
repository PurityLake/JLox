// ###################################################

// DO NOT EDIT THIS FILE IT IS GENERATED AUTOMATICALLY

// ###################################################

package com.puritylake.lox.parsing;

import com.puritylake.lox.exceptions.*;

import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitBlockStmt(Block stmt) throws Exception;
        R visitExpressionStmt(Expression stmt) throws Exception;
        R visitFunctionStmt(Function stmt) throws Exception;
        R visitIfStmt(If stmt) throws Exception;
        R visitPrintStmt(Print stmt) throws Exception;
        R visitReturnStmt(Return stmt) throws Exception;
        R visitVarStmt(Var stmt) throws Exception;
        R visitWhileStmt(While stmt) throws Exception;
        R visitForStmt(For stmt) throws Exception;
        R visitBreakStmt(Break stmt) throws ControlFlowChange;
        R visitContinueStmt(Continue stmt) throws ControlFlowChange;
    }
    public static class Block extends Stmt {
       public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitBlockStmt(this);
        }

        public final List<Stmt> statements;
    }
    public static class Expression extends Stmt {
       public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitExpressionStmt(this);
        }

        public final Expr expression;
    }
    public static class Function extends Stmt {
       public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitFunctionStmt(this);
        }

        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;
    }
    public static class If extends Stmt {
       public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitIfStmt(this);
        }

        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
    }
    public static class Print extends Stmt {
       public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitPrintStmt(this);
        }

        public final Expr expression;
    }
    public static class Return extends Stmt {
       public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitReturnStmt(this);
        }

        public final Token keyword;
        public final Expr value;
    }
    public static class Var extends Stmt {
       public Var(Token name, Expr initializer, boolean initialized) {
            this.name = name;
            this.initializer = initializer;
            this.initialized = initialized;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitVarStmt(this);
        }

        public final Token name;
        public final Expr initializer;
        public final boolean initialized;
    }
    public static class While extends Stmt {
       public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitWhileStmt(this);
        }

        public final Expr condition;
        public final Stmt body;
    }
    public static class For extends Stmt {
       public For(Stmt init, Expr cond, Expr post, Stmt body) {
            this.init = init;
            this.cond = cond;
            this.post = post;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws Exception {
            return visitor.visitForStmt(this);
        }

        public final Stmt init;
        public final Expr cond;
        public final Expr post;
        public final Stmt body;
    }
    public static class Break extends Stmt {
       public Break(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws ControlFlowChange {
            return visitor.visitBreakStmt(this);
        }

        public final Token name;
    }
    public static class Continue extends Stmt {
       public Continue(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) throws ControlFlowChange {
            return visitor.visitContinueStmt(this);
        }

        public final Token name;
    }

    public abstract <R> R accept(Visitor<R> visitor) throws Exception;
}

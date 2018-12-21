package JavaInterpreter.Milk;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  abstract <R> R accept(Visitor<R> visitor);
}

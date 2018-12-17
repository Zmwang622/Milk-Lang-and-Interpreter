package JavaInterpreter.Milk;

import java.util.List;

abstract class Expr {
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }
}

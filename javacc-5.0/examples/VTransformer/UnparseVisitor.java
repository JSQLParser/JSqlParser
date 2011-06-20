/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */


//package VTransformer;

import java.io.PrintStream;

public class UnparseVisitor implements JavaParserVisitor
{

  protected PrintStream out;


  public UnparseVisitor(PrintStream o)
  {
    out = o;
  }


  public Object print(SimpleNode node, Object data) {
    Token t1 = node.getFirstToken();
    Token t = new Token();
    t.next = t1;

    SimpleNode n;
    for (int ord = 0; ord < node.jjtGetNumChildren(); ord++) {
      n = (SimpleNode)node.jjtGetChild(ord);
      while (true) {
	t = t.next;
	if (t == n.getFirstToken()) break;
	print(t);
      }
      n.jjtAccept(this, data);
      t = n.getLastToken();
    }

    while (t != node.getLastToken()) {
      t = t.next;
      print(t);
    }
    return data;
  }


  protected void print(Token t) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        out.print(addUnicodeEscapes(tt.image));
        tt = tt.next;
      }
    }
    out.print(addUnicodeEscapes(t.image));
  }


  private String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if ((ch < 0x20 || ch > 0x7e) &&
	  ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
  	String s = "0000" + Integer.toString(ch, 16);
  	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
  }


  public Object visit(SimpleNode node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCompilationUnit node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPackageDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTImportDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTTypeDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnmodifiedClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassBody node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNestedClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassBodyDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclarationLookahead node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNestedInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInterfaceMemberDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFieldDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableDeclarator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableDeclaratorId node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArrayInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclarator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFormalParameters node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFormalParameter node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConstructorDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExplicitConstructorInvocation node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimitiveType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTResultType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTName node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNameList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAssignmentOperator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalAndExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInclusiveOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExclusiveOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAndExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTEqualityExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInstanceOfExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTRelationalExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTShiftExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAdditiveExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMultiplicativeExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnaryExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPreIncrementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPreDecrementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCastLookahead node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPostfixExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCastExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimaryExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimaryPrefix node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimarySuffix node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBooleanLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNullLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArguments node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArgumentList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAllocationExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArrayDimsAndInits node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLabeledStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBlock node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBlockStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLocalVariableDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTEmptyStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSwitchStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSwitchLabel node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTIfStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTWhileStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTDoStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForInit node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatementExpressionList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForUpdate node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBreakStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTContinueStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTReturnStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTThrowStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSynchronizedStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTTryStatement node, Object data)
  {
    return print(node, data);
  }

}

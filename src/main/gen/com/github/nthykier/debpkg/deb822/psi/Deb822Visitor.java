// This is a generated file. Not intended for manual editing.
package com.github.nthykier.debpkg.deb822.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class Deb822Visitor extends PsiElementVisitor {

  public void visitAllParagraphs(@NotNull Deb822AllParagraphs o) {
    visitPsiElement(o);
  }

  public void visitField(@NotNull Deb822Field o) {
    visitPsiElement(o);
  }

  public void visitFieldValuePair(@NotNull Deb822FieldValuePair o) {
    visitPsiElement(o);
  }

  public void visitParagraph(@NotNull Deb822Paragraph o) {
    visitPsiElement(o);
  }

  public void visitSubstvar(@NotNull Deb822Substvar o) {
    visitPsiElement(o);
  }

  public void visitValueParts(@NotNull Deb822ValueParts o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.psi.impl.light;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class LightMemberReference extends LightElement implements PsiJavaCodeReferenceElement {
  private final @NotNull PsiMember myRefMember;
  private final PsiSubstitutor mySubstitutor;

  private LightReferenceParameterList myParameterList;

  public LightMemberReference(@NotNull PsiManager manager, @NotNull PsiMember member, PsiSubstitutor substitutor) {
    super(manager, JavaLanguage.INSTANCE);
    myRefMember = member;

    mySubstitutor = substitutor;
  }

  @Override
  public @NotNull PsiElement resolve() {
      return myRefMember;
  }

  @Override
  public @NotNull JavaResolveResult advancedResolve(boolean incompleteCode){
    final PsiElement resolved = resolve();
    PsiSubstitutor substitutor = mySubstitutor;
    if (substitutor == null) {
        substitutor = PsiSubstitutor.EMPTY;
    }
    return new CandidateInfo(resolved, substitutor);
  }

  @Override
  public JavaResolveResult @NotNull [] multiResolve(boolean incompleteCode){
    final JavaResolveResult result = advancedResolve(incompleteCode);
    if(result != JavaResolveResult.EMPTY) return new JavaResolveResult[]{result};
    return JavaResolveResult.EMPTY_ARRAY;
  }

  @Override
  public void processVariants(@NotNull PsiScopeProcessor processor){
    throw new RuntimeException("Variants are not available for light references");
  }

  @Override
  public PsiElement getReferenceNameElement() {
    return null;
  }

  @Override
  public PsiReferenceParameterList getParameterList() {
    LightReferenceParameterList parameterList = myParameterList;
    if (parameterList == null) {
      myParameterList = parameterList = new LightReferenceParameterList(myManager, PsiTypeElement.EMPTY_ARRAY);
    }
    return parameterList;
  }

  @Override
  public String getQualifiedName() {
    final PsiClass containingClass = myRefMember.getContainingClass();
    if (containingClass != null) {
      final String qualifiedName = containingClass.getQualifiedName();
      if (qualifiedName != null) {
        return qualifiedName + '.' + myRefMember.getName();
      }
    }
    return myRefMember.getName();
  }

  @Override
  public String getReferenceName() {
    return getQualifiedName();
  }

  @Override
  public String getText() {
    return myRefMember.getName() + getParameterList().getText();
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @Override
  public @NotNull String getCanonicalText() {
    String name = getQualifiedName();
    if (name == null) return null;
    PsiType[] types = getTypeParameters();
    if (types.length == 0) return name;

    StringBuilder buf = new StringBuilder();
    buf.append(name);
    buf.append('<');
    for (int i = 0; i < types.length; i++) {
      if (i > 0) buf.append(',');
      buf.append(types[i].getCanonicalText());
    }
    buf.append('>');

    return buf.toString();
  }

  @Override
  public PsiElement copy() {
      return new LightMemberReference(myManager, myRefMember, mySubstitutor);
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    //TODO?
    throw new IncorrectOperationException();
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    //TODO?
    throw new IncorrectOperationException();
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitReferenceElement(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public String toString() {
    return "LightClassReference:" + myRefMember.getName();
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    return element instanceof PsiClass && element.getManager().areElementsEquivalent(resolve(), element);
  }

  @Override
  public Object @NotNull [] getVariants() {
    throw new RuntimeException("Variants are not available for light references");
  }

  @Override
  public boolean isSoft(){
    return false;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  @Override
  public @NotNull PsiElement getElement() {
    return this;
  }

  @Override
  public boolean isValid() {
    PsiReferenceParameterList parameterList = getParameterList();
    if (parameterList != null && !parameterList.isValid()) return false;
    return myRefMember.isValid();
  }

  @Override
  public PsiType @NotNull [] getTypeParameters() {
    PsiReferenceParameterList parameterList = getParameterList();
    return parameterList == null ? PsiType.EMPTY_ARRAY : parameterList.getTypeArguments();
  }

  @Override
  public PsiElement getQualifier() {
    return null;
  }

  @Override
  public boolean isQualified() {
    return false;
  }
}

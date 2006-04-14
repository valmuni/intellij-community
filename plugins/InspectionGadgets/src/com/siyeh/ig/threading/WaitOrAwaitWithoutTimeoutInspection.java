/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.threading;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class WaitOrAwaitWithoutTimeoutInspection extends ExpressionInspection {

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }


    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "wait.or.await.without.timeout.problem.descriptor");
    }
    public BaseInspectionVisitor buildVisitor() {
        return new WaitWithoutTimeoutVisitor();
    }

    private static class WaitWithoutTimeoutVisitor extends BaseInspectionVisitor {

        public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiReferenceExpression methodExpression = expression.getMethodExpression();
            final String methodName = methodExpression.getReferenceName();
            if (!"wait".equals(methodName) && !"await".equals(methodName)) {
                return;
            }
            final PsiExpressionList argList = expression.getArgumentList();
            final PsiExpression[] args = argList.getExpressions();
            final int numParams = args.length;
            if (numParams != 0) {
                return;
            }
            if("await".equals(methodName))
            {
                final PsiMethod method = expression.resolveMethod();
                if(method == null)
                {
                    return;
                }
                final PsiClass containingClass = method.getContainingClass();
                if(containingClass == null)
                {
                    return;
                }
                final String className = containingClass.getName();
                if(!"java.util.concurrent.locks.Condition".equals(className))
                {
                    return;
                }
            }
            registerMethodCallError(expression);
        }
    }

}

/*
 *    Copyright (C) 2016 Björn Quentin
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.mobilej.thinr.lint;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Lint detector to detect wrong usage of lambdas together with Thinr.
 */
public class ThinrDetector extends Detector implements Detector.JavaPsiScanner {

    public static final Issue ISSUE = Issue.create(
            "ThinrDontLeak",
            "Lambda referencing the outer scope will create a leak",
            "Lambdas used in Thinr are not allowed to access the outer scope.",
            Category.CORRECTNESS,
            6,
            Severity.ERROR,
            new Implementation(
                    ThinrDetector.class,
                    Scope.JAVA_FILE_SCOPE
            )
    );

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        List<Class<? extends PsiElement>> res = new ArrayList<>();
        res.add(PsiLambdaExpression.class);
        return res;
        //return Collections.singletonList(PsiLambdaExpression.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(@NonNull final JavaContext context) {
        return new JavaElementVisitor() {
            @Override
            public void visitLambdaExpression(PsiLambdaExpression expression) {

                if (!(expression.getParent() instanceof PsiExpressionList)) {
                    return;
                }

                PsiExpressionList exprList = (PsiExpressionList) expression.getParent();
                if (!(exprList.getParent() instanceof PsiMethodCallExpression)) {
                    return;
                }
                PsiMethodCallExpression call = (PsiMethodCallExpression) exprList.getParent();

                if (call.getType() == null) {
                    return;
                }

                String callType = call.getType().getCanonicalText();

                if (!callType.startsWith("de.mobilej.thinr.Thinr")) {
                    return;
                }

                markLeakSuspects(expression, expression, context);
            }
        };
    }

    private void markLeakSuspects(PsiElement element, PsiElement lambdaBody, @NonNull final JavaContext context) {
        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression ref = (PsiReferenceExpression) element;

            if (ref.getQualifierExpression() == null) {

                PsiElement res = ref.resolve();
                if (!(res instanceof PsiParameter)) {
                    if (!(res instanceof PsiClass)) {

                        boolean error = false;
                        if (res instanceof PsiLocalVariable) {
                            PsiLocalVariable lVar = (PsiLocalVariable) res;
                            if (!isParent(lambdaBody, lVar.getParent())) {
                                error = true;
                            }
                        }

                        if (res instanceof PsiField) {
                            error = true;
                        }

                        if (error) {
                            context.report(ISSUE, element, context.getNameLocation(element), "Possible leak");
                        }
                    }
                }
            }
        }

        for (PsiElement psiElement : element.getChildren()) {
            markLeakSuspects(psiElement, lambdaBody, context);
        }
    }

    private boolean isParent(PsiElement lambdaBody, PsiElement element) {
        if (element == lambdaBody) {
            return true;
        }

        if (element.getParent() != null) {
            if (element.getParent() == lambdaBody) {
                return true;
            } else {
                return isParent(lambdaBody, element.getParent());
            }
        }

        return false;
    }

}
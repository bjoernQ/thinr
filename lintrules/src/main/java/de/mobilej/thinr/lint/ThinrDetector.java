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
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;
import java.util.List;

/**
 * Lint detector to detect wrong usage of lambdas together with Thinr.
 */
public class ThinrDetector extends Detector implements Detector.ClassScanner {

    public static final Issue ISSUE = Issue.create(
            "ThinrDontLeak",
            "Lambda referencing the outer scope will create a leak",
            "Lambdas used in Thinr are not allowed to access the outer scope.",
            Category.CORRECTNESS,
            6,
            Severity.ERROR,
            new Implementation(
                    ThinrDetector.class,
                    EnumSet.of(Scope.CLASS_FILE)
            )
    );

    private static final String[] TO_CHECK = {
            "Lde/mobilej/thinr/ThinrFunctionOnMain;",
            "Lde/mobilej/thinr/ThinrFunctionInBackground;",
            "Lde/mobilej/thinr/ThinrFinalFunctionOnMain;",
            "Lde/mobilej/thinr/ThinrFinalFunctionInBackground;",
            "Lde/mobilej/thinr/ThinrOnCancelFunctionOnMain;"
    };

    @Override
    public void checkClass(@NonNull ClassContext context, @NonNull ClassNode classNode) {
        List methods = classNode.methods;
        for (Object methodObject : methods) {
            MethodNode method = (MethodNode) methodObject;

            InsnList inst = method.instructions;
            for (int i = 0; i < inst.size(); i++) {
                // CHECK LAMBDA CALLS
                if (inst.get(i) instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode invokeDynInsnNode = (InvokeDynamicInsnNode) inst.get(i);

                    if ("function".equals(invokeDynInsnNode.name)) {
                        String desc = invokeDynInsnNode.desc;
                        for (String toCheck : TO_CHECK) {
                            if (desc.endsWith(toCheck) && !desc.startsWith("()")) {
                                context.report(ISSUE,
                                        context.getLocation(method, classNode), // flags the surrounding method as a problem
                                        "Don't access the outer scope from Lambdas passed to Thinr.");
                            }
                        }
                    }
                }
            }
        }
    }
}
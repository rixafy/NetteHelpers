package org.nette.helpers.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import org.nette.helpers.utils.*

class ControlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(StringLiteralExpression::class.java),
            ComponentNameCompletionProvider()
        )
    }

    private class ComponentNameCompletionProvider : CompletionProvider<CompletionParameters?>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val stringLiteral = parameters.originalPosition?.parent as? StringLiteralExpression ?: return
            val prefix = if (result.prefixMatcher.prefix.contains("-")) {
                result.prefixMatcher.prefix.take(result.prefixMatcher.prefix.lastIndexOf("-") + 1)
            } else {
                ""
            }

            var target: PsiElement? = null

            // Case 1: $this['...'] array access
            val arrayAccess = stringLiteral.parent?.parent as? ArrayAccessExpression
            if (arrayAccess != null) {
                target = arrayAccess.value
            } else {
                // Case 2: $this->getComponent('...')->...
                val methodRef = PsiTreeUtil.getParentOfType(stringLiteral, MethodReference::class.java)
                if (methodRef != null && methodRef.name == "getComponent") {
                    val parametersList = methodRef.parameters
                    if (parametersList.isNotEmpty() && parametersList[0] == stringLiteral) {
                        target = methodRef.classReference
                    }
                }
            }

            val classes = target?.resolvePhpClasses()?.filter { it.isComponent() } ?: return
            if (classes.isEmpty()) return

            val seen = HashSet<String>()
            for (phpClass in target.resolvePhpClasses().filter { it.isComponent() } ) {
                for (method in phpClass.getControls()) {
                    val name = method.asControlName()
                    if (seen.add(name)) {
                        result.addElement(
                            LookupElementBuilder
                                .create(prefix + name)
                                .withPresentableText(name)
                                .withTypeText(method.type.toString(), true)
                        )
                    }
                }
            }
        }
    }
}

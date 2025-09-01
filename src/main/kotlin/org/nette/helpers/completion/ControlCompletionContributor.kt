package org.nette.helpers.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression
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
            val arrayAccess = parameters.originalPosition?.parent?.parent?.parent as? ArrayAccessExpression ?: return
            val prefix = if (result.prefixMatcher.prefix.contains("-")) {
                result.prefixMatcher.prefix.take(result.prefixMatcher.prefix.lastIndexOf("-") + 1)
            } else {
                ""
            }

            val classes = arrayAccess.value?.resolvePhpClasses()?.filter { it.isComponent() } ?: return
            if (classes.isEmpty()) return

            // Avoid duplicate component names if multiple classes provide the same factory
            val seen = HashSet<String>()
            for (phpClass in classes) {
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

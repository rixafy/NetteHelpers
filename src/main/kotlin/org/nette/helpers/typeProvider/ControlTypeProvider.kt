package org.nette.helpers.typeProvider

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import org.nette.helpers.utils.*

class ControlTypeProvider : PhpTypeProvider4 {
    override fun getKey(): Char = 'N'

    override fun getType(element: PsiElement): PhpType? {
        if (DumbService.getInstance(element.project).isDumb) {
            return null
        }

        var candidate: PsiElement? = null
        var componentName: String? = null

        // Resolve $this['abc']
        if (element is ArrayAccessExpression) {
            val index = element.index ?: return null
            val idxValue = index.value ?: return null
            if (idxValue !is StringLiteralExpression) return null
            candidate = element.value ?: return null
            componentName = idxValue.contents
        }

        // Resolve $this->getComponent('abc')
        if (element is MethodReference) {
            val name = element.name ?: return null
            if (name != "getComponent") return null
            val parameters = element.parameters
            if (parameters.isEmpty()) return null
            val first = parameters[0]
            if (first !is StringLiteralExpression) return null
            candidate = element.classReference ?: return null
            componentName = first.contents
        }

        for (phpClass in candidate?.resolvePhpClasses()?.filter { it.isComponent() } ?: return null) {
            return phpClass.getControls().firstOrNull { it.asControlName() == componentName }?.type ?: continue
        }

        return null
    }

    override fun complete(s: String, project: Project): PhpType? {
        return null
    }

    override fun getBySignature(
        expression: String,
        set: Set<String>,
        depth: Int,
        project: Project
    ): MutableCollection<out PhpNamedElement> {
        return mutableListOf()
    }
}

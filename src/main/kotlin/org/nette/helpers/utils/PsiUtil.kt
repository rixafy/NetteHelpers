package org.nette.helpers.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.Variable

fun PsiElement.resolvePhpClasses(): List<PhpClass> {
    // $this -> containing class
    if (this is Variable && this.name == "this") {
        PsiTreeUtil.getParentOfType(this, PhpClass::class.java)?.let { return listOf(it) }
    }

    // Direct resolves
    if (this is ClassReference) {
        val resolved = this.resolve()
        if (resolved is PhpClass) return listOf(resolved)
    }

    if (this is NewExpression) {
        val resolved = this.classReference?.resolve()
        if (resolved is PhpClass) return listOf(resolved)
    }

    if (this is PhpClass) return listOf(this)

    // Resolve via type information
    val typed = this as? PhpTypedElement ?: return emptyList()
    val project = this.project
    val index = PhpIndex.getInstance(project)
    val completed = index.completeType(project, typed.type, null)

    val result = LinkedHashSet<PhpClass>()
    for (candidate in completed.types) {
        if (!candidate.startsWith("\\")) continue
        val classes = index.getClassesByFQN(candidate)
        result.addAll(classes)
    }

    return result.toList()
}

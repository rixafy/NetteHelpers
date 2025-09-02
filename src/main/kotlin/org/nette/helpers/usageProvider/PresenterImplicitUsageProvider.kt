package org.nette.helpers.usageProvider

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import org.nette.helpers.utils.isAnyPresenterMethod
import org.nette.helpers.utils.isComponent

class PresenterImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement): Boolean {
        if (DumbService.getInstance(element.project).isDumb) {
            return false
        }

        if (element is PhpClass && element.isComponent()) {
            return true
        }

        if (element is Method && element.isAnyPresenterMethod()) {
            return element.containingClass?.isComponent() ?: false
        }

        return false
    }

    override fun isImplicitRead(element: PsiElement): Boolean {
        return false
    }

    override fun isImplicitWrite(element: PsiElement): Boolean {
        return false
    }
}

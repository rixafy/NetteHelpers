package org.nette.helpers.utils

import com.jetbrains.php.lang.psi.elements.Method
import java.util.Locale.getDefault

fun Method.asControlName(): String {
    if (!this.name.startsWith("createComponent") || this.name == "createComponent") {
        throw IllegalStateException("Method is not a control")
    }

    return this.name.removePrefix("createComponent").replaceFirstChar { it.lowercase(getDefault()) }
}

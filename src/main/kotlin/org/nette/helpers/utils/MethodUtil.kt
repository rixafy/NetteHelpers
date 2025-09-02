package org.nette.helpers.utils

import com.jetbrains.php.lang.psi.elements.Method
import java.util.Locale.getDefault

fun Method.asControlName(): String {
    if (!this.name.startsWith("createComponent") || this.name == "createComponent") {
        throw IllegalStateException("Method is not a control")
    }

    return this.name.removePrefix("createComponent").replaceFirstChar { it.lowercase(getDefault()) }
}

fun Method.isAnyPresenterMethod(): Boolean {
    return isControl() || isAction() || isRender() || isSignal() || isStartup() || isBeforeRender() || isAfterRender() || isShutdown()
}

fun Method.isControl(): Boolean {
    return this.name.startsWith("createComponent") && this.name != "createComponent"
}

fun Method.isAction(): Boolean {
    return this.name.startsWith("action") && this.name != "action"
}

fun Method.isRender(): Boolean {
    return this.name.startsWith("render") && this.name != "render"
}

fun Method.isStartup(): Boolean {
    return this.name == "startup"
}

fun Method.isBeforeRender(): Boolean {
    return this.name == "beforeRender"
}

fun Method.isAfterRender(): Boolean {
    return this.name == "afterRender"
}

fun Method.isShutdown(): Boolean {
    return this.name == "shutdown"
}

fun Method.isSignal(): Boolean {
    return this.name.startsWith("handle") && this.name != "handle"
}

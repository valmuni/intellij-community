// "Change type of base property 'A.x' to 'Any'" "true"
interface A {
    val x: CharSequence
}

class B(override val x: Any<caret>) : A

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.ChangeVariableTypeFix$ForOverridden
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.ChangeTypeQuickFixFactories$UpdateTypeQuickFix
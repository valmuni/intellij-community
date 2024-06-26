package soInlineCallInLastStatementInInlineFunctionArgument

fun main(args: Array<String>) {
    bar {
        nop()
        //Breakpoint!
        foo()
    }
}

inline fun bar(f: () -> Unit) {
    nop()
    f()
}

inline fun foo() {
    nop()
}

fun nop() {}

// STEP_OVER: 4
// REGISTRY: debugger.kotlin.step.through.inline.lambdas=false
fun test() {
    class Test {
        operator fun unaryPlus(a: Int=1): Test = Test()
    }
    val test = Test()
    test.unaryPl<caret>us()
}

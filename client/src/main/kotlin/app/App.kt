package app

fun main(args: Array<String>): Unit {
    val hello = Hello()
    hello.doSomething()
}

class Hello {
    var x = 0

    fun doSomething(): Unit {
        x++
    }
}
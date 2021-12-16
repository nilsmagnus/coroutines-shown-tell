package coroutine.exceptions

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GlobalScopeTest {

    @Test
    fun `dont use it if you dont know what it is`() = runBlocking {
        GlobalScope.launch {
            printSome("Throwing exception from GlobalScope")
            throw IndexOutOfBoundsException()
        }
        printSome("I dont care about exceptions in globalscope-coroutines")
    }
}
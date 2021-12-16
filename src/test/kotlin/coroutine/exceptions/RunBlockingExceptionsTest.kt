package coroutine.exceptions

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import java.lang.Thread.sleep
import kotlin.test.Test

class CoroutineTests {


    @Test
    fun `all coroutines launched completes at least at end of parent scope`(){
        do1()
        printSome("do 1 done")
    }

    private fun do1()= runBlocking {
        async {
            delay(500)
            printSome("1")
        }
        async {
            printSome("2")
        }
        async {
            delay(100)
            printSome("3")
        }

        val deferredResult = async {
            printSome("4")
            4 // async returns a result when done
        }

        printSome("Waited for " + deferredResult.await())

        printSome("all asyncs completes in runblocking")
    }






    @Test
    fun severalLaunchInRunblocking() = runBlocking {
        launch(Dispatchers.IO) {
            delay(500)
            printSome("1 from IO dispatcher")
        }
        launch(Dispatchers.Unconfined) {
            printSome("2 from unconfined")
        }
        launch(Dispatchers.Default) {
            delay(100)
            printSome("3 from default")
        }
        launch  {
            delay(100)
            printSome("4 on root scope")
        }

        val job = launch {
            printSome("5")
            4 // useless statement, launch does not return anything else than 'Unit'
        }

        printSome("I will wait here until job is done: " + job.join())

        printSome("all asyncs completes in runblocking")
    }


    @Test
    fun `nested async with different scopes`() = runBlocking {
        val scope = CoroutineScope(Job())
        async {
            async {
                scope.async {
                    delay(200)
                    printSome("leafnode ")
                }
            }
        }
        printSome("heavily nested")
    }


    @Test
    fun launchJoinsWhenRunBlockingEnds() = runBlocking {
        launch {
            delay(100)
            throw Exception("launched coroutines will explode when runblocking-scope ends")
        }

        printSome("I will print")
    }


    @Test
    fun `try catch naively`() = runBlocking {
        try {
            launch {
                delay(100)
                throw Exception("launched coroutines will still explode when runblocking-scope ends")
            }
        } catch (e: Exception) {
            printSome("I dont catch a thing")
        }

        printSome("I will print")
    }


    @Test
    fun `try catch closer`() = runBlocking {
        launch {
            try {
                delay(100)
                throw Exception("launched coroutines will still explode when runblocking-scope ends")
            } catch (e: Exception) {
                printSome("I catch a thing")
            }
        }
        printSome("I will print")
    }





    @Test
    fun asyncJoinsWhenRunBlockingEnds() = runBlocking {

        async {
            delay(50)
            throw Exception("async joins when runblocking ends ")
        }

        printSome("I will print")
    }






    @Test
    fun `exception handler can handle your exceptions, but cannot be used with runblocking`()   {

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
            printSome("Handle $exception in CoroutineExceptionHandler")
        }

        val scope = CoroutineScope(Job() + coroutineExceptionHandler)
        scope.launch {
            delay(50)
            throw Exception("BOOM")
        }

        sleep(200)
        printSome("I will print")
    }




    @Test
    fun `SHOW PRETTY DIAGRAM if a job fails with exceptions, all siblings and parents will also fail `() = runBlocking {

        async {
            delay(50)
            async {
                delay(100)
                printSome("I am cancelled because my sibling failed")
            }
            async {
                printSome("This is an ambush")
                throw Exception("Cluster-bomb!")
            }
        }

        delay(200)

        printSome("I will never print because my child failed")
    }



    @Test
    fun asyncAwaitFailsOnWait() = runBlocking {
        val one = async {
            throw RuntimeException("Boom")
        }
        one.await()
        printSome("I will not print")
    }

    @Test
    fun nestedAsyncAwaitFailsOnWait() = runBlocking {
        val one = async {
            async {
                throw RuntimeException("Boom")
            }
        }
        one.await()
        printSome("I will not print")
    }
}


fun printSome(msg: String) = println("\t\t\t---\t---\t---\t$msg")


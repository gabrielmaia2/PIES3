package com.newhome.app

import android.app.Activity
import com.google.android.gms.tasks.*
import com.google.firebase.inject.Deferred
import org.junit.Assert
import java.util.concurrent.Executor

class TestUtils {
    companion object {
        inline fun <reified E> assertThrowsAsync(message: String, testFunc: () -> Unit) : E {
            lateinit var ex: Throwable

            var exceptionThrown = false
            try {
                testFunc()
            } catch (e: Throwable) {
                ex = e
                exceptionThrown = e is E
            }

            Assert.assertTrue(message, exceptionThrown)
            return ex as E
        }

        inline fun <reified E> assertThrowsAsync(testFunc: () -> Unit) : E =
            assertThrowsAsync("Exception was not thrown", testFunc)

        fun <T> createSuccessTask(result: T): Task<T> {
            val task = object : Task<T>() {
                lateinit var task: Task<T>

                override fun isComplete(): Boolean = true

                override fun isSuccessful(): Boolean = true

                // ...
                override fun addOnCompleteListener(
                    executor: Executor,
                    onCompleteListener: OnCompleteListener<T>
                ): Task<T> {
                    onCompleteListener.onComplete(task)
                    return task
                }

                override fun addOnFailureListener(onFailureListener: OnFailureListener): Task<T> {
                    return task
                }

                override fun addOnFailureListener(
                    activity: Activity,
                    onFailureListener: OnFailureListener
                ): Task<T> {
                    return task
                }

                override fun addOnFailureListener(
                    executor: Executor,
                    onFailureListener: OnFailureListener
                ): Task<T> {
                    return task
                }

                override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<in T>): Task<T> {
                    onSuccessListener.onSuccess(result)
                    return task
                }

                override fun addOnSuccessListener(
                    activity: Activity,
                    onSuccessListener: OnSuccessListener<in T>
                ): Task<T> {
                    onSuccessListener.onSuccess(result)
                    return task
                }

                override fun addOnSuccessListener(
                    executor: Executor,
                    onSuccessListener: OnSuccessListener<in T>
                ): Task<T> {
                    onSuccessListener.onSuccess(result)
                    return task
                }

                override fun getException(): java.lang.Exception? = null

                override fun getResult(): T = result

                override fun <X : Throwable?> getResult(exceptionType: Class<X>): T = result

                override fun isCanceled(): Boolean = false
            }

            return task
        }

        fun createVoidSuccessTask(): Task<Void> = TestUtils2.createVoidTask()

        inline fun <T, reified E : Exception> createFailureTask(exception: E): Task<T> {
            val task = object : Task<T>() {
                lateinit var task: Task<T>

                override fun isComplete(): Boolean = true

                override fun isSuccessful(): Boolean = false

                // ...
                override fun addOnCompleteListener(
                    executor: Executor,
                    onCompleteListener: OnCompleteListener<T>
                ): Task<T> {
                    onCompleteListener.onComplete(task)
                    return task
                }

                override fun addOnFailureListener(onFailureListener: OnFailureListener): Task<T> {
                    onFailureListener.onFailure(exception)
                    return task
                }

                override fun addOnFailureListener(
                    activity: Activity,
                    onFailureListener: OnFailureListener
                ): Task<T> {
                    onFailureListener.onFailure(exception)
                    return task
                }

                override fun addOnFailureListener(
                    executor: Executor,
                    onFailureListener: OnFailureListener
                ): Task<T> {
                    onFailureListener.onFailure(exception)
                    return task
                }

                override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<in T>): Task<T> {
                    return task
                }

                override fun addOnSuccessListener(
                    activity: Activity,
                    onSuccessListener: OnSuccessListener<in T>
                ): Task<T> {
                    return task
                }

                override fun addOnSuccessListener(
                    executor: Executor,
                    onSuccessListener: OnSuccessListener<in T>
                ): Task<T> {
                    return task
                }

                override fun getException(): java.lang.Exception = exception

                override fun getResult(): T {
                    throw RuntimeExecutionException(exception)
                }

                override fun <X : Throwable?> getResult(exceptionType: Class<X>): T {
                    if (exceptionType.isInstance(E::class)) {
                        throw java.lang.IllegalStateException(exception)
                    } else {
                        throw RuntimeExecutionException(exception)
                    }
                }

                override fun isCanceled(): Boolean = false
            }

            return task
        }
    }
}
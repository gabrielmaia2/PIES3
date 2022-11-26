package com.newhome.app.dao

import android.graphics.Bitmap
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.firebase.FirebaseUsuarioProvider
import com.newhome.app.dto.NovoUsuario
import com.newhome.app.dto.UsuarioData
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseUsuarioProviderTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var firestore: FirebaseFirestore
    private lateinit var imageProvider: IImageProvider

    private lateinit var provider: FirebaseUsuarioProvider

    @Before
    fun setup() {
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        firestore = MockUtils.mockFirestore()
        imageProvider = MockUtils.mockImageProvider("usuarios/userid")

        provider = FirebaseUsuarioProvider(firestore, imageProvider)
    }

    @Test
    fun `verify get user`() = runTest {
        val user = provider.getUser("userid").await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.get() }
        assertEquals("userid", user.id)
        assertEquals("username", user.nome)
        assertEquals("details", user.detalhes)
    }

    @Test
    fun `verify get nonexistent user`() = runTest {
        val e = TestUtils.assertThrowsAsync<Exception> {
            provider.getUser("nonexistentid").await()
        }
        assertEquals("User does not exist.", e.message)
    }

    @Test
    fun `verify create user`() = runTest {
        val user = NovoUsuario(
            "userid",
            "username",
            "details",
            18
        )
        provider.createUser(user).await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.set(any()) }
    }

    @Test
    fun `verify update user`() = runTest {
        val user = UsuarioData(
            "userid",
            "username",
            "details"
        )
        provider.updateUser(user).await()
        val doc = firestore.collection("usuarios").document("userid")
        coVerify(exactly = 1) { doc.set(any(), SetOptions.merge()) }
    }

    @Test
    fun `verify update nonexistent user`() = runTest {
        val user = UsuarioData(
            "nonexistentid",
            "username",
            "details"
        )
        val e = TestUtils.assertThrowsAsync<Exception> { provider.updateUser(user).await() }
        assertEquals("User does not exist.", e.message)
    }

    @Test
    fun `verify delete user`() = runTest {
        // TODO implement
    }

    @Test
    fun `verify delete nonexistent user`() = runTest {
        // TODO implement
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify get user image`() = runTest {
        val image = provider.getUserImage("userid").await()
        coVerify(exactly = 1) { imageProvider.getImageOrDefault("usuarios/userid") }
        assertEquals(nonDefaultBitmap, image)
    }

    @Test
    fun `verify get nonexistent user image`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.getUserImage("nonexistentid").await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify set user image`() = runTest {
        provider.setUserImage("userid", nonDefaultBitmap).await()
        coVerify(exactly = 1) { imageProvider.saveImage("usuarios/userid", nonDefaultBitmap) }
    }

    @Test
    fun `verify set nonexistent user image`() = runTest {
        val e = TestUtils.assertThrowsAsync<NoSuchElementException> {
            provider.setUserImage("nonexistentid", nonDefaultBitmap).await()
        }
        assertEquals("Couldn't find user with specified ID.", e.message)
    }
}

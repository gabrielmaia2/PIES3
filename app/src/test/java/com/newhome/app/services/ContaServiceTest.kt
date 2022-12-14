package com.newhome.app.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.firestore.*
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credentials
import com.newhome.app.dto.NewAccount
import com.newhome.app.services.concrete.ContaService
import com.newhome.app.utils.Utils
import org.junit.Assert.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContaServiceTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var contaProvider: IContaProvider
    private lateinit var usuarioProvider: IUsuarioProvider
    private lateinit var imageProvider: IImageProvider

    private lateinit var service: ContaService

    @Before
    fun setup() {
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        contaProvider = MockUtils.mockContaProvider()
        usuarioProvider = MockUtils.mockUsuarioProvider()
        imageProvider = MockUtils.mockImageProvider("usuarios/userid", "usuarios/currentuserid")

        service = ContaService(usuarioProvider, contaProvider, imageProvider)
    }

    @Test
    fun `verify get account id`() = runTest {
        val contaId = service.getContaID()
        assertEquals("currentuserid", contaId)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify send email verification`() = runTest {
        service.enviarEmailConfirmacao().await()
        coVerify(exactly = 1) { contaProvider.enviarEmailConfirmacao() }
    }

    @Test
    fun `verify sign up invalid name`() = runTest {
        val newAccount = NewAccount("emailcorreto@example.com", "#SenhaCorreta", "Nom", 18)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(newAccount).await() }
        assertEquals("Nome deve ter entre 4 e 128 caracteres.", e.message)

        // length is 129
        newAccount.name = "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande " +
                "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande N"
        e = TestUtils.assertThrowsAsync { service.cadastrar(newAccount).await() }
        assertEquals("Nome deve ter entre 4 e 128 caracteres.", e.message)
    }

    @Test
    fun `verify sign up invalid age`() = runTest {
        val newAccount = NewAccount("emailcorreto@example.com", "#SenhaCorreta", "Nome Correto", 17)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(newAccount).await() }
        assertEquals("Idade deve estar entre 18 e 80.", e.message)

        newAccount.age = 81
        e = TestUtils.assertThrowsAsync { service.cadastrar(newAccount).await() }
        assertEquals("Idade deve estar entre 18 e 80.", e.message)
    }

    @Test
    fun `verify sign up invalid password`() = runTest {
        val newAccount = NewAccount("emailcorreto@example.com", "#Senha1", "Nome Correto", 18)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(newAccount).await() }
        assertEquals("Senha deve ter entre 8 e 64 caracteres.", e.message)

        // length is 65
        newAccount.password = "#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra123"
        e = TestUtils.assertThrowsAsync { service.cadastrar(newAccount).await() }
        assertEquals("Senha deve ter entre 8 e 64 caracteres.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign up valid data`() = runTest {
        val newAccount = NewAccount("emailcorreto@example.com", "#SenhaCorreta123", "Nome", 18)
        service.cadastrar(newAccount).await()

        // length is 128
        newAccount.name = "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande " +
                "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande "
        service.cadastrar(newAccount).await()

        newAccount.age = 18
        service.cadastrar(newAccount).await()

        newAccount.age = 80
        service.cadastrar(newAccount).await()

        newAccount.password = "#Senha12"
        service.cadastrar(newAccount).await()

        // length is 64
        newAccount.password = "#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12"
        service.cadastrar(newAccount).await()

        coVerify(exactly = 6) { contaProvider.criarConta(any()) }
        coVerify(exactly = 6) { contaProvider.enviarEmailConfirmacao() }
        verify(exactly = 6) { usuarioProvider.createUser(any(), any()) }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in no email verified`() = runTest {
        val contaProvider = MockUtils.mockContaProvider()
        val service = ContaService(usuarioProvider, contaProvider, imageProvider)
        every { contaProvider.emailConfirmacaoVerificado() } returns false

        val e = TestUtils.assertThrowsAsync<Exception> {
            service.logar(Credentials("emailcorreto@example.com", "#SenhaCorreta123")).await()
        }
        coVerify(exactly = 1) { contaProvider.logar(any()) }
        coVerify(exactly = 1) { contaProvider.enviarEmailConfirmacao() }
        coVerify(exactly = 1) { contaProvider.sair() }
        assertEquals(
            "Email not verified. Please, verify your email address before signing in.",
            e.message
        )
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in`() = runTest {
        service.logar(Credentials("emailcorreto@example.com", "#SenhaCorreta123")).await()
        coVerify(exactly = 1) { contaProvider.logar(any()) }
        coVerify(exactly = 0) { contaProvider.enviarEmailConfirmacao() }
        coVerify(exactly = 0) { contaProvider.sair() }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in with google first time`() = runTest {
        val usuarioProvider = MockUtils.mockUsuarioProvider()
        val service = ContaService(usuarioProvider, contaProvider, imageProvider)

        val account = mockk<GoogleSignInAccount>()
        val photoUrl = mockk<Uri>()
        every { account.displayName } returns "Nome Correto"
        every { account.photoUrl } returns photoUrl
        every { photoUrl.toString() } returns "https://www.example.com"

        mockkObject(Utils)
        coEvery { Utils.decodeBitmap(any()) } returns nonDefaultBitmap

        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeStream(any()) } returns nonDefaultBitmap

        every { usuarioProvider.getUser(any(), any()) } returns null

        service.entrarComGoogle(account).await()
        coVerify(exactly = 1) { contaProvider.entrarComGoogle(any()) }
        coVerify(exactly = 1) { usuarioProvider.createUser(any(), any()) }
        coVerify(exactly = 1) { imageProvider.saveUserImage(any(), nonDefaultBitmap) }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in with google`() = runTest {
        service.entrarComGoogle(mockk()).await()
        coVerify(exactly = 1) { contaProvider.entrarComGoogle(any()) }
        coVerify(exactly = 0) { usuarioProvider.createUser(any(), any()) }
        coVerify(exactly = 0) { imageProvider.saveUserImage(any(), any()) }
    }

    @Test
    fun `verify try use signed in account not signed in`() = runTest {
        val contaProvider = MockUtils.mockContaProvider()
        val service = ContaService(usuarioProvider, contaProvider, imageProvider)
        every { contaProvider.getContaID() } returns null

        val e = TestUtils.assertThrowsAsync<Exception> { service.tentarUsarContaLogada() }
        assertEquals("User not signed in.", e.message)
    }

    @Test
    fun `verify try use signed in account`() = runTest {
        service.tentarUsarContaLogada()
        assertEquals("currentuserid", service.getContaID())
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign out`() = runTest {
        service.sair().await()
        coVerify(exactly = 1) { contaProvider.sair() }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify delete account`() = runTest {
        // TODO
        service.excluirConta().await()
        coVerify(exactly = 1) { contaProvider.excluirConta() }
    }
}

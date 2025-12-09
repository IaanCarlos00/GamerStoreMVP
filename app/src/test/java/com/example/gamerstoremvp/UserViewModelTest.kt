package com.example.gamerstoremvp

import android.content.Context
import android.content.SharedPreferences
import com.example.gamerstoremvp.core.theme.Order
import com.example.gamerstoremvp.core.theme.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class UserViewModelTest {

    private lateinit var userViewModel: UserViewModel
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Preparamos los "dobles" (mocks) de las dependencias de Android
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        // Simulamos SharedPreferences para que devuelva el editor
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        
        // Simulamos que por defecto no hay usuario guardado (retorna null)
        `when`(mockSharedPreferences.getString(anyString(), org.mockito.ArgumentMatchers.nullable(String::class.java)))
            .thenReturn(null)

        userViewModel = UserViewModel(mockContext)
    }

    @Test
    fun `iniciarSesion actualiza el usuario actual`() {
        val user = User(id="1", name="Test", email="test@example.com", password="password", phone="phone", address="address", profileImageResId=0, levelUpPoints=0)
        
        // Ejecutamos la acción: Iniciar sesión
        userViewModel.loginUser(user)
        
        // Verificamos: El usuario actual en el ViewModel debe coincidir con el que iniciamos sesión
        assertEquals(user, userViewModel.currentUser)
    }

    @Test
    fun `cerrarSesion limpia el usuario actual`() {
        val user = User(id="1", name="Test", email="test@example.com", password="password", phone="phone", address="address", profileImageResId=0, levelUpPoints=0)
        userViewModel.loginUser(user)
        
        // Ejecutamos la acción: Cerrar sesión
        userViewModel.logoutUser()
        
        // Verificamos: El usuario actual debe ser nulo y la lista de órdenes debe estar vacía
        assertNull(userViewModel.currentUser)
        assertTrue(userViewModel.orders.isEmpty())
    }

    @Test
    fun `registrarUsuario agrega el usuario a la lista total`() {
        // Inicialmente hay 1 usuario por defecto
        val initialSize = userViewModel.allUsers.size
        
        val newUser = User(id="2", name="Nuevo Usuario", email="new@example.com", password="pass", phone="phone", address="addr", profileImageResId=0, levelUpPoints=0)
        
        // Ejecutamos la acción: Registrar usuario
        userViewModel.registerUser(newUser, null)
        
        // Verificamos: El tamaño de la lista aumentó en 1
        assertEquals(initialSize + 1, userViewModel.allUsers.size)
        // Verificamos: El nuevo usuario está en la lista
        assertTrue(userViewModel.allUsers.any { it.id == newUser.id })
    }

    @Test
    fun `registrarUsuario con referido actualiza los puntos del referente`() {
        val referrer = User(id="ref1", name="Referente", email="ref@example.com", password="pass", phone="phone", address="addr", profileImageResId=0, levelUpPoints=1000)
        // Registramos primero al referente
        userViewModel.registerUser(referrer, null)
        
        val newUser = User(id="new1", name="Nuevo Usuario", email="new@example.com", password="pass", phone="phone", address="addr", profileImageResId=0, levelUpPoints=0)
        
        // Ejecutamos la acción: Registrar usuario pasando al referente
        userViewModel.registerUser(newUser, referrer)
        
        // Buscamos al referente actualizado en la lista
        val updatedReferrer = userViewModel.allUsers.find { it.id == referrer.id }
        
        // Verificamos: Debe tener 1000 puntos más (1000 iniciales + 1000 bono = 2000)
        assertEquals(2000, updatedReferrer?.levelUpPoints)
    }

    @Test
    fun `actualizarUsuario modifica el usuario actual si los IDs coinciden`() {
        val originalUser = User(id="userUpdate", name="Original", email="orig@example.com", password="pass", phone="phone", address="addr", profileImageResId=0, levelUpPoints=0)
        userViewModel.registerUser(originalUser, null)
        userViewModel.loginUser(originalUser)
        
        val updatedUser = originalUser.copy(name = "Nombre Actualizado")
        
        // Ejecutamos la acción: Actualizar usuario
        userViewModel.updateUser(updatedUser)

        // Verificamos: El nombre cambió tanto en el usuario actual como en la lista
        assertEquals("Nombre Actualizado", userViewModel.currentUser?.name)
        assertEquals("Nombre Actualizado", userViewModel.allUsers.find { it.id == originalUser.id }?.name)
    }

    @Test
    fun `guardarOrden agrega la orden a la lista`() {
         val user = User(id="userOrder", name="Test", email="test@example.com", password="password", phone="phone", address="address", profileImageResId=0, levelUpPoints=0)
         userViewModel.loginUser(user)
         
         val order = Order(
            items = emptyList(),
            totalAmount = 100,
            userId = user.id
         )
         
         // Ejecutamos la acción: Guardar orden
         userViewModel.saveOrder(order)
         
         // Verificamos: La lista de órdenes contiene la orden creada
         assertTrue(userViewModel.orders.any { it.userId == user.id && it.totalAmount == 100 })
    }
}

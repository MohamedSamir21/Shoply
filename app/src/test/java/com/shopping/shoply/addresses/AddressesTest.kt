package com.shopping.shoply.addresses

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.addresses.domain.rules.AddressField
import com.shopping.shoply.addresses.domain.rules.AddressForm
import com.shopping.shoply.addresses.domain.rules.AddressLabel
import com.shopping.shoply.addresses.domain.rules.addressFrom
import com.shopping.shoply.addresses.domain.rules.formOf
import com.shopping.shoply.addresses.domain.rules.idToPromote
import com.shopping.shoply.addresses.domain.rules.idsToDemote
import com.shopping.shoply.addresses.domain.rules.invalidFields
import com.shopping.shoply.addresses.presentation.ADDRESS_ID_ARG
import com.shopping.shoply.addresses.presentation.AddressEditIntent
import com.shopping.shoply.addresses.presentation.AddressEditViewModel
import com.shopping.shoply.addresses.presentation.AddressMessage
import com.shopping.shoply.addresses.presentation.AddressesEvent
import com.shopping.shoply.addresses.presentation.AddressesIntent
import com.shopping.shoply.addresses.presentation.AddressesViewModel
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeAddressBookRepository
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("AddressesTest")
class AddressesTest {
    val home = DeliveryAddress("Home", "24 Elm Street, Apt 5B", id = "a1", recipientName = "Ahmed", line2 = "Brooklyn 11201", phone = "+1 555 0100", primary = true)
    val work = DeliveryAddress("Work", "9 Main St", id = "a2", recipientName = "Ahmed", line2 = "New York 10001", phone = "+1 555 0100")
    val validForm = AddressForm(AddressLabel.WORK, "Ahmed", "+1 555 0100", "9 Main St", "", "New York", "10001")

    val fakeProfileRepository = FakeProfileRepository().apply { addresses.value = listOf(home, work) }
    val fakeAddressBookRepository = FakeAddressBookRepository(fakeProfileRepository)

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun editSut(id: String = "") =
        AddressEditViewModel(SavedStateHandle(mapOf(ADDRESS_ID_ARG to id)), fakeProfileRepository, fakeAddressBookRepository)

    @Test
    @DisplayName("invalidFields: blank required fields and a short phone; apartment optional")
    fun `invalidFields rules`() {
        assertThat(invalidFields(validForm)).isEmpty()
        assertThat(invalidFields(AddressForm(phone = "12345"))).containsExactly(
            AddressField.NAME, AddressField.PHONE, AddressField.STREET, AddressField.CITY, AddressField.ZIP,
        )
    }

    @Test
    @DisplayName("addressFrom and formOf round-trip; the first address is always the default")
    fun `form mapping`() {
        val form = formOf(home)
        assertThat(form.street).isEqualTo("24 Elm Street")
        assertThat(form.apartment).isEqualTo("Apt 5B")
        assertThat(form.city).isEqualTo("Brooklyn")
        assertThat(form.zip).isEqualTo("11201")
        assertThat(addressFrom(form, "a1", isFirst = false)).isEqualTo(home)
        assertThat(addressFrom(validForm, "", isFirst = true).primary).isTrue()
    }

    @Test
    @DisplayName("idsToDemote / idToPromote keep exactly one default")
    fun `default rules`() {
        assertThat(idsToDemote(listOf(home, work), work.copy(primary = true))).containsExactly("a1")
        assertThat(idsToDemote(listOf(home, work), work)).isEmpty()
        assertThat(idToPromote(listOf(home, work), "a1")).isEqualTo("a2")
        assertThat(idToPromote(listOf(home, work), "a2")).isNull()
    }

    @Test
    @DisplayName("Addresses: Set as Default demotes the old default and says DEFAULT_SET")
    fun `set default`() = runTest(main.dispatcher) {
        val sut = AddressesViewModel(fakeProfileRepository, fakeAddressBookRepository)

        sut.events.test {
            sut.onIntent(AddressesIntent.SetDefault("a2"))
            assertThat(awaitItem()).isEqualTo(AddressesEvent.ShowMessage(AddressMessage.DEFAULT_SET))
        }
        assertThat(fakeAddressBookRepository.saveCalls.single().second).containsExactly("a1")
        assertThat(sut.state.value.addresses.single { it.primary }.id).isEqualTo("a2")
    }

    @Test
    @DisplayName("Edit: Save with blanks marks the fields and saves nothing; a valid form saves and is Done")
    fun `save validates then saves`() = runTest(main.dispatcher) {
        val sut = editSut()
        sut.onIntent(AddressEditIntent.Save)
        assertThat(sut.state.value.errors).isNotEmpty()
        assertThat(fakeAddressBookRepository.saveCalls).isEmpty()

        sut.events.test {
            sut.onIntent(AddressEditIntent.Change(validForm))
            sut.onIntent(AddressEditIntent.Save)
            assertThat(awaitItem()).isEqualTo(AddressesEvent.Done(AddressMessage.SAVED))
        }
    }

    @Test
    @DisplayName("Edit: loads the saved address; deleting the default promotes the next one")
    fun `edit loads and delete promotes`() = runTest(main.dispatcher) {
        val sut = editSut("a1")
        assertThat(sut.state.value.form.street).isEqualTo("24 Elm Street")

        sut.events.test {
            sut.onIntent(AddressEditIntent.Delete)
            assertThat(awaitItem()).isEqualTo(AddressesEvent.Done(AddressMessage.DELETED))
        }
        assertThat(fakeAddressBookRepository.deleteCalls.single()).isEqualTo("a1" to "a2")
    }
}

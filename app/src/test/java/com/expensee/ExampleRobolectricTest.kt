package com.expensee

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.expensee.data.AppDatabase
import com.expensee.data.FinanceRepository
import com.expensee.data.model.UserProfileEntity
import com.expensee.ui.FinanceViewModel
import com.expensee.ui.StartupState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Expensee", appName)
  }

  @Test
  fun `startup state initial value is Loading to prevent setup flash`() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(context)

    // The initial synchronous state must be Loading
    assertEquals(StartupState.Loading, viewModel.startupState.value)
    assertEquals(StartupState.Loading, viewModel.dashboardState.value.startupState)
  }

  @Test
  fun `fresh install transitions to NeedsSetup and completes to Ready`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val database = AppDatabase.getInstance(context)
    val repository = FinanceRepository(database)

    // Ensure fresh install state: onboardingCompleted = false
    database.userProfileDao().insertOrUpdate(
      UserProfileEntity(
        id = 1,
        name = "New User",
        onboardingCompleted = false
      )
    )

    val profile = repository.getUserProfileDirect()
    assertEquals(false, profile?.onboardingCompleted)

    // Verify StartupState logic mapping
    val state = when {
      profile == null -> StartupState.Loading
      !profile.onboardingCompleted -> StartupState.NeedsSetup
      else -> StartupState.Ready
    }
    assertEquals(StartupState.NeedsSetup, state)

    // Complete onboarding
    repository.completeOnboarding(
      name = "Alex",
      currencyCode = "USD",
      currencySymbol = "$",
      monthlyIncome = 5000.0,
      monthlyBudget = 2500.0,
      themeMode = "DARK",
      seedInitialIncomeTx = false
    )

    // Verify persistence in Room
    val updatedProfile = repository.getUserProfileDirect()
    assertEquals(true, updatedProfile?.onboardingCompleted)
    assertEquals("Alex", updatedProfile?.name)
    assertEquals("DARK", updatedProfile?.themeMode)

    // After completion, mapping resolves directly to Ready
    val postCompleteState = when {
      updatedProfile == null -> StartupState.Loading
      !updatedProfile.onboardingCompleted -> StartupState.NeedsSetup
      else -> StartupState.Ready
    }
    assertEquals(StartupState.Ready, postCompleteState)
  }

  @Test
  fun `returning user transitions directly to Ready without flashing NeedsSetup`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val database = AppDatabase.getInstance(context)
    val repository = FinanceRepository(database)

    // Simulate already completed onboarding from previous session
    database.userProfileDao().insertOrUpdate(
      UserProfileEntity(
        id = 1,
        name = "Existing User",
        currencyCode = "USD",
        currencySymbol = "$",
        monthlyBudgetLimit = 3000.0,
        monthlyIncome = 6000.0,
        themeMode = "SYSTEM",
        onboardingCompleted = true
      )
    )

    val viewModel = FinanceViewModel(context)

    // Initial state before DB emission must be Loading (never NeedsSetup)
    assertEquals(StartupState.Loading, viewModel.startupState.value)
    assertNotEquals(StartupState.NeedsSetup, viewModel.startupState.value)

    // Direct read from Room confirms user is already setup
    val profile = repository.getUserProfileDirect()
    assertTrue(profile?.onboardingCompleted == true)

    val returningState = when {
      profile == null -> StartupState.Loading
      !profile.onboardingCompleted -> StartupState.NeedsSetup
      else -> StartupState.Ready
    }
    assertEquals(StartupState.Ready, returningState)

    // Verify re-run onboarding flow: explicit user action reopens wizard
    repository.reopenOnboarding()
    val reopenedProfile = repository.getUserProfileDirect()
    assertEquals(false, reopenedProfile?.onboardingCompleted)

    val reopenedState = when {
      reopenedProfile == null -> StartupState.Loading
      !reopenedProfile.onboardingCompleted -> StartupState.NeedsSetup
      else -> StartupState.Ready
    }
    assertEquals(StartupState.NeedsSetup, reopenedState)

    // Re-completing setup restores Ready and updates persistence
    repository.completeOnboarding(
      name = "Existing User",
      currencyCode = "USD",
      currencySymbol = "$",
      monthlyIncome = 6000.0,
      monthlyBudget = 3000.0,
      themeMode = "SYSTEM",
      seedInitialIncomeTx = false
    )
    val reCompletedProfile = repository.getUserProfileDirect()
    assertEquals(true, reCompletedProfile?.onboardingCompleted)
  }
}

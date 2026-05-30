package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Quran Study & Tafsir", appName)
  }

  @Test
  fun `test database seeding`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.QuranRepository(context)
    val surahs = repo.getSurahs()
    println("SUCCESSFULLY SEEDED: ${surahs.size} surahs found.")
    assertEquals(114, surahs.size)
  }

  @Test
  fun `test viewModel instantiation`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.QuranViewModel(application)
    org.junit.Assert.assertNotNull(viewModel)
  }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.CharuAIInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Charu AI OS", appName)
  }

  @Test
  fun `test charu local pattern parser for bengali commands`() {
    val youtubeCmd = CharuAIInterpreter.matchLocalPattern("ইউটিউব খোলো")
    assertEquals("OPEN_APP", youtubeCmd.action)
    assertEquals("com.google.android.youtube", youtubeCmd.packageName)
    assertEquals("Opening YouTube, sir.", youtubeCmd.assistantReply)

    val homeCmd = CharuAIInterpreter.matchLocalPattern("হোম স্ক্রিনে যাও")
    assertEquals("GLOBAL_HOME", homeCmd.action)
    assertEquals("Going to home screen.", homeCmd.assistantReply)

    val messengerCmd = CharuAIInterpreter.matchLocalPattern("মেসেঞ্জারে গিয়ে রনিকে লেখো কেমন আছো")
    assertNotNull(messengerCmd.sequence)
    assertEquals(6, messengerCmd.sequence?.size)
    assertEquals("OPEN_APP", messengerCmd.sequence?.get(0)?.action)
    assertEquals("com.facebook.orca", messengerCmd.sequence?.get(0)?.packageName)
  }

  @Test
  fun `test markdown json cleaner`() {
    val raw = "```json\n{\"assistant_reply\":\"Test\",\"action\":\"GLOBAL_HOME\"}\n```"
    val cleaned = CharuAIInterpreter.cleanMarkdownJson(raw)
    assertEquals("{\"assistant_reply\":\"Test\",\"action\":\"GLOBAL_HOME\"}", cleaned)
  }
}

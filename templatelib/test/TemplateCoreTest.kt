namespace std.template

import std.*
import std.template.io.*
import std.io.*
import std.util.*
import std.test.*
import java.util.*

class EmailTemplate(var name: String = "James", var time: Date = Date()) : TextTemplate() {
  override fun render() {
    print("Hello there $name and how are you? Today is $time. Kotlin rocks")
  }
}

/**
 TODO compile error
 http://youtrack.jetbrains.net/issue/KT-865

class MoreDryTemplate(var name: String = "James", var time: Date = Date()) : TextTemplate() {
  override fun render() {
    +"Hey there $name and how are you? Today is $time. Kotlin rocks"
  }
}
*/

class TemplateCoreTest() : TestSupport() {
  fun testDefaultValues() {
    val text = EmailTemplate().renderToText()
    assert {
      println(text)
      text.startsWith("Hello there James")
    }
  }

  fun testDifferentValues() {
    val text = EmailTemplate("Andrey").renderToText()
    assert {
      println(text)
      text.startsWith("Hello there Andrey")
    }
  }

  /*
   TODO compile error

  fun testMoreDryTemplate() {
    val text = MoreDryTemplate("Alex").renderToText()
    assert {
      println(text)
      text.startsWith("Hey there Alex")
    }
  }
  */
}
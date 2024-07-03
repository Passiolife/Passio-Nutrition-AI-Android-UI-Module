package ai.passio.nutrition.uimodule

import org.junit.Test

import org.junit.Assert.*
import java.lang.Integer.min
import kotlin.math.absoluteValue

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val res1 = longestPalindrome("babad")
        val res2 = longestPalindrome("")
        val res3 = longestPalindrome("a")
        val res4 = longestPalindrome("c12345bbaabb")
        val res5 = longestPalindrome("123bcb321thghi")
        val res6 = longestPalindrome("uu123uu3")
        println()
    }

    fun longestPalindrome(s: String): String {
        if (s.length == 1) return s
        var longest = ""
        var left = 0
        while (left < s.length) {
            var right = s.length
            while (left < right) {
                val subs = s.subSequence(left, right).toString()
                if (isPalindrome(subs) && subs.length > longest.length) {
                    longest = subs
                    break
                }
                right--
            }
            left++
        }
        return longest
    }

    fun isPalindrome(s: String): Boolean {
        var left = 0
        var right = s.length - 1
        while (left < right) {
            if (s[left] != s[right]) {
                return false
            }
            left++
            right--
        }
        return true
    }
}
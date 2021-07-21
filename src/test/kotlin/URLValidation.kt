import me.arynxd.monke.util.isValidUrl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class URLValidation {
    @Test
    fun `valid url`() {
        assertTrue("https://google.com".isValidUrl())
        assertTrue("http://youtube.com".isValidUrl())
        assertTrue("http://google.com".isValidUrl())
    }

    @Test
    fun `invalid url`() {
        assertFalse("garbage".isValidUrl())
        assertFalse("htt://google.com".isValidUrl())
        assertFalse("ww://youtube.com".isValidUrl())
    }
}
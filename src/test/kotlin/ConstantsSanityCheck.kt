
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import me.arynxd.monke.DEFAULT_BOT_PREFIX
import me.arynxd.monke.MONKE_VERSION

class ConstantsSanityCheck {
    @Test
    fun `should always be true`() {
        assertEquals(DEFAULT_BOT_PREFIX, "mk!")
        assertEquals(MONKE_VERSION, "1.0.0")
    }
}
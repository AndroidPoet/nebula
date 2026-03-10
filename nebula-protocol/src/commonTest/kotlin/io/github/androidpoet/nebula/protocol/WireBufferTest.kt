package io.github.androidpoet.nebula.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WireBufferTest {

  @Test
  fun test_writeByte_readByte_roundTrips() {
    val buf = WireBuffer()
    buf.writeByte(0)
    buf.writeByte(127)
    buf.writeByte(255)
    buf.rewind()
    assertEquals(0, buf.readByte())
    assertEquals(127, buf.readByte())
    assertEquals(255, buf.readByte())
  }

  @Test
  fun test_writeShort_readShort_bigEndian() {
    val buf = WireBuffer()
    buf.writeShort(0)
    buf.writeShort(256)
    buf.writeShort(0x7FFF)
    buf.rewind()
    assertEquals(0, buf.readShort())
    assertEquals(256, buf.readShort())
    assertEquals(0x7FFF, buf.readShort())
  }

  @Test
  fun test_writeInt_readInt_roundTrips() {
    val buf = WireBuffer()
    buf.writeInt(0)
    buf.writeInt(1)
    buf.writeInt(-1)
    buf.writeInt(Int.MAX_VALUE)
    buf.writeInt(Int.MIN_VALUE)
    buf.rewind()
    assertEquals(0, buf.readInt())
    assertEquals(1, buf.readInt())
    assertEquals(-1, buf.readInt())
    assertEquals(Int.MAX_VALUE, buf.readInt())
    assertEquals(Int.MIN_VALUE, buf.readInt())
  }

  @Test
  fun test_writeFloat_readFloat_roundTrips() {
    val buf = WireBuffer()
    buf.writeFloat(0f)
    buf.writeFloat(1.5f)
    buf.writeFloat(-3.14f)
    buf.writeFloat(Float.MAX_VALUE)
    buf.writeFloat(Float.MIN_VALUE)
    buf.rewind()
    assertEquals(0f, buf.readFloat())
    assertEquals(1.5f, buf.readFloat())
    assertEquals(-3.14f, buf.readFloat())
    assertEquals(Float.MAX_VALUE, buf.readFloat())
    assertEquals(Float.MIN_VALUE, buf.readFloat())
  }

  @Test
  fun test_writeLong_readLong_roundTrips() {
    val buf = WireBuffer()
    buf.writeLong(0L)
    buf.writeLong(Long.MAX_VALUE)
    buf.writeLong(Long.MIN_VALUE)
    buf.writeLong(-281470681808896L)
    buf.rewind()
    assertEquals(0L, buf.readLong())
    assertEquals(Long.MAX_VALUE, buf.readLong())
    assertEquals(Long.MIN_VALUE, buf.readLong())
    assertEquals(-281470681808896L, buf.readLong())
  }

  @Test
  fun test_writeDouble_readDouble_roundTrips() {
    val buf = WireBuffer()
    buf.writeDouble(3.141592653589793)
    buf.writeDouble(-2.718281828459045)
    buf.rewind()
    assertEquals(3.141592653589793, buf.readDouble())
    assertEquals(-2.718281828459045, buf.readDouble())
  }

  @Test
  fun test_writeString_readString_roundTrips() {
    val buf = WireBuffer()
    buf.writeString("")
    buf.writeString("hello")
    buf.writeString("日本語テスト")
    buf.writeString("emoji 🚀")
    buf.rewind()
    assertEquals("", buf.readString())
    assertEquals("hello", buf.readString())
    assertEquals("日本語テスト", buf.readString())
    assertEquals("emoji 🚀", buf.readString())
  }

  @Test
  fun test_writeBoolean_readBoolean_roundTrips() {
    val buf = WireBuffer()
    buf.writeBoolean(true)
    buf.writeBoolean(false)
    buf.rewind()
    assertEquals(true, buf.readBoolean())
    assertEquals(false, buf.readBoolean())
  }

  @Test
  fun test_writeColor_readColor_roundTrips() {
    val buf = WireBuffer()
    buf.writeColor(0xFF6750A4)
    buf.writeColor(0x00000000)
    buf.writeColor(0xFFFFFFFF)
    buf.rewind()
    assertEquals(0xFF6750A4, buf.readColor())
    assertEquals(0x00000000, buf.readColor())
    assertEquals(0xFFFFFFFF, buf.readColor())
  }

  @Test
  fun test_writeFloatArray_readFloatArray_roundTrips() {
    val buf = WireBuffer()
    val input = floatArrayOf(1f, 2.5f, -3f, 0f)
    buf.writeFloatArray(input)
    buf.rewind()
    val output = buf.readFloatArray()
    assertEquals(input.size, output.size)
    for (i in input.indices) {
      assertEquals(input[i], output[i])
    }
  }

  @Test
  fun test_writeBytes_readBytes_roundTrips() {
    val buf = WireBuffer()
    val input = byteArrayOf(1, 2, 3, 0, -1, 127)
    buf.writeBytes(input)
    buf.rewind()
    val output = buf.readBytes()
    assertTrue(input.contentEquals(output))
  }

  @Test
  fun test_size_tracksWritePosition() {
    val buf = WireBuffer()
    assertEquals(0, buf.size)
    buf.writeInt(42)
    assertEquals(4, buf.size)
    buf.writeString("hi")
    assertEquals(4 + 4 + 2, buf.size) // int + length-prefix + 2 bytes
  }

  @Test
  fun test_remaining_tracksUnread() {
    val buf = WireBuffer()
    buf.writeInt(1)
    buf.writeInt(2)
    assertEquals(8, buf.remaining)
    buf.rewind()
    buf.readInt()
    assertEquals(4, buf.remaining)
  }

  @Test
  fun test_toByteArray_trimmedToSize() {
    val buf = WireBuffer(1024)
    buf.writeInt(42)
    val bytes = buf.toByteArray()
    assertEquals(4, bytes.size)
  }

  @Test
  fun test_fromByteArray_restoresContent() {
    val original = WireBuffer()
    original.writeString("test")
    original.writeFloat(3.14f)
    val bytes = original.toByteArray()

    val restored = WireBuffer.fromByteArray(bytes)
    assertEquals("test", restored.readString())
    assertEquals(3.14f, restored.readFloat())
  }

  @Test
  fun test_startWithSize_endWithSize_writesPayloadSize() {
    val buf = WireBuffer()
    buf.startWithSize(42) // opcode + 4-byte size placeholder
    buf.writeFloat(1f)
    buf.writeFloat(2f)
    buf.endWithSize()

    buf.rewind()
    assertEquals(42, buf.readByte()) // opcode
    assertEquals(8, buf.readInt())   // payload size = 2 floats = 8 bytes
    assertEquals(1f, buf.readFloat())
    assertEquals(2f, buf.readFloat())
  }

  @Test
  fun test_skip_advancesReadPosition() {
    val buf = WireBuffer()
    buf.writeInt(1)
    buf.writeInt(2)
    buf.writeInt(3)
    buf.rewind()
    buf.readInt() // read 1
    buf.skip(4)   // skip 2
    assertEquals(3, buf.readInt())
  }

  @Test
  fun test_peekOpcode_doesNotAdvance() {
    val buf = WireBuffer()
    buf.writeByte(42)
    buf.writeByte(99)
    buf.rewind()
    assertEquals(42, buf.peekOpcode())
    assertEquals(42, buf.peekOpcode()) // still 42
    assertEquals(42, buf.readByte())   // now advances
    assertEquals(99, buf.readByte())
  }

  @Test
  fun test_autoGrow_handlesLargeData() {
    val buf = WireBuffer(8) // tiny initial capacity
    repeat(1000) { buf.writeInt(it) }
    buf.rewind()
    repeat(1000) { assertEquals(it, buf.readInt()) }
  }
}

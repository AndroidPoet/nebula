package io.github.androidpoet.nebula.protocol

/**
 * Binary wire format for the Nebula remote compose protocol.
 *
 * Encodes UI operations as a compact byte stream matching the AndroidX
 * Compose Remote wire format. Each operation starts with a single-byte
 * opcode followed by its payload. Multi-byte values use big-endian order.
 *
 * Supports size-prefixed blocks via [startWithSize]/[endWithSize] for
 * skip-ahead parsing — the player can jump past unknown operations.
 */
public class WireBuffer(initialCapacity: Int = 1024 * 1024) {

  private var data = ByteArray(initialCapacity)
  private var position = 0
  private var readPosition = 0
  private val sizeStack = ArrayDeque<Int>()

  /** Total bytes written. */
  public val size: Int get() = position

  /** Current read position. */
  public val readIndex: Int get() = readPosition

  /** Bytes remaining to read. */
  public val remaining: Int get() = position - readPosition

  /** Reset read cursor to beginning. */
  public fun rewind() {
    readPosition = 0
  }

  /** Set read position to a specific index. */
  public fun setReadPosition(pos: Int) {
    readPosition = pos
  }

  /** Get the raw byte array trimmed to size. */
  public fun toByteArray(): ByteArray = data.copyOf(position)

  // ── Opcode Write ───────────────────────────────────

  /**
   * Write a single-byte opcode, starting a new operation.
   */
  public fun start(opcode: Int) {
    ensureCapacity(1)
    data[position++] = opcode.toByte()
  }

  /**
   * Write an opcode followed by a 4-byte size placeholder.
   *
   * Call [endWithSize] after writing the operation payload to
   * backfill the size field. This enables skip-ahead parsing.
   */
  public fun startWithSize(opcode: Int) {
    start(opcode)
    sizeStack.addLast(position)
    writeInt(0) // placeholder for size
  }

  /**
   * Backfill the size field written by [startWithSize].
   *
   * The size covers only the payload bytes (excluding the opcode
   * and the 4-byte size field itself).
   */
  public fun endWithSize() {
    val sizePos = sizeStack.removeLast()
    val payloadSize = position - sizePos - 4
    data[sizePos] = (payloadSize shr 24).toByte()
    data[sizePos + 1] = (payloadSize shr 16).toByte()
    data[sizePos + 2] = (payloadSize shr 8).toByte()
    data[sizePos + 3] = payloadSize.toByte()
  }

  // ── Primitive Write ────────────────────────────────

  public fun writeByte(value: Int) {
    ensureCapacity(1)
    data[position++] = value.toByte()
  }

  public fun writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
  }

  public fun writeShort(value: Int) {
    ensureCapacity(2)
    data[position++] = (value shr 8).toByte()
    data[position++] = value.toByte()
  }

  public fun writeInt(value: Int) {
    ensureCapacity(4)
    data[position++] = (value shr 24).toByte()
    data[position++] = (value shr 16).toByte()
    data[position++] = (value shr 8).toByte()
    data[position++] = value.toByte()
  }

  public fun writeFloat(value: Float) {
    writeInt(value.toRawBits())
  }

  public fun writeLong(value: Long) {
    ensureCapacity(8)
    data[position++] = (value shr 56).toByte()
    data[position++] = (value shr 48).toByte()
    data[position++] = (value shr 40).toByte()
    data[position++] = (value shr 32).toByte()
    data[position++] = (value shr 24).toByte()
    data[position++] = (value shr 16).toByte()
    data[position++] = (value shr 8).toByte()
    data[position++] = value.toByte()
  }

  public fun writeDouble(value: Double) {
    writeLong(value.toRawBits())
  }

  public fun writeString(value: String) {
    val bytes = value.encodeToByteArray()
    writeInt(bytes.size)
    ensureCapacity(bytes.size)
    bytes.copyInto(data, position)
    position += bytes.size
  }

  public fun writeColor(value: Long) {
    writeLong(value)
  }

  /** Write a float array prefixed by its length. */
  public fun writeFloatArray(values: FloatArray) {
    writeInt(values.size)
    for (v in values) writeFloat(v)
  }

  /** Write raw bytes. */
  public fun writeBytes(bytes: ByteArray) {
    writeInt(bytes.size)
    ensureCapacity(bytes.size)
    bytes.copyInto(data, position)
    position += bytes.size
  }

  // ── Primitive Read ─────────────────────────────────

  public fun readByte(): Int {
    return data[readPosition++].toInt() and 0xFF
  }

  public fun readBoolean(): Boolean {
    return readByte() != 0
  }

  public fun readShort(): Int {
    val high = (data[readPosition++].toInt() and 0xFF) shl 8
    val low = data[readPosition++].toInt() and 0xFF
    return high or low
  }

  public fun readInt(): Int {
    val b0 = (data[readPosition++].toInt() and 0xFF) shl 24
    val b1 = (data[readPosition++].toInt() and 0xFF) shl 16
    val b2 = (data[readPosition++].toInt() and 0xFF) shl 8
    val b3 = data[readPosition++].toInt() and 0xFF
    return b0 or b1 or b2 or b3
  }

  public fun readFloat(): Float {
    return Float.fromBits(readInt())
  }

  public fun readLong(): Long {
    val b0 = (data[readPosition++].toLong() and 0xFF) shl 56
    val b1 = (data[readPosition++].toLong() and 0xFF) shl 48
    val b2 = (data[readPosition++].toLong() and 0xFF) shl 40
    val b3 = (data[readPosition++].toLong() and 0xFF) shl 32
    val b4 = (data[readPosition++].toLong() and 0xFF) shl 24
    val b5 = (data[readPosition++].toLong() and 0xFF) shl 16
    val b6 = (data[readPosition++].toLong() and 0xFF) shl 8
    val b7 = data[readPosition++].toLong() and 0xFF
    return b0 or b1 or b2 or b3 or b4 or b5 or b6 or b7
  }

  public fun readDouble(): Double {
    return Double.fromBits(readLong())
  }

  public fun readString(): String {
    val length = readInt()
    val str = data.decodeToString(readPosition, readPosition + length)
    readPosition += length
    return str
  }

  public fun readColor(): Long = readLong()

  /** Read a float array prefixed by its length. */
  public fun readFloatArray(): FloatArray {
    val size = readInt()
    return FloatArray(size) { readFloat() }
  }

  /** Read raw bytes prefixed by length. */
  public fun readBytes(): ByteArray {
    val size = readInt()
    val result = data.copyOfRange(readPosition, readPosition + size)
    readPosition += size
    return result
  }

  /** Read the opcode byte at current position. */
  public fun readOpcode(): Int {
    return readByte()
  }

  /** Peek at the next opcode without advancing the read position. */
  public fun peekOpcode(): Int {
    return data[readPosition].toInt() and 0xFF
  }

  /**
   * Skip ahead by [count] bytes from current read position.
   * Used with size-prefixed operations for skip-ahead parsing.
   */
  public fun skip(count: Int) {
    readPosition += count
  }

  /** Load from an existing byte array. */
  public fun loadFrom(bytes: ByteArray) {
    data = bytes.copyOf()
    position = bytes.size
    readPosition = 0
  }

  /**
   * Move a block of bytes within the buffer.
   * Supports reordering commands for optimization.
   */
  public fun moveBlock(srcStart: Int, srcEnd: Int, destStart: Int) {
    val block = data.copyOfRange(srcStart, srcEnd)
    val blockSize = srcEnd - srcStart

    if (destStart < srcStart) {
      data.copyInto(data, destStart + blockSize, destStart, srcStart)
      block.copyInto(data, destStart)
    } else {
      data.copyInto(data, srcStart, srcEnd, destStart)
      block.copyInto(data, destStart - blockSize)
    }
  }

  private fun ensureCapacity(needed: Int) {
    if (position + needed > data.size) {
      data = data.copyOf(maxOf(data.size * 2, position + needed))
    }
  }

  public companion object {
    public const val VERSION: Int = 1

    public fun fromByteArray(bytes: ByteArray): WireBuffer {
      return WireBuffer(bytes.size).apply { loadFrom(bytes) }
    }
  }
}

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.github.fommil.jni.JniLoader;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class Main {

  /**
   * The number of frames (a.k.a samples) in one cycle.
   * 256 is a typical value.
   */
  private static final int CYCLE_LENGTH = 256;
  private static final int MAX_CYCLE_LENGTH = 4096;

  static void loadNativeLib() {
    JniLoader.load("native/libbenchmark.so");
  }

  /**
   * Create a direct float buffer of given length.
   *
   * @param length the number float elements provided by the buffer.
   * @return a direct float buffer.
   */
  static FloatBuffer makeDirectFloatBuffer(int length) {
    ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(length * Float.SIZE / Byte.SIZE);
    newByteBuffer.order(ByteOrder.nativeOrder()); // see https://bugs.openjdk.java.net/browse/JDK-5043362
    FloatBuffer newFloatBuffer = newByteBuffer.asFloatBuffer();
    newFloatBuffer.clear();
    return newFloatBuffer;
  }

  /**
   * Transfers the entire content of the given source array into the destination buffer.
   * Source and destination must be of the same size.
   *
   * @param dest the destination buffer.
   * @param src  The src array.
   */
  static void fillBufferFromJArray(FloatBuffer dest, float[] src) {
    dest.clear();
    if (dest.remaining() != src.length) {
      throw new RuntimeException("Source and destination shall be of the same size.");
    }
    dest.put(src);
    dest.clear();
  }

  /**
   * Create a java- float- array of given length. And fill it with random values between -1.0 and +1.0.
   *
   * @param length the number float elements provided by the array.
   * @return an array of floats.
   */
  static float[] makeJArray(int length) {
    float[] jArray = new float[length];
    for (int i = 0; i < jArray.length; i++) {
      jArray[i] = (float) (Math.random() * 2.0) - 1.0f;
    }
    return jArray;
  }


  /**
   * Simulate the writing of data into the native world using direct buffers.
   *
   * The native implementation shall use `GetDirectBufferAddress` to access the buffer data
   * and `memcopy` to transfer the data from the buffer to some native data structure.
   *
   * @param directFloatOutputBuffer the data that would be written to a port or so.
   * @param count                   the number of items to be written (the size of the directFloatOutputBuffer).
   */
  static native void nativeWriteOutDirectBuffer(FloatBuffer directFloatOutputBuffer, int count);

  /**
   * Simulate the reading of data from the native world using direct buffers.
   *
   * The native implementation shall use `GetDirectBufferAddress` to access the buffer data
   * and `memcopy` to transfer the data from some native data structure to the buffer.
   *
   * @param directFloatInputBuffer a container that gets filled by the native part.
   * @param count                  the number of items to be written (the size of the directFloatOutputBuffer).
   */
  static native void nativeReadInDirectBuffer(FloatBuffer directFloatInputBuffer, int count);

  /**
   * Simulate the writing of data into the native world using java arrays.
   *
   * The native implementation shall use `SetFloatArrayRegion` to access and
   * transfer the data from the array to some native data structure.
   *
   * @param count            the number of items to be written (the size of the floatOutputArray).
   * @param floatOutputArray data that should be written to a port or so.
   */
  static native void nativeWriteOutJArray(float[] floatOutputArray, int count);

  /**
   * Simulate the reading of data from the native world.
   *
   * The native implementation shall use `GetFloatArrayRegion` to access and
   * transfer the data from some native data structure into the array.
   *
   * @param floatInputArray a container that gets filled by the native part.
   * @param count           the number of items to be read (the size of the floatInputArray).
   */
  static native void nativeReadJArray(float[] floatInputArray, int count);

  /**
   * Do some calculation within the native world involving the elements of a direct buffer.
   *
   * For the sake of this example we'll calculate the Euclidean-norm of the buffer elements.
   *
   * @param directFloatBuffer a buffer filled with non trivial data (not all zero).
   * @param count             the size of the directFloatOutputBuffer.
   * @return the Euclidean norm of the buffer content.
   */
  static native float nativeProcessDirectBuffer(FloatBuffer directFloatBuffer, int count);

  /**
   * Do some calculation within the java world involving the elements of a direct buffer.
   *
   * For the sake of this example we'll calculate the Euclidean-norm of the buffer elements.
   *
   * @param directFloatBuffer a buffer filled with non trivial data (not all zero).
   * @return the Euclidean norm of the buffer content.
   */
  static float javaProcessDirectBuffer(FloatBuffer directFloatBuffer) {
    directFloatBuffer.rewind();
    float result = 0.0f;
    float next;
    while (directFloatBuffer.hasRemaining()) {
      next = directFloatBuffer.get();
      result += next * next;
    }
    return (float) Math.sqrt(result);

  }

  /**
   * Do some calculation within the java world involving the elements of a java-array.
   *
   * For the sake of this example we'll calculate the Euclidean-norm of the array elements.
   *
   * @param floatArray a buffer filled with non trivial data (not all zero).
   * @return the Euclidean norm of the array content.
   */
  static float javaProcessJArray(float[] floatArray) {
    float result = 0.0f;

    for (float f : floatArray) {
      result += f * f;
    }

    return (float) Math.sqrt(result);
  }


  static public void main(String[] args) {
    int cycleLength = CYCLE_LENGTH;
    if (args.length > 0) {
      cycleLength = toInt(args[0], CYCLE_LENGTH);
    }
    cycleLength = Math.min(MAX_CYCLE_LENGTH, cycleLength);

    loadNativeLib();

    writeDirectBuffer_vs_writeJArray(cycleLength);
    readDirectBuffer_vs_readJArray(cycleLength);
    process_array_vs_buffer(cycleLength);
  }

  private static void writeDirectBuffer_vs_writeJArray(int cycleLength) {
    System.err.print("\nWriting using direct buffer vs writing using JArray\n");
    long repetitions = 5000000000L / cycleLength;
    long start_ms;
    long end_ms;

    float[] JArray = makeJArray(cycleLength);
    FloatBuffer floatBuffer = makeDirectFloatBuffer(cycleLength);
    fillBufferFromJArray(floatBuffer, JArray);

    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      nativeWriteOutDirectBuffer(floatBuffer, cycleLength);
    }
    end_ms = System.currentTimeMillis();
    long directBufferFullDuration_ms = end_ms - start_ms;
    reportMeasurement("DirectBuffer", cycleLength, repetitions, start_ms, end_ms);

    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      nativeWriteOutJArray(JArray, cycleLength);
    }
    end_ms = System.currentTimeMillis();
    long JArrayFullDuration_ms = end_ms - start_ms;
    reportMeasurement("JArray", cycleLength, repetitions, start_ms, end_ms);

    reportConclusion("DirectBuffer", "JArray", 1d/directBufferFullDuration_ms, 1d/JArrayFullDuration_ms);
  }

  private static void readDirectBuffer_vs_readJArray(int cycleLength) {
    System.err.print("\nReading using direct buffer vs reading using JArray\n");
    long repetitions = 3000000000L / cycleLength;
    long start_ms;
    long end_ms;

    float[] JArray = new float[cycleLength];
    FloatBuffer floatBuffer = makeDirectFloatBuffer(cycleLength);

    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      nativeReadInDirectBuffer(floatBuffer, cycleLength);
    }
    end_ms = System.currentTimeMillis();
    long directBufferFullDuration_ms = end_ms - start_ms;
    reportMeasurement("DirectBuffer", cycleLength, repetitions, start_ms, end_ms);

    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      nativeReadJArray(JArray, cycleLength);
    }
    end_ms = System.currentTimeMillis();
    long JArrayFullDuration_ms = end_ms - start_ms;
    reportMeasurement("JArray", cycleLength, repetitions, start_ms, end_ms);

    reportConclusion("DirectBuffer", "JArray", 1d/directBufferFullDuration_ms, 1d/JArrayFullDuration_ms);
  }

  private static void process_array_vs_buffer(int cycleLength) {
    System.err.print("\nProcessing using direct buffer vs processing using JArray\n");
    long repetitions = 300000000L / cycleLength;
    long start_ms;
    long end_ms;

    float[] JArray = makeJArray(cycleLength);
    FloatBuffer floatBuffer = makeDirectFloatBuffer(cycleLength);
    fillBufferFromJArray(floatBuffer, JArray);

    start_ms = System.currentTimeMillis();
    float total = 0.0f;
    for (long i = 0; i < repetitions; i++) {
      total += nativeProcessDirectBuffer(floatBuffer, cycleLength);
    }
    end_ms = System.currentTimeMillis();
    long processDirectBufferNatively_ms = end_ms - start_ms;
    reportMeasurement("process DirectBuffer natively", cycleLength, repetitions, start_ms, end_ms);

    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      total += javaProcessDirectBuffer(floatBuffer);
    }
    end_ms = System.currentTimeMillis();
    long processDirectBufferFromJava = end_ms - start_ms;
    reportMeasurement("process DirectBuffer from Java", cycleLength, repetitions, start_ms, end_ms);


    start_ms = System.currentTimeMillis();
    for (long i = 0; i < repetitions; i++) {
      total += javaProcessJArray(JArray);
    }
    end_ms = System.currentTimeMillis();
    long processJavaArray_ms = end_ms - start_ms;
    reportMeasurement("process Java Array", cycleLength, repetitions, start_ms, end_ms);

    reportConclusion("DirectBuffer natively", "JArray", 1d/processDirectBufferNatively_ms, 1d/processJavaArray_ms);
    reportConclusion("DirectBuffer from Java", "JArray", 1d/processDirectBufferFromJava, 1d/processJavaArray_ms);
  }


  private static void reportMeasurement(String subject, int cycleLength, long repetitions, long start, long end) {
    double duration_ms = end - start; // the duration of the test in ms.
    double perCall_ms = duration_ms / repetitions; // the duration of one call in ms.
    double cycleTime_ms = cycleLength / 44.100d; // the duration of one cycle in ms.
    double possible_calls_perCycle = cycleTime_ms / perCall_ms; // the number of times, we can do this operation in one cycle

    System.err.printf("--- %s-%d takes %.1f ns per call. (testing-time %.0f ms)%n", subject, cycleLength, perCall_ms * 1e6, duration_ms);
    System.err.printf("    maximum calls per cycle: %.0f%n", possible_calls_perCycle);
    System.err.printf("    calls per second: %.0f%n", 1000d/perCall_ms);

  }

  private static void reportConclusion(String subject_1, String subject_2, double speed_1, double speed_2) {
    double factor = (100d * speed_1/speed_2) -100d;
    System.err.printf("%s is by %.2f%% faster than %s.%n", subject_1, factor, subject_2);
  }
}

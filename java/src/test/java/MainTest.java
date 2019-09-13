import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.nio.FloatBuffer;

class MainTest {

  @BeforeAll
  static void install() {
    Main.loadNativeLib();
  }


  /**
   * makeDirectFloatBuffer() shall return a buffer:
   *
   * - of the required length
   * - that is direct
   */
  @Test
  void makeDirectFloatBuffer() {
    int length = 13;
    int maxIndex = length-1;
    FloatBuffer floatBuffer = Main.makeDirectFloatBuffer(length);

    assertThat(floatBuffer.capacity()).isEqualTo(length);
    assertThat(floatBuffer.isDirect()).isTrue();

    floatBuffer.put(maxIndex,1.1f);
    assertThat(floatBuffer.get(maxIndex)).isEqualTo(1.1f);

    assertThrows(IndexOutOfBoundsException.class, () -> {
      floatBuffer.put(maxIndex+1,1.1f);
    });
  }

  /**
   * makeJArray shall:
   *
   * 1. return an array of the required length
   * 2. the values should range between 1.0 and -1.0 and not all be 0.0
   */
  @Test
  void makeJArray() {
    int length = 13;
    float[] jArray = Main.makeJArray(length);

    // 1. return an array of the required length
    assertThat(jArray.length).isEqualTo(length);

    // 2. the values should range between 1.0 and -1.0 and not all be 0.0
    float minValue = 0.0f;
    float maxValue = 0.0f;
    for(float val: jArray){
      minValue = Math.min(minValue, val);
      maxValue = Math.max(maxValue, val);
    }

    assertThat(minValue).isAtLeast(-1);
    assertThat(maxValue).isAtMost(1);

    assertThat(minValue).isLessThan(-0.1f);
    assertThat(maxValue).isGreaterThan(0.1f);
  }

  /**
   * After `fillBufferFromJArray` is applied to a `floatBuffer` and a `jArray`
   * both should have the same content.
   */
  @Test
  void fillBufferFromJArray() {
    int length = 13;
    FloatBuffer floatBuffer = Main.makeDirectFloatBuffer(length);
    float[] jArray = Main.makeJArray(length);

    Main.fillBufferFromJArray(floatBuffer, jArray);

    for(int i= 0;i<length;i++){
      assertThat(jArray[i]).isEqualTo(floatBuffer.get(i));
    }
  }
  /**
   * Testing `nativeWriteOutDirectBuffer` and `nativeReadInDirectBuffer`.
   */
  @Test
  void nativeWriteReadDirectBuffer() {
    // Prepare the writeBuffer with known data.
    int length = 13;
    float[] referenceData = Main.makeJArray(length);
    FloatBuffer writeBuffer = Main.makeDirectFloatBuffer(length);
    Main.fillBufferFromJArray(writeBuffer, referenceData);

    // now write this data to native.
    Main.nativeWriteOutDirectBuffer(writeBuffer, length);

    // prepare the read buffer.
    FloatBuffer readBuffer = Main.makeDirectFloatBuffer(length);
    // read- back the data from native.
    Main.nativeReadInDirectBuffer(readBuffer,length);

    // check that we got the expected data in our red buffer
    for(int i= 0;i<length;i++){
      assertThat(readBuffer.get(i)).isEqualTo(referenceData[i]);
    }
  }


  /**
   * Testing `nativeWriteOutJArray` and `nativeReadJArray`.
   */
  @Test
  void nativeWriteReadDirectJArray() {
    // prepare the writeArray with known data
    int length = 13;
    float[] data = Main.makeJArray(length);
    float[] writeArray = data.clone();

    // now write this data to native
    Main.nativeWriteOutJArray(writeArray, length);

    // prepare the read array
    float[] readArray = new float[length];
    // read back the data from native
    Main.nativeReadJArray(readArray,length);

    // check that we got the expected data in our red Array
    assertThat(readArray).isEqualTo(data);

  }

  /**
   * Verify that `javaProcessJArray` returns a plausible result.
   */
  @Test
  void javaProcessJArray() {
    // prepare data
    int length = 13;
    float[] data = Main.makeJArray(length);

    // calculate the Euclidean Norm
    float norm = Main.javaProcessJArray(data);

    // As the there are some vector components >0 the norm must be >0
    assertThat(norm).isGreaterThan(0);
    // As the there are some vector components <1 the norm must be less than the dimension.
    assertThat(norm).isLessThan(length);

  }

  /**
   * Verify that `nativeProcessDirectBuffer` return about the same result as `javaProcessJArray`.
   */
  @Test
  void nativeProcessDirectBuffer() {
    // prepare the floatBuffer with known data
    int length = 3000;
    float[] data = Main.makeJArray(length);
    FloatBuffer floatBuffer = Main.makeDirectFloatBuffer(length);
    Main.fillBufferFromJArray(floatBuffer, data);

    // For reference, calculate the Euclidean-Norm in java.
    float reference = Main.javaProcessJArray(data);

    // check, if we get the same by doing it in the native lib.
    float norm = Main.nativeProcessDirectBuffer(floatBuffer, length);
    assertThat(norm).isWithin(1.0e-18f).of(reference);
  }

  /**
   * Verify that `javaProcessDirectBuffer` return about the same result as `javaProcessJArray`.
   */
  @Test
  void javaProcessDirectBuffer(){
    // prepare the floatBuffer with known data
    int length = 3000;
    float[] data = Main.makeJArray(length);
    FloatBuffer floatBuffer = Main.makeDirectFloatBuffer(length);
    Main.fillBufferFromJArray(floatBuffer, data);

    // For reference, calculate the Euclidean-Norm in java.
    float reference = Main.javaProcessJArray(data);

    // check, if we get the same by doing it in the native lib.
    float norm = Main.javaProcessDirectBuffer(floatBuffer);
    assertThat(norm).isWithin(1.0e-18f).of(reference);
  }


}
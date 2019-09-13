/*
 * File: main.cpp
 *
 *
 * Copyright 2019 Harald Postner <Harald at free_creations.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <jni.h>
#include <cstring>
#include <cmath>
#include "jni_headers/Main.h"


#ifndef JNI_VERSION_1_2
#error "Needs Java version 1.2 or higher.\n"
#endif
using namespace std;
#define MAX_CYCLE_LENGTH  4096

/**
 * This simulates the data that go to an output port or come from an input port.
 */
static float localBuffer[MAX_CYCLE_LENGTH];



/**
 * Simulate the writing of data into the native world using direct buffers.
 *
 * The native implementation shall use `GetDirectBufferAddress` to access the buffer data
 * and `memcopy` to transfer the data from the buffer to some native data structure.
 *
 * @param env the Java environment.
 * @param directFloatOutputBuffer the data that would be written to a port or so.
 * @param count the number of items to be written (the size of the directFloatOutputBuffer).
 *
 * > static native void nativeWriteOutDirectBuffer(FloatBuffer directFloatOutputBuffer,int count);
*/
JNIEXPORT void JNICALL Java_Main_nativeWriteOutDirectBuffer
        (JNIEnv *env, jclass, jobject directFloatOutputBuffer, jint count) {

    auto javaBuffer = (float *) env->GetDirectBufferAddress(directFloatOutputBuffer);
    if (!javaBuffer) {
        env->FatalError("given object is not a direct java.nio.Buffer");
        return;
    }
    if (count > MAX_CYCLE_LENGTH) {
        env->FatalError("count exceeds the maximum cycle length");
        return;
    }

    memcpy(&localBuffer, javaBuffer, count * sizeof(float));
}
/**
 * Simulate the reading of data from the native world using direct buffers.
 *
 * The native implementation shall use `GetDirectBufferAddress` to access the buffer data
 * and `memcopy` to transfer the data from some native data structure to the buffer.
 *
 * @param env the Java environment.
 * @param directFloatInputBuffer a container that gets filled by the native part.
 * @param count the number of items to be written (the size of the directFloatOutputBuffer).
 * > static native void nativeReadInDirectBuffer(FloatBuffer directFloatInputBuffer, int count);
 */
JNIEXPORT void JNICALL Java_Main_nativeReadInDirectBuffer
        (JNIEnv *env, jclass, jobject directFloatInputBuffer, jint count) {

    auto javaBuffer = (float *) env->GetDirectBufferAddress(directFloatInputBuffer);
    if (!javaBuffer) {
        env->FatalError("given object is not a direct java.nio.Buffer");
        return;
    }
    if (count > MAX_CYCLE_LENGTH) {
        env->FatalError("count exceeds the maximum cycle length");
        return;
    }
    memcpy(javaBuffer, &localBuffer, count * sizeof(float));
}

/**
 * Simulate the writing of data into the native world using java arrays.
 *
 * The native implementation shall use `SetFloatArrayRegion` to access and
 * transfer the data from the array to some native data structure.
 *
 * @param env the Java environment.
 * @param floatOutputArray data that should be written to a port or so.
 * >static native void nativeWriteOutJArray(float[] floatOutputArray);
 */
JNIEXPORT void JNICALL Java_Main_nativeWriteOutJArray
        (JNIEnv *env, jclass, jfloatArray floatOutputArray, jint count) {
    if (count > MAX_CYCLE_LENGTH) {
        env->FatalError("count exceeds the maximum cycle length");
        return;
    }
    env->GetFloatArrayRegion(floatOutputArray, 0, count, localBuffer);

}
/**
 * Simulate the reading of data from the native world.
 *
 * The native implementation shall use `GetFloatArrayRegion` to access and
 * transfer the data from some native data structure into the array.
 *
 * @param env the Java environment.
 * @param floatInputArray a container that gets filled by the native part.
 * > static native void nativeReadJArray(float[] floatInputArray);
 */
JNIEXPORT void JNICALL Java_Main_nativeReadJArray
        (JNIEnv *env, jclass, jfloatArray floatInputArray, jint count) {

    if (count > MAX_CYCLE_LENGTH) {
        env->FatalError("count exceeds the maximum cycle length");
        return;
    }
    env->SetFloatArrayRegion(floatInputArray, 0, count, localBuffer);
}

/**
 * Do some calculations within the native world involving the elements of a direct buffer.
 *
 * For the sake of this example we'll calculate the Euclidean-norm of the buffer elements.
 *
 * @param env the Java environment.
 * @param directFloatBuffer a buffer filled with non trivial data (not all zero).
 * @param count the size of the directFloatOutputBuffer.
 * @return the Euclidean norm of the buffer content.
 * > static native float nativeProcessDirectBuffer(FloatBuffer directFloatBuffer, int count);
 */
JNIEXPORT jfloat JNICALL Java_Main_nativeProcessDirectBuffer
        (JNIEnv *env, jclass, jobject directFloatBuffer, jint count) {

    auto javaBuffer = (float *) env->GetDirectBufferAddress(directFloatBuffer);
    if (!javaBuffer) {
        env->FatalError("given object is not a direct java.nio.Buffer");
        return 0.0;
    }
    jfloat result = 0.0;
    for (int i = 0; i < count; i++) {
        result += javaBuffer[i] * javaBuffer[i];
    }
    return sqrt(result);

}




import ai.onnxruntime.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestOrt {
    public static void main(String[] args) throws Exception {
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        
        // Option 1: ByteBuffer
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        buf.put((byte)0);
        buf.rewind();
        OnnxTensor t1 = OnnxTensor.createTensor(env, buf, new long[]{1}, OnnxJavaType.BOOL);
        System.out.println("T1 value (byte buffer 0): " + ((boolean[]) t1.getValue())[0]);
        
        buf.put(0, (byte)1);
        OnnxTensor t2 = OnnxTensor.createTensor(env, buf, new long[]{1}, OnnxJavaType.BOOL);
        System.out.println("T2 value (byte buffer 1): " + ((boolean[]) t2.getValue())[0]);
        
        // Option 2: arrayOf
        OnnxTensor t3 = OnnxTensor.createTensor(env, new boolean[]{false});
        System.out.println("T3 value (boolean array false): " + ((boolean[]) t3.getValue())[0]);
    }
}

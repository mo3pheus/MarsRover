package space.exploration.mars.rover.communication;

import java.io.*;

public class SerializationUtil {
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object deserialize(byte[] messageBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn   = new ByteArrayInputStream(messageBytes);
        ObjectInputStream    objectIn = new ObjectInputStream(byteIn);
        return objectIn.readObject();
    }
}

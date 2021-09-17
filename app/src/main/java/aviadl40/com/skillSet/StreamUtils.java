package aviadl40.com.skillSet;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class StreamUtils {
	private static final ByteArrayOutputStream BAOS = new ByteArrayOutputStream(1024);

	public static Bitmap readBitmap(InputStream i) throws IOException {
		byte[] bs = new byte[readShort(i) & 0xffff];
		if (i.read(bs) != bs.length)
			throw new SocketException("end of stream");
		return BitmapFactory.decodeByteArray(bs, 0, bs.length);
	}

	public static short readByteUnsigned(InputStream i) throws IOException {
		int b = i.read();
		if (b == -1)
			throw new SocketException("end of stream");
		return (short) b;
	}

	public static boolean readBoolean(InputStream i) throws IOException {
		return readByteUnsigned(i) > 0;
	}

	public static <T extends Enum<T>> T readEnum(InputStream i, Class<T> c) throws IOException {
		short index = readByteUnsigned(i);
		T[] constants = c.getEnumConstants();
		if (constants == null)
			throw new IOException("Class object " + c + " does not represent an enum type");
		if (index > constants.length)
			throw new IOException("unknown enum constant: " + c.getSimpleName() + '.' + index);
		return constants[index];
	}

	public static short readShort(InputStream i) throws IOException {
		byte[] bs = new byte[2];
		if (i.read(bs) != bs.length)
			throw new SocketException("end of stream");

		// TODO: not memory efficient
		return ByteBuffer.wrap(bs).getShort();
	}

	public static String readString(InputStream i) throws IOException {
		byte[] bs = new byte[readByteUnsigned(i)]; // Does not read null!
		i.read(bs);
		return new String(bs, StandardCharsets.UTF_8);
	}

	public static void write(OutputStream o, boolean bool) throws IOException {
		o.write(new byte[]{(byte) (bool ? 1 : 0)});
	}

	public static void write(OutputStream o, Enum<?> e) throws IOException {
		o.write(e.ordinal());
	}

	public static void write(OutputStream o, short s) throws IOException {
		o.write(new byte[]{(byte) (s >> 8), (byte) (s % 256)});
	}

	public static void write(OutputStream o, String s) throws IOException {
		// Does not support null String
		byte[] bs = s.getBytes(StandardCharsets.UTF_8);
		o.write(bs.length);
		o.write(bs);
	}

	public static void write(OutputStream o, Bitmap image, boolean hasTransparency) throws IOException {
		synchronized (BAOS) {
			try {
				if (!image.compress(hasTransparency ? CompressFormat.PNG : CompressFormat.JPEG, 92, BAOS))
					throw new IOException("failed to compress image");
				if (BAOS.size() >= Item.ImageItem.MAX_SIZE)
					throw new IOException("image is too big");
				write(o, (short) BAOS.size());
				BAOS.writeTo(o);
			} finally {
				BAOS.reset();
			}
		}
	}
}
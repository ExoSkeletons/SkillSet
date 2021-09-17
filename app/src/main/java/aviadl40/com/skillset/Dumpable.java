package aviadl40.com.skillset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Dumpable {
	void readFrom(InputStream i) throws IOException;

	void writeTo(OutputStream o) throws IOException;
}
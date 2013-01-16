package com.aliyun.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.util.EncodingUtil;
import org.eclipse.jetty.io.Buffer;

public class ChunkedInputStream {
	
	private int getChunkSizeFromInputStream(final Buffer content) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// States: 0=normal, 1=\r was scanned, 2=inside quoted string, -1=end
		int state = 0;
		while (state != -1) {
			int b = content.get();
			if (b == -1) {
				throw new IOException("chunked stream ended unexpectedly");
			}
			switch (state) {
			case 0:
				switch (b) {
				case '\r':
					state = 1;
					break;
				case '\"':
					state = 2;
					/* fall through */
				default:
					baos.write(b);
				}
				break;

			case 1:
				if (b == '\n') {
					state = -1;
				} else {
					// this was not CRLF
					throw new IOException("Protocol violation: Unexpected" + " single newline character in chunk size");
				}
				break;

			case 2:
				switch (b) {
				case '\\':
					b = content.get();
					baos.write(b);
					break;
				case '\"':
					state = 0;
					/* fall through */
				default:
					baos.write(b);
				}
				break;
			default:
				throw new RuntimeException("assertion failed");
			}
		}

		// parse data
		String dataString = EncodingUtil.getAsciiString(baos.toByteArray());
		int separator = dataString.indexOf(';');
		dataString = (separator > 0) ? dataString.substring(0, separator).trim() : dataString.trim();

		int result;
		try {
			result = Integer.parseInt(dataString.trim(), 16);
		} catch (NumberFormatException e) {
			throw new IOException("Bad chunk size: " + dataString);
		}
		return result;
	}
}

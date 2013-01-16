package com.aliyun.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.util.EncodingUtil;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.io.Buffer;

public class ChunkClient2 {

	private static int getChunkSizeFromInputStream(final Buffer content) throws IOException {

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

	private class ChunkResponse {
		private int pos;
		private int size;
		private StringBuffer response = new StringBuffer();
	}

	public void call(HttpClient client) {

		final ChunkResponse chunkResponse = new ChunkResponse();

		HttpExchange exchange = new ContentExchange() {

			protected void onConnectionFailed(Throwable x) {
				x.printStackTrace();
			}

			protected void onResponseComplete() throws IOException {
			}

			protected void onException(Throwable x) {
				x.printStackTrace();
			}

			protected void onResponseContent(Buffer content) throws IOException {
				onResponseChunked(content);
			}

			protected void onResponseChunked(Buffer content) throws IOException {
				if (chunkResponse.pos == 0 && content.hasContent()) {
					chunkResponse.size = getChunkSizeFromInputStream(content);
				}

				String buffer = new String(content.asArray());
				chunkResponse.response.append(buffer);
				chunkResponse.pos += buffer.length();

				if (chunkResponse.size + 2 == chunkResponse.pos) {
					System.out.println(chunkResponse.response.toString());

					// reset
					chunkResponse.response.delete(0, chunkResponse.size);
					chunkResponse.pos = 0;
					chunkResponse.size = 0;
				}

			}

		};

		try {
			exchange.setScheme(HttpSchemes.HTTP);
			exchange.setVersion(HttpVersions.HTTP_1_1);
			exchange.setMethod(HttpMethods.GET);
			exchange.setTimeout(10000);
			exchange.setURL("http://127.0.0.1:8080/httpchunk/main?mode=chunked");
			exchange.setRequestHeader(HttpHeaders.HOST, "*");
			exchange.setRequestHeader(HttpHeaders.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

			client.send(exchange);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpClient client = new HttpClient();
		client.setConnectBlocking(true);
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		try {
			client.start();

			ChunkClient2 chunkClient = new ChunkClient2();
			chunkClient.call(client);

			Thread.sleep(1000000);
			client.stop();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

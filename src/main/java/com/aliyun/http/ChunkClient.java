package com.aliyun.http;

import java.io.IOException;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class ChunkClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpClient client = new HttpClient();

		GetMethod method = new GetMethod(
				"http://10.1.171.196:8080/httpchunk/main?mode=chunked");
		ChunkedInputStream inputStream = null;
		try {
			client.executeMethod(method);

			inputStream = new ChunkedInputStream(
					method.getResponseBodyAsStream(), method);

			StringBuffer buffer = new StringBuffer();
			int ch;
			while ((ch = inputStream.read()) != -1) {
				buffer.append((char) ch);
			}
			System.out.println(buffer.toString());

		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}


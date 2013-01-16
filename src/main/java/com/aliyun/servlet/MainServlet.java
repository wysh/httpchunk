package com.aliyun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1561025702742066433L;

	private String process() {
		String head = "{\"code\": 200,\"msg\": \"successful\",\"data\": [    ";
		String body = "{\"vm_name\": \"AY11050408313426aa1e4\",\"aliyun_idkp\": \"47243\",\"internet_tx\": 5120,\"hostname\": \"AY1105030750551\",\"gmt_create\": \"2011-05-03 19:50:55\",\"region_no\": \"cn-hangzhou-dg-a01\",\"zone_no\": \"cn-hangzhou-1-a\",\"internet_ip\": [{\"start_ip\": \"223.41.2.0\",\"end_ip\": \"223.41.2.255\"},{\"start_ip\": \"223.41.3.1\",\"end_ip\": \"223.41.3.1\"}],\"intranet_ip\": [{\"start_ip\": \"10.1.1.1\",\"end_ip\": \"10.1.1.1\"}]}";

		StringBuffer buffer = new StringBuffer(head);
		buffer.append(body);

		for (int i = 0; i < 100; i++) {
			buffer.append(",");
			buffer.append(body);
		}
		buffer.append("]}");
		return buffer.toString();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if ("chunked".equals(req.getParameter("mode"))) {
			resp.addHeader("Transfer-Encoding", "chunked");
			resp.addHeader("Content-Type", "text/plain");

			for (int i = 0; i < 2; i++) {
				String body = process();
				String hex = Integer.toHexString(body.getBytes().length);
				resp.getWriter().write(hex + "\r\n" + body + "\r\n");
				resp.getWriter().flush();
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			resp.getWriter().close();
		} else {
			// resp.addHeader("Content-Length","10");
			resp.getWriter().write("no chunked");
			resp.getWriter().flush();
			resp.getWriter().close();
		}

	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	public static void main(String[] args) {
		// System.out.println(Integer.toHexString(process().getBytes().length));
	}
}
